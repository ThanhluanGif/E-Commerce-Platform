package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Order;
import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.repository.OrderRepository;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * AI Product Copywriter: Tạo mô tả sản phẩm hấp dẫn dựa trên từ khóa, tên sản phẩm và danh mục
     */
    public String generateProductDescription(String productName, String category, List<String> keywords) {
        if (aiApiKey != null && !aiApiKey.isEmpty()) {
            try {
                // Giả lập gọi API LLM thực tế (ví dụ: Gemini API hoặc OpenAI)
                // String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + aiApiKey;
                // restTemplate.postForObject(...)
            } catch (Exception e) {
                System.err.println("Lỗi gọi LLM API: " + e.getMessage());
            }
        }

        // Fallback Logic tạo mô tả thông minh
        String keywordStr = String.join(", ", keywords);
        return String.format(
            "### 🌟 Giới thiệu sản phẩm: %s\n\n" +
            "Chào mừng bạn đến với dòng sản phẩm **%s** thế hệ mới! Đây là sự lựa chọn hoàn hảo được thiết kế đặc biệt nhằm đáp ứng các tiêu chuẩn cao nhất về chất lượng và độ bền.\n\n" +
            "#### 💡 Điểm nổi bật vượt trội:\n" +
            "- **Thiết kế tối ưu**: Mang lại phong cách tinh tế, hiện đại, bắt kịp xu hướng thị trường.\n" +
            "- **Chất liệu cao cấp**: Đảm bảo sự an toàn, thoải mái và bền bỉ trong suốt quá trình sử dụng.\n" +
            "- **Tính năng đột phá**: Hỗ trợ tối đa nhu cầu của bạn nhờ công nghệ tiên tiến.\n\n" +
            "#### 🔑 Từ khóa nổi bật:\n" +
            "*%s*\n\n" +
            "Hãy đặt mua ngay **%s** hôm nay tại E-Shop để nhận được các ưu đãi giảm giá tốt nhất cùng chính sách miễn phí vận chuyển trên toàn quốc! Hàng chính hãng 100%%, cam kết bảo hành và đổi trả dễ dàng trong vòng 7 ngày.",
            productName, category, keywordStr, productName
        );
    }

    /**
     * Visual Search (Tìm kiếm bằng hình ảnh):
     * Nhận diện đặc trưng ảnh (giả lập vector matching) để tìm sản phẩm tương tự từ DB.
     */
    public List<Product> searchByImage(String imageUrl) {
        // Thực tế: Ảnh được upload lên, một model CNN/ViT (như ResNet, CLIP) trích xuất vector embedding
        // Sau đó thực hiện tìm kiếm vector tương đồng (vector search) trong DB (như pgvector).
        // Giải pháp giả lập thông minh: Phân tích đường dẫn ảnh/tên file ảnh để tìm danh mục hoặc từ khóa tương thích
        String lowerUrl = imageUrl.toLowerCase();
        List<Product> allProducts = productRepository.findAll();

        if (lowerUrl.contains("shirt") || lowerUrl.contains("ao") || lowerUrl.contains("clothing")) {
            return allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("áo") || p.getName().toLowerCase().contains("thời trang"))
                    .limit(8)
                    .collect(Collectors.toList());
        } else if (lowerUrl.contains("shoe") || lowerUrl.contains("giay")) {
            return allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("giày") || p.getName().toLowerCase().contains("thể thao"))
                    .limit(8)
                    .collect(Collectors.toList());
        } else if (lowerUrl.contains("phone") || lowerUrl.contains("laptop") || lowerUrl.contains("computer") || lowerUrl.contains("electronic")) {
            return allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("điện thoại") || p.getName().toLowerCase().contains("laptop") || p.getName().toLowerCase().contains("tai nghe"))
                    .limit(8)
                    .collect(Collectors.toList());
        }

        // Mặc định trả về 6 sản phẩm bán chạy/ngẫu nhiên
        Collections.shuffle(allProducts);
        return allProducts.stream().limit(6).collect(Collectors.toList());
    }

    /**
     * Chatbot Hỗ trợ Khách hàng AI:
     * Trả lời tự động các câu hỏi về chính sách, tra cứu đơn hàng hoặc hỗ trợ trực tuyến.
     */
    public Map<String, Object> processChatbotQuery(String message, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        String responseText;
        boolean requestHuman = false;

        String query = message.toLowerCase().trim();

        // 1. Tra cứu đơn hàng tự động
        if (query.contains("đơn hàng") || query.contains("tra cứu") || query.contains("ord-")) {
            Optional<Order> orderOpt = Optional.empty();
            // Trích xuất mã đơn hàng dạng ORD-YYYYMMDD-XXXX
            int ordIndex = query.indexOf("ord-");
            if (ordIndex != -1 && query.length() >= ordIndex + 17) {
                String potentialCode = message.substring(ordIndex, ordIndex + 17).toUpperCase();
                orderOpt = orderRepository.findByOrderCode(potentialCode);
            }

            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                responseText = String.format(
                    "🤖 **Thông tin Đơn hàng của bạn:**\n" +
                    "- **Mã đơn hàng**: %s\n" +
                    "- **Ngày đặt**: %s\n" +
                    "- **Tổng tiền**: %,.2f đ\n" +
                    "- **Trạng thái**: **%s**\n" +
                    "- **Phương thức thanh toán**: %s\n\n" +
                    "Bạn có cần hỗ trợ gì thêm về đơn hàng này không?",
                    order.getOrderCode(),
                    order.getCreatedAt().toString().substring(0, 10),
                    order.getTotalPrice(),
                    order.getStatus().name(),
                    order.getPaymentMethod()
                );
            } else {
                // Lấy đơn hàng gần nhất của user
                List<Order> myOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
                if (!myOrders.isEmpty()) {
                    Order latest = myOrders.get(0);
                    responseText = String.format(
                        "🤖 Tôi thấy bạn có đơn hàng mới nhất là:\n" +
                        "- **Mã**: `%s` đặt ngày %s\n" +
                        "- **Trạng thái**: **%s**\n\n" +
                        "Để tra cứu chi tiết đơn hàng khác, vui lòng nhập chính xác mã đơn hàng (Ví dụ: `ORD-20260706-ABCD`).",
                        latest.getOrderCode(),
                        latest.getCreatedAt().toString().substring(0, 10),
                        latest.getStatus().name()
                    );
                } else {
                    responseText = "🤖 Bạn chưa có đơn hàng nào tại hệ thống của chúng tôi để tra cứu.";
                }
            }
        }
        // 2. Trả lời chính sách đổi trả
        else if (query.contains("đổi trả") || query.contains("hoàn tiền") || query.contains("trả hàng")) {
            responseText = "🤖 **Chính sách đổi trả hàng:**\n" +
                    "1. E-Shop hỗ trợ đổi trả hàng trong vòng **7 ngày** kể từ khi nhận hàng thành công.\n" +
                    "2. Sản phẩm đổi trả phải còn nguyên tem mác, chưa qua sử dụng và ở trạng thái ban đầu.\n" +
                    "3. Đối với sản phẩm lỗi do nhà sản xuất, phí vận chuyển đổi trả sẽ do E-Shop chi trả hoàn toàn.\n" +
                    "Để gửi yêu cầu, bạn vui lòng truy cập trang **Lịch sử đơn hàng** -> chọn đơn hàng cần trả -> bấm **Yêu cầu trả hàng**.";
        }
        // 3. Trả lời chính sách vận chuyển / giao hàng
        else if (query.contains("giao hàng") || query.contains("vận chuyển") || query.contains("ship") || query.contains("bao lâu")) {
            responseText = "🤖 **Chính sách giao hàng tại E-Shop:**\n" +
                    "- **Nội thành (Hà Nội, TP.HCM)**: Giao hàng nhanh từ 1 - 2 ngày.\n" +
                    "- **Các tỉnh thành khác**: Từ 3 - 5 ngày làm việc.\n" +
                    "- **Phí vận chuyển**: Đồng giá 30.000đ toàn quốc. Miễn phí vận chuyển cho đơn hàng trên 500.000đ!\n" +
                    "Hệ thống sẽ tự động chọn Kho hàng tối ưu gần bạn nhất để xử lý đơn hàng nhanh chóng.";
        }
        // 4. Kết nối nhân viên hỗ trợ
        else if (query.contains("nhân viên") || query.contains("gặp người") || query.contains("admin") || query.contains("hỗ trợ viên")) {
            responseText = "🤖 Dạ, tôi đang kết nối bạn với Nhân viên hỗ trợ trực tuyến. Vui lòng chờ trong giây lát! Hỗ trợ viên sẽ phản hồi bạn ngay tại khung chat này.";
            requestHuman = true;
        }
        // 5. Mặc định chat thông minh
        else {
            responseText = "🤖 Chào bạn! Tôi là Trợ lý Ảo E-Shop AI. Tôi có thể giúp bạn:\n" +
                    "- Tra cứu trạng thái đơn hàng (Nhập mã đơn hoặc gõ 'tra cứu đơn hàng').\n" +
                    "- Hướng dẫn đổi trả hàng, chính sách giao hàng.\n" +
                    "- Kết nối trực tiếp với nhân viên hỗ trợ (Gõ 'gặp nhân viên').\n\n" +
                    "Hôm nay bạn cần hỗ trợ thông tin gì ạ?";
        }

        result.put("response", responseText);
        result.put("requestHuman", requestHuman);
        return result;
    }
}
