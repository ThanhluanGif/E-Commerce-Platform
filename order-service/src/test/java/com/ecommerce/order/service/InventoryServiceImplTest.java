package com.ecommerce.order.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.entity.InventoryTransaction;
import com.ecommerce.order.entity.Warehouse;
import com.ecommerce.order.entity.WarehouseStock;
import com.ecommerce.order.repository.InventoryTransactionRepository;
import com.ecommerce.order.repository.WarehouseStockRepository;
import com.ecommerce.order.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void testVerifyStockSuccess() {
        WarehouseStock stock = WarehouseStock.builder()
                .physicalQty(10)
                .reservedQty(2)
                .build();
        when(warehouseStockRepository.findByProductVariantId(1L))
                .thenReturn(Collections.singletonList(stock));

        assertDoesNotThrow(() -> inventoryService.verifyStock(1L, 5));
    }

    @Test
    void testVerifyStockInsufficient() {
        WarehouseStock stock = WarehouseStock.builder()
                .physicalQty(10)
                .reservedQty(8)
                .build();
        when(warehouseStockRepository.findByProductVariantId(1L))
                .thenReturn(Collections.singletonList(stock));

        AppException ex = assertThrows(AppException.class, () -> inventoryService.verifyStock(1L, 5));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void testReserveStockSingleWarehouse() {
        Warehouse wh = Warehouse.builder().id(1L).code("WH-1").build();
        WarehouseStock stock = WarehouseStock.builder()
                .warehouse(wh)
                .physicalQty(10)
                .reservedQty(2)
                .build();
        when(warehouseStockRepository.findByProductVariantIdForUpdate(1L))
                .thenReturn(Collections.singletonList(stock));

        inventoryService.reserveStock(100L, 1L, 5);

        assertEquals(7, stock.getReservedQty());
        verify(warehouseStockRepository, times(1)).save(stock);

        ArgumentCaptor<InventoryTransaction> txCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionRepository, times(1)).save(txCaptor.capture());
        InventoryTransaction tx = txCaptor.getValue();
        assertEquals("RESERVE", tx.getType());
        assertEquals(5, tx.getQuantity());
        assertEquals(100L, tx.getReferenceId());
    }

    @Test
    void testReserveStockSplitWarehouses() {
        Warehouse wh1 = Warehouse.builder().id(1L).code("WH-1").build();
        WarehouseStock stock1 = WarehouseStock.builder()
                .warehouse(wh1)
                .physicalQty(5)
                .reservedQty(2) // 3 available
                .build();

        Warehouse wh2 = Warehouse.builder().id(2L).code("WH-2").build();
        WarehouseStock stock2 = WarehouseStock.builder()
                .warehouse(wh2)
                .physicalQty(10)
                .reservedQty(5) // 5 available
                .build();

        when(warehouseStockRepository.findByProductVariantIdForUpdate(1L))
                .thenReturn(Arrays.asList(stock1, stock2));

        inventoryService.reserveStock(100L, 1L, 6);

        assertEquals(5, stock1.getReservedQty()); // +3 (max available)
        assertEquals(8, stock2.getReservedQty()); // +3 (remaining)

        verify(warehouseStockRepository, times(1)).save(stock1);
        verify(warehouseStockRepository, times(1)).save(stock2);
        verify(inventoryTransactionRepository, times(2)).save(any(InventoryTransaction.class));
    }
}
