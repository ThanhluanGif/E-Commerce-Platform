package com.ecommerce.order.service.impl;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.entity.InventoryTransaction;
import com.ecommerce.order.entity.WarehouseStock;
import com.ecommerce.order.repository.InventoryTransactionRepository;
import com.ecommerce.order.repository.WarehouseStockRepository;
import com.ecommerce.order.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final WarehouseStockRepository warehouseStockRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public void verifyStock(Long productVariantId, int quantity) {
        List<WarehouseStock> stocks = warehouseStockRepository.findByProductVariantId(productVariantId);
        int totalAvailable = stocks.stream()
                .mapToInt(s -> s.getPhysicalQty() - s.getReservedQty())
                .sum();
        if (totalAvailable < quantity) {
            log.warn("Insufficient stock for variant ID {}. Requested: {}, Available: {}", productVariantId, quantity, totalAvailable);
            throw new AppException(HttpStatus.BAD_REQUEST, 
                    "Insufficient stock for variant ID: " + productVariantId + ". Available: " + totalAvailable + ", Requested: " + quantity);
        }
    }

    @Override
    @Transactional
    public void reserveStock(Long orderId, Long productVariantId, int quantity) {
        List<WarehouseStock> stocks = warehouseStockRepository.findByProductVariantId(productVariantId);
        
        // Try to reserve from a single warehouse first
        WarehouseStock selectedStock = null;
        for (WarehouseStock stock : stocks) {
            int available = stock.getPhysicalQty() - stock.getReservedQty();
            if (available >= quantity) {
                selectedStock = stock;
                break;
            }
        }

        if (selectedStock != null) {
            selectedStock.setReservedQty(selectedStock.getReservedQty() + quantity);
            selectedStock.setUpdatedAt(LocalDateTime.now());
            warehouseStockRepository.save(selectedStock);

            InventoryTransaction tx = InventoryTransaction.builder()
                    .warehouse(selectedStock.getWarehouse())
                    .productVariantId(productVariantId)
                    .type("RESERVE")
                    .quantity(quantity)
                    .referenceType("ORDER")
                    .referenceId(orderId)
                    .createdAt(LocalDateTime.now())
                    .build();
            inventoryTransactionRepository.save(tx);
            log.info("Successfully reserved {} items of variant {} in warehouse {}", quantity, productVariantId, selectedStock.getWarehouse().getId());
            return;
        }

        // If no single warehouse has enough, split the reservation across multiple warehouses
        int remainingToReserve = quantity;
        for (WarehouseStock stock : stocks) {
            int available = stock.getPhysicalQty() - stock.getReservedQty();
            if (available > 0) {
                int reserveFromThis = Math.min(available, remainingToReserve);
                stock.setReservedQty(stock.getReservedQty() + reserveFromThis);
                stock.setUpdatedAt(LocalDateTime.now());
                warehouseStockRepository.save(stock);

                InventoryTransaction tx = InventoryTransaction.builder()
                        .warehouse(stock.getWarehouse())
                        .productVariantId(productVariantId)
                        .type("RESERVE")
                        .quantity(reserveFromThis)
                        .referenceType("ORDER")
                        .referenceId(orderId)
                        .createdAt(LocalDateTime.now())
                        .build();
                inventoryTransactionRepository.save(tx);

                remainingToReserve -= reserveFromThis;
                log.info("Reserved partial {} items of variant {} in warehouse {}", reserveFromThis, productVariantId, stock.getWarehouse().getId());
                if (remainingToReserve == 0) {
                    break;
                }
            }
        }

        if (remainingToReserve > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, 
                    "Insufficient stock for variant ID: " + productVariantId + " during reservation. Remaining to reserve: " + remainingToReserve);
        }
    }
}
