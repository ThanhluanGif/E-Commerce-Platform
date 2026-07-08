package com.ecommerce.order.listener;

import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.order.entity.Coupon;
import com.ecommerce.order.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledCouponListener {

    private final CouponRepository couponRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order.cancelled.coupon.queue", durable = "true"),
            exchange = @Exchange(value = "order.exchange", type = "topic"),
            key = "order.cancelled"
    ))
    @Transactional

    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order.cancelled event for coupon release. Order Code: {}, Coupon ID: {}", 
                event.getOrderCode(), event.getCouponId());

        if (event.getCouponId() == null) {
            log.info("No coupon was applied to Order Code: {}. Skipping.", event.getOrderCode());
            return;
        }

        couponRepository.findById(event.getCouponId()).ifPresentOrElse(
                coupon -> {
                    int oldUsedCount = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;
                    int newUsedCount = Math.max(0, oldUsedCount - 1);
                    coupon.setUsedCount(newUsedCount);
                    couponRepository.save(coupon);
                    log.info("Successfully released coupon code '{}'. used_count updated: {} -> {}", 
                            coupon.getCode(), oldUsedCount, newUsedCount);
                },
                () -> log.warn("Coupon with ID {} not found in database. Cannot release usage.", event.getCouponId())
        );
    }
}
