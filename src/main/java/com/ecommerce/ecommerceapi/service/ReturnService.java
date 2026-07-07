package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.ReturnRequestCreateDTO;
import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReturnService {

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private NotificationService notificationService;

    public ReturnRequest createReturnRequest(Integer userId, Integer orderId, ReturnRequestCreateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền yêu cầu hoàn hàng cho đơn hàng này!");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Chỉ đơn hàng đã giao thành công mới có thể yêu cầu hoàn trả!");
        }

        if (returnRequestRepository.findByOrderId(orderId).isPresent()) {
            throw new BadRequestException("Đơn hàng này đã có yêu cầu hoàn trả trước đó!");
        }

        ReturnRequest request = ReturnRequest.builder()
                .order(order)
                .user(order.getUser())
                .reason(dto.getReason())
                .imagesUrl(dto.getImagesUrl())
                .status(ReturnStatus.PENDING)
                .build();

        ReturnRequest saved = returnRequestRepository.save(request);

        // Notify Seller
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            Shop shop = order.getOrderItems().get(0).getProduct().getShop();
            if (shop != null && shop.getUser() != null) {
                notificationService.createNotification(
                        shop.getUser().getId(),
                        "Yêu cầu hoàn trả mới",
                        "Đơn hàng " + order.getOrderCode() + " đã gửi yêu cầu trả hàng/hoàn tiền. Lý do: " + dto.getReason(),
                        null,
                        null
                );
            }
        }

        return saved;
    }

    public List<ReturnRequest> getReturnsForUser(Integer userId) {
        return returnRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<ReturnRequest> getReturnsForSeller(Integer userId) {
        Shop shop = shopRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cửa hàng của người bán!"));
        return returnRequestRepository.findByOrderShopIdOrderByCreatedAtDesc(shop.getId());
    }

    public List<ReturnRequest> getAllReturnsForAdmin() {
        return returnRequestRepository.findAll();
    }

    public ReturnRequest getReturnById(Integer id) {
        return returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu hoàn hàng!"));
    }

    public ReturnRequest sellerRespond(Integer returnId, Integer sellerUserId, boolean approved, String note) {
        ReturnRequest request = getReturnById(returnId);
        
        Shop shop = shopRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cửa hàng của người bán!"));

        if (request.getOrder().getOrderItems() == null || request.getOrder().getOrderItems().isEmpty()) {
            throw new BadRequestException("Đơn hàng không hợp lệ!");
        }

        Shop orderShop = request.getOrder().getOrderItems().get(0).getProduct().getShop();
        if (!orderShop.getId().equals(shop.getId())) {
            throw new BadRequestException("Bạn không có quyền phản hồi yêu cầu của cửa hàng khác!");
        }

        if (request.getStatus() != ReturnStatus.PENDING) {
            throw new BadRequestException("Yêu cầu hoàn trả này đã được xử lý trước đó!");
        }

        if (approved) {
            request.setStatus(ReturnStatus.APPROVED);
            request.setSellerNote(note != null ? note : "Người bán đã chấp nhận yêu cầu hoàn trả.");
        } else {
            request.setStatus(ReturnStatus.REJECTED);
            request.setSellerNote(note != null ? note : "Người bán đã từ chối yêu cầu hoàn trả.");
        }

        ReturnRequest saved = returnRequestRepository.save(request);

        // Notify Buyer
        notificationService.createNotification(
                request.getUser().getId(),
                "Cập nhật yêu cầu trả hàng",
                "Yêu cầu trả hàng cho đơn " + request.getOrder().getOrderCode() + " đã được người bán " + 
                        (approved ? "chấp nhận. Vui lòng chờ quản trị viên hoàn tiền." : "từ chối. Lý do: " + note),
                null,
                null
        );

        return saved;
    }

    public ReturnRequest adminResolve(Integer returnId, boolean refund, String note) {
        ReturnRequest request = getReturnById(returnId);

        if (request.getStatus() != ReturnStatus.APPROVED && request.getStatus() != ReturnStatus.PENDING) {
            throw new BadRequestException("Chỉ yêu cầu đang chờ duyệt hoặc đã được người bán đồng ý mới có thể hoàn tiền!");
        }

        if (refund) {
            request.setStatus(ReturnStatus.REFUNDED);
            request.setAdminNote(note != null ? note : "Quản trị viên đã duyệt hoàn tiền thành công.");
        } else {
            request.setStatus(ReturnStatus.CLOSED);
            request.setAdminNote(note != null ? note : "Quản trị viên từ chối duyệt hoàn tiền. Đóng yêu cầu.");
        }

        ReturnRequest saved = returnRequestRepository.save(request);

        // Notify Buyer & Seller
        notificationService.createNotification(
                request.getUser().getId(),
                "Hoàn tiền đơn hàng",
                "Yêu cầu trả hàng cho đơn " + request.getOrder().getOrderCode() + " đã được quản trị viên xử lý: " + 
                        (refund ? "Đã hoàn tiền thành công." : "Không được duyệt hoàn tiền."),
                null,
                null
        );

        if (request.getOrder().getOrderItems() != null && !request.getOrder().getOrderItems().isEmpty()) {
            Shop shop = request.getOrder().getOrderItems().get(0).getProduct().getShop();
            if (shop != null && shop.getUser() != null) {
                notificationService.createNotification(
                        shop.getUser().getId(),
                        "Cập nhật xử lý hoàn hàng",
                        "Đơn hoàn hàng cho mã " + request.getOrder().getOrderCode() + " đã được quản trị viên xử lý: " + 
                                (refund ? "Đồng ý hoàn tiền." : "Từ chối hoàn tiền."),
                        null,
                        null
                );
            }
        }

        return saved;
    }
}
