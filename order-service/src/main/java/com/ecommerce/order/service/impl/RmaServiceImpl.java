package com.ecommerce.order.service.impl;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.dto.QcRequestDto;
import com.ecommerce.order.dto.ReturnRequestDto;
import com.ecommerce.order.dto.ReturnResponseDto;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.ReturnItemRepository;
import com.ecommerce.order.repository.ReturnRequestRepository;
import com.ecommerce.order.service.PaymentService;
import com.ecommerce.order.service.RmaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RmaServiceImpl implements RmaService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnItemRepository returnItemRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public ReturnResponseDto submitReturn(String userId, ReturnRequestDto request) {
        log.info("Submitting return request for Order ID: {}, User: {}", request.getOrderId(), userId);

        // 1. Locate the order
        Order order = orderRepository.findById(new OrderId(request.getOrderId(), request.getOrderCreatedAt()))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Order not found with ID: " + request.getOrderId()));

        // 2. Validate order ownership
        if (!order.getUserId().toString().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Access denied: Order does not belong to user.");
        }

        // 3. Validate completion status
        if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only completed orders can be returned.");
        }

        // 4. Validate order completed under 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        if (order.getUpdatedAt() != null && order.getUpdatedAt().isBefore(sevenDaysAgo)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Returns are only allowed within 7 days of completion.");
        }

        // 5. Generate request ID
        long returnRequestId = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(1000);

        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<ReturnItem> returnItems = new ArrayList<>();

        // 6. Process return items and calculate refund amounts
        for (ReturnRequestDto.ReturnItemDto itemDto : request.getItems()) {
            OrderItem orderItem = orderItemRepository.findById(new OrderItemId(itemDto.getOrderItemId(), request.getOrderCreatedAt()))
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Order item not found with ID: " + itemDto.getOrderItemId()));

            if (itemDto.getQuantity() > orderItem.getQuantity()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Return quantity exceeds purchased quantity for item: " + itemDto.getOrderItemId());
            }

            // Calculate refund price for the item (unit price after proportional item discount)
            BigDecimal unitDiscount = orderItem.getDiscountAmount() != null 
                    ? orderItem.getDiscountAmount().divide(BigDecimal.valueOf(orderItem.getQuantity()), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal refundPrice = orderItem.getUnitPrice().subtract(unitDiscount);
            BigDecimal itemRefundAmount = refundPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalRefundAmount = totalRefundAmount.add(itemRefundAmount);

            ReturnItem returnItem = ReturnItem.builder()
                    .returnRequestId(returnRequestId)
                    .orderItemId(itemDto.getOrderItemId())
                    .quantity(itemDto.getQuantity())
                    .refundPrice(refundPrice)
                    .condition("UNOPENED") // Default condition when submitting return
                    .orderCreatedAt(request.getOrderCreatedAt())
                    .build();

            returnItems.add(returnItem);
        }

        // 7. Save ReturnRequest
        ReturnRequest returnRequest = ReturnRequest.builder()
                .id(returnRequestId)
                .orderId(request.getOrderId())
                .userId(Long.parseLong(userId))
                .reason(request.getReason())
                .status("PENDING")
                .refundAmount(totalRefundAmount)
                .refundStatus("PENDING")
                .orderCreatedAt(request.getOrderCreatedAt())
                .build();

        returnRequestRepository.save(returnRequest);
        returnItemRepository.saveAll(returnItems);

        log.info("Successfully created Return Request ID: {} with refund amount: {}", returnRequestId, totalRefundAmount);
        return mapToResponse(returnRequest, returnItems);
    }

    @Override
    @Transactional
    public ReturnResponseDto approveReturn(Long returnId, LocalDateTime orderCreatedAt) {
        log.info("Approving return request ID: {}", returnId);

        ReturnRequest returnRequest = returnRequestRepository.findById(new ReturnRequestId(returnId, orderCreatedAt))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Return request not found with ID: " + returnId));

        if (!"PENDING".equalsIgnoreCase(returnRequest.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Return request must be in PENDING status to approve.");
        }

        // Generate return tracking number / label
        String trackingNumber = "RET-TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        returnRequest.setStatus("APPROVED");
        returnRequest.setReturnTrackingNumber(trackingNumber);
        returnRequest.setUpdatedAt(LocalDateTime.now());
        returnRequestRepository.save(returnRequest);

        List<ReturnItem> items = returnItemRepository.findByReturnRequestId(returnId);
        log.info("Return request ID: {} approved with tracking number: {}", returnId, trackingNumber);
        return mapToResponse(returnRequest, items);
    }

    @Override
    @Transactional
    public ReturnResponseDto processQc(QcRequestDto request) {
        log.info("Processing QC Gate for Return ID: {}, Passed: {}", request.getReturnRequestId(), request.isQcPassed());

        ReturnRequest returnRequest = returnRequestRepository.findById(new ReturnRequestId(request.getReturnRequestId(), request.getOrderCreatedAt()))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Return request not found with ID: " + request.getReturnRequestId()));

        if (!"APPROVED".equalsIgnoreCase(returnRequest.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Return request must be APPROVED before quality control check.");
        }

        List<ReturnItem> dbItems = returnItemRepository.findByReturnRequestId(request.getReturnRequestId());

        // Update each item inspection status
        for (QcRequestDto.QcItemDto qcItem : request.getItems()) {
            ReturnItem returnItem = dbItems.stream()
                    .filter(item -> item.getOrderItemId().equals(qcItem.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Returned item not found for order item ID: " + qcItem.getOrderItemId()));

            returnItem.setCondition(qcItem.getCondition());
            returnItem.setInspectedBy(request.getInspectedBy());
            returnItem.setInspectionNotes(request.getInspectionNotes());
            returnItemRepository.save(returnItem);
        }

        if (request.isQcPassed()) {
            log.info("QC Pass. Initiating automatic refund...");
            returnRequest.setStatus("ITEM_RECEIVED");

            // Execute payment refund via VNPay/Stripe
            boolean refundSuccess = paymentService.refundPayment(returnRequest.getOrderId(), returnRequest.getRefundAmount());
            if (refundSuccess) {
                returnRequest.setStatus("REFUNDED");
                returnRequest.setRefundStatus("SUCCESS");
                log.info("Automatic refund successfully completed for Order ID: {}", returnRequest.getOrderId());
            } else {
                returnRequest.setRefundStatus("FAILED");
                log.error("Automatic refund failed for Order ID: {}", returnRequest.getOrderId());
            }
        } else {
            log.warn("QC Fail. Rejecting return request ID: {} and blocking payment.", request.getReturnRequestId());
            returnRequest.setStatus("REJECTED");
            returnRequest.setRefundStatus("REJECTED");
        }

        returnRequest.setUpdatedAt(LocalDateTime.now());
        returnRequestRepository.save(returnRequest);

        return mapToResponse(returnRequest, dbItems);
    }

    private ReturnResponseDto mapToResponse(ReturnRequest req, List<ReturnItem> items) {
        return ReturnResponseDto.builder()
                .id(req.getId())
                .orderId(req.getOrderId())
                .reason(req.getReason())
                .status(req.getStatus())
                .refundAmount(req.getRefundAmount())
                .refundStatus(req.getRefundStatus())
                .returnTrackingNumber(req.getReturnTrackingNumber())
                .createdAt(req.getCreatedAt())
                .orderCreatedAt(req.getOrderCreatedAt())
                .items(items.stream().map(item -> ReturnResponseDto.ReturnItemResponseDto.builder()
                        .id(item.getId())
                        .orderItemId(item.getOrderItemId())
                        .quantity(item.getQuantity())
                        .refundPrice(item.getRefundPrice())
                        .condition(item.getCondition())
                        .inspectedBy(item.getInspectedBy())
                        .inspectionNotes(item.getInspectionNotes())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
