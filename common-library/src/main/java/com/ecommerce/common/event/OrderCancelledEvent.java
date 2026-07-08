package com.ecommerce.common.event;

import lombok.*;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent implements Serializable {
    private Long orderId;
    private String orderCode;
    private Long couponId;
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item implements Serializable {
        private Long productVariantId;
        private Integer quantity;
    }
}
