package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.OrderRequest;
import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.OrderItemRepository;
import com.ecommerce.ecommerceapi.repository.OrderRepository;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import com.ecommerce.ecommerceapi.repository.ProductVariantRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CartService cartService;

    public Order createOrder(Integer userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        List<CartItem> cartItems = cartService.getCartForUser(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Giỏ hàng của bạn đang trống. Không thể đặt hàng!");
        }

        // Validate stock for all items
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .orderCode(generateOrderCode())
                .totalPrice(BigDecimal.ZERO) // Temporary, will sum up below
                .build();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            ProductVariant variant = cartItem.getVariant();
            
            if (variant != null) {
                if (variant.getStockQuantity() < cartItem.getQuantity()) {
                    throw new BadRequestException("Biến thể '" + variant.getName() + "' của sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho (Còn lại: " + variant.getStockQuantity() + ")!");
                }
                
                // Deduct variant stock
                variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
                productVariantRepository.save(variant);
            } else {
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new BadRequestException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho (Còn lại: " + product.getStockQuantity() + ")!");
                }
                
                // Deduct product stock
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            }

            BigDecimal priceAtPurchase = product.getSalePrice() != null && product.getSalePrice().compareTo(BigDecimal.ZERO) > 0
                    ? product.getSalePrice()
                    : product.getPrice();
                    
            if (variant != null) {
                BigDecimal vPrice = variant.getPrice() != null ? variant.getPrice() : product.getPrice();
                BigDecimal vSalePrice = variant.getSalePrice() != null ? variant.getSalePrice() : variant.getPrice();
                priceAtPurchase = vSalePrice != null && vSalePrice.compareTo(BigDecimal.ZERO) > 0 ? vSalePrice : vPrice;
            }

            BigDecimal itemTotal = priceAtPurchase.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .variant(variant)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(priceAtPurchase)
                    .build();

            orderItems.add(orderItem);
        }

        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        // Save order (will cascade save order items because of CascadeType.ALL)
        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(userId);

        return savedOrder;
    }

    public List<Order> getOrdersForUser(Integer userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderById(Integer orderId, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        // Check ownership (admins can see any order)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        if (!order.getUser().getId().equals(userId) && !user.getRole().name().equals("Admin")) {
            throw new BadRequestException("Bạn không có quyền truy cập đơn hàng này!");
        }

        return order;
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Order updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Không thể chuyển đổi trạng thái cho đơn hàng đã hủy hoặc đã giao thành công!");
        }

        // Return stock if cancelled
        if (status == OrderStatus.CANCELLED) {
            returnStockForOrder(order);
        }

        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order cancelOrder(Integer orderId, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền hủy đơn hàng này!");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể hủy đơn hàng ở trạng thái CHỜ XỬ LÝ (PENDING)!");
        }

        returnStockForOrder(order);
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public Order payOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Đơn hàng này không ở trạng thái chờ thanh toán!");
        }
        order.setStatus(OrderStatus.SHIPPING);
        return orderRepository.save(order);
    }

    private void returnStockForOrder(Order order) {
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    private String generateOrderCode() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = LocalDate.now().format(formatter);
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "ORD-" + dateStr + "-" + randomSuffix;
    }
}
