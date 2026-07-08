package com.ecommerce.product.listener;

import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.product.client.OrderClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderCancelledInventoryListenerTest {

    @Mock
    private OrderClient orderClient;

    @InjectMocks
    private OrderCancelledInventoryListener listener;

    @Test
    public void testHandleOrderCancelled_Success() {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(1001L)
                .orderCode("ORD-20260708-999")
                .items(List.of(OrderCancelledEvent.Item.builder()
                        .productVariantId(3001L)
                        .quantity(2)
                        .build()))
                .build();

        listener.handleOrderCancelled(event);

        verify(orderClient, times(1)).releaseStock(event);
    }
}
