# E-Commerce Platform (Hệ thống bán hàng doanh nghiệp)

Chào mừng bạn đến với dự án **E-Commerce Platform**, một hệ thống thương mại điện tử cấp doanh nghiệp được thiết kế với hiệu năng cao, khả năng chịu tải tốt và độ tin cậy tối ưu.

## 🔗 Liên kết Dự án
*   **GitHub Repository**: [https://github.com/ThanhluanGif/E-Commerce-Platform.git](https://github.com/ThanhluanGif/E-Commerce-Platform.git)
*   **Tài liệu Thiết kế Kiến trúc Hệ thống**: [E_COMMERCE_SYSTEM_DESIGN.md](E_COMMERCE_SYSTEM_DESIGN.md)

---

## 🎯 Chỉ số Thiết kế & Mục tiêu (SLAs & Targets)
Hệ thống được thiết kế để giải quyết bài toán tải cao (High Traffic) và lượng dữ liệu lớn (Data Volumetry):
*   **Throughput**: Hỗ trợ tối thiểu 2,000 TPS cho luồng checkout và 10,000 req/s cho các luồng tra cứu sản phẩm.
*   **Latency**: Response time (p95) < 80ms cho luồng đọc (Multi-level Cache) và p99 < 300ms cho luồng ghi.
*   **Data Integrity**: 0% sai lệch tồn kho (Over-selling), 100% giao dịch lỗi có cơ chế bù tự động (Saga Rollback).
*   **High Availability**: Thiết kế dự phòng cao, tự động khôi phục lỗi.

---

## 🏗️ Kiến trúc & Công nghệ chính
*   **Backend**: Java/Spring Boot hoặc Microservices (Chi tiết trong tài liệu thiết kế)
*   **Cơ sở dữ liệu**: MySQL/PostgreSQL kết hợp Redis Cache
*   **Message Broker**: Kafka / RabbitMQ phục vụ xử lý bất đồng bộ
*   **Hạ tầng**: Docker, Kubernetes, AWS/GCP cloud services

---

## 📂 Tài liệu Thiết kế chi tiết
Để xem chi tiết sơ đồ thiết kế hệ thống, phân rã công việc (WBS), thiết kế cơ sở dữ liệu và các luồng xử lý (Checkout, Payment, Inventory...), vui lòng đọc file tài liệu:
👉 **[Tài liệu Thiết kế Kiến trúc chi tiết (E_COMMERCE_SYSTEM_DESIGN.md)](E_COMMERCE_SYSTEM_DESIGN.md)**

---

## 🛠️ Hướng dẫn phát triển
1. **Clone repository**:
   ```bash
   git clone https://github.com/ThanhluanGif/E-Commerce-Platform.git
   ```
2. **Chọn nhánh làm việc**:
   * Nhánh chính: `main`
   * Nhánh phát triển của Luân: `Luân`
   * Nhánh phát triển của Hưng: `Hưng`
