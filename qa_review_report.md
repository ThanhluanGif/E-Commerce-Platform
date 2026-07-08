# 📋 Báo Cáo Phân Tích Chất Lượng & Vá Lỗi Hệ Thống (QA Analysis Report)

> **Vị trí:** Senior QA Engineer (10+ năm kinh nghiệm) & Principal Software Engineer  
> **Dự án:** E-Commerce Platform (Kiến trúc Microservices)  
> **Phạm vi kiểm thử:** Phân tích mã nguồn và thiết kế hệ thống của **11 Task lớn** đã hoàn thành (bao gồm `gateway-service`, `auth-service`, `product-service` và thư viện dùng chung `common-library`).

Hệ thống đã xây dựng được một bộ khung Microservices tương đối hoàn chỉnh về mặt kỹ thuật (Gateway, Rate Limiter, Auth JWT, Redis Session, Cây danh mục đệ quy, Product CRUD, Redis Cache-Aside và Elasticsearch). Chúng tôi đã tiến hành kiểm thử tĩnh (Static Testing), phân tích luồng code (Data Flow & Logic Analysis) và **tích hợp thành công các bản vá lỗi ở mức production** cho toàn bộ **13 lỗi phát hiện** liên quan đến bảo mật, race condition, đồng bộ dữ liệu và ràng buộc CSDL.

---

## 📊 Bảng Tổng Hợp Lỗi & Trạng Thái Vá Lỗi (Hotfix Status)

| ID | Tên Lỗi / Vấn Đề | Phân Loại | Mức Độ | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| **BUG-01** | Lỗi hết hạn Access Token chặn đứng luồng Đăng xuất (Logout Flow Blocked) | Logic / UX | 🔴 **Critical** | ✅ **DONE** (Resolved) |
| **BUG-02** | Xung đột ràng buộc Unique khi tạo mới dữ liệu trùng SKU/Slug đã bị Soft-Delete | Database Schema | 🟡 **High** | ✅ **DONE** (Resolved) |
| **BUG-03** | Race Condition gây nhiễm độc cache (Stale Cache) khi cập nhật sản phẩm concurrent | Race Condition | 🟡 **High** | ✅ **DONE** (Resolved) |
| **BUG-04** | Bất nhất dữ liệu giữa Elasticsearch và PostgreSQL khi Database Transaction bị Rollback | Transaction / Rollback | 🟡 **High** | ✅ **DONE** (Resolved) |
| **BUG-05** | Bỏ qua Validation dữ liệu của Variant và Attribute do thiếu `@Valid` ở Request DTO | Input Validation | 🟡 **High** | ✅ **DONE** (Resolved) |
| **BUG-06** | Concurrency Race Condition tạo dữ liệu trùng lặp trong Attribute Values gây sập API | Race Condition / Duplicate | 🟡 **High** | ✅ **DONE** (Resolved) |
| **BUG-07** | Bỏ lọt lỗ hổng Phân quyền sửa/xóa sản phẩm tại Product Service | Security / Auth | 🟡 **High** | ✅ **DONE** (Resolved) |
| **BUG-08** | Lỗi Header Spoofing (Giả mạo định danh) qua các endpoint Public trên Gateway | Security | 🔴 **Critical** | ✅ **DONE** (Resolved) |
| **BUG-09** | Rate Limiting hoạt động sai lệch khi hệ thống chạy sau Reverse Proxy/Load Balancer | Networking | ⚪ **Medium** | ✅ **DONE** (Resolved) |
| **BUG-10** | Lọc sản phẩm theo danh mục bỏ qua việc quét đệ quy các danh mục con (Subcategories) | Business Logic | ⚪ **Medium** | ✅ **DONE** (Resolved) |
| **BUG-11** | Nguy cơ tràn bộ nhớ JVM (OutOfMemoryError) khi khởi tạo chỉ mục Elasticsearch | Scalability | ⚪ **Medium** | ✅ **DONE** (Resolved) |
| **BUG-12** | Tham số phân trang âm (`page < 0`, `size < 1`) gây lỗi 500 thay vì trả về 400 | Input Validation | 🟢 **Low** | ✅ **DONE** (Resolved) |
| **BUG-13** | Concurrency Race Condition khi xoay vòng Refresh Token (Token Rotation) | Race Condition | ⚪ **Medium** | ✅ **DONE** (Resolved) |
| **BUG-14** | Concurrency Race Condition (Lost Update) khi đặt trước tồn kho Warehouse Stock | Race Condition | 🔴 **Critical** | 🚨 **OPEN** (Pending) |
| **BUG-15** | Lỗ hổng hoàn tiền nhiều lần do thiếu kiểm tra trùng lặp yêu cầu đổi trả (RMA) | Financial Logic | 🔴 **Critical** | 🚨 **OPEN** (Pending) |
| **BUG-16** | Concurrency Race Condition (Lost Update) khi cập nhật số lần sử dụng Coupon | Race Condition | ⚪ **Medium** | 🚨 **OPEN** (Pending) |
| **BUG-17** | Lỗi NonUniqueResultException khi tìm kiếm Order qua orderCode tại phân vùng khác nhau | DB Partitioning | 🟡 **High** | 🚨 **OPEN** (Pending) |
| **BUG-18** | Bỏ qua validation dữ liệu lồng nhau do thiếu `@Valid` trong ReturnRequest và QC DTO | Input Validation | 🟡 **High** | 🚨 **OPEN** (Pending) |
| **BUG-19** | Luồng đổi trả RMA bị đóng băng do thiếu cơ chế chuyển đơn hàng sang COMPLETED | Business Flow | 🟡 **High** | 🚨 **OPEN** (Pending) |
| **BUG-20** | Luồng giải phóng kho thiết kế kém hiệu quả và rủi ro lỗi mạng (Circular Call) | Architecture | ⚪ **Medium** | 🚨 **OPEN** (Pending) |
| **BUG-21** | Bất nhất trạng thái giỏ hàng khi checkout thất bại (Redis Cart bị xóa trước khi DB commit) | Transaction Integrity | 🟡 **High** | 🚨 **OPEN** (Pending) |
| **BUG-22** | Nguy cơ trùng lặp ID đơn hàng dưới tải cao (ID Collision) | Data Integrity | ⚪ **Medium** | 🚨 **OPEN** (Pending) |
| **BUG-23** | Lỗ hổng bảo mật do phơi bày cổng trực tiếp và bỏ qua xác thực token của order-service | Security | 🔴 **Critical** | 🚨 **OPEN** (Pending) |

---

## 🔍 Chi Tiết Các Bug, Kịch Bản Tái Hiện & Bản Vá Production (Hotfix Review)

---

### BUG-01: Lỗi hết hạn Access Token chặn đứng luồng Đăng xuất (Logout Flow Blocked)
*   **Mô tả:** 
    Trong `AuthService.logout`, việc validate access token trước khi đăng xuất ném exception nếu token đã hết hạn, chặn việc Controller chạy lệnh xóa cookie `refresh_token` ở client.
*   **Điều kiện xảy ra:** Access Token hết hạn và người dùng bấm nút "Đăng xuất" (Logout).
*   **Mức độ nghiêm trọng:** 🔴 **Critical** (Chức năng cốt lõi bị lỗi và ảnh hưởng xấu đến UX/Bảo mật).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Hàm `validateToken` ném exception `ExpiredJwtException` khi token hết hạn, làm nghẽn luồng xử lý trước khi lệnh xóa cookie ở Controller được thực thi.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Cấu hình `AuthService.logout` để bắt riêng `ExpiredJwtException`. Nếu token hết hạn, ta vẫn lấy được Claims từ exception để định danh `userId` và thực hiện xóa Refresh Token trên Redis. Các lỗi giả mạo token khác vẫn bị chặn và ném `401 Unauthorized`. Luồng Controller luôn được đảm bảo chạy lệnh xóa cookie `refresh_token` thông qua khối `finally`.
*   **Đánh giá an toàn (Safety):** Hoàn toàn an toàn do token hết hạn không thể tái sử dụng để truy cập tài nguyên, việc giải mã chỉ nhằm mục đích thu hồi phiên làm việc trên Redis.

---

### BUG-02: Xung đột ràng buộc Unique khi tạo mới dữ liệu trùng SKU/Slug đã bị Soft-Delete
*   **Mô tả:** 
    PostgreSQL kiểm tra ràng buộc `UNIQUE` trên toàn bảng khiến việc tạo mới Slug/SKU trùng với bản ghi đã bị xóa mềm (`deleted_at IS NOT NULL`) thất bại.
*   **Mức độ nghiêm trọng:** 🟡 **High** (Lỗi CSDL làm sập tính năng nghiệp vụ của Admin).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Ràng buộc `UNIQUE` inline chuẩn trong Postgres không bỏ qua các dòng có `deleted_at IS NOT NULL`.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Tạo bản vá migration Flyway `V4__fix_soft_delete_unique_constraints.sql` thực hiện drop constraints unique inline cũ của các bảng `products`, `product_variants`, `categories`, `brands` và thay thế bằng các **Partial Unique Index** (ví dụ: `CREATE UNIQUE INDEX uq_products_slug ON catalog.products(slug) WHERE deleted_at IS NULL;`).
*   **Đánh giá an toàn (Safety):** Đảm bảo tính toàn vẹn dữ liệu cho các sản phẩm đang hoạt động, đồng thời cho phép tái sử dụng SKU/Slug của các bản ghi cũ đã xóa mà không làm mất lịch sử dữ liệu.

---

### BUG-03: Race Condition gây nhiễm độc cache (Stale Cache) khi cập nhật sản phẩm concurrent
*   **Mô tả:** 
    Spring `@CacheEvict` xóa cache trước khi DB transaction thực sự commit, luồng đọc song song đọc dữ liệu cũ từ DB rồi ghi ngược lại vào Redis gây nhiễm độc cache (Stale Cache).
*   **Mức độ nghiêm trọng:** 🟡 **High** (Dẫn tới hiển thị sai lệch thông tin sản phẩm trên UI).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Thứ tự thực hiện xóa cache của Spring Cache Interceptor diễn ra trước khi DB thực hiện commit giao dịch.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Kích hoạt cấu hình `.transactionAware()` trên `RedisCacheManager` trong `RedisCacheConfig.java`. Việc này hoãn tất cả các thao tác ghi/xóa cache cho đến sau khi DB transaction được commit thành công.
*   **Đánh giá an toàn (Safety):** Loại bỏ hoàn toàn race condition nhiễm độc cache mà không làm thay đổi logic xử lý nghiệp vụ sản phẩm.

---

### BUG-04: Bất nhất dữ liệu giữa Elasticsearch và PostgreSQL khi Database Transaction bị Rollback
*   **Mô tả:** 
    Đồng bộ Elasticsearch diễn ra ngay trong transaction DB, nếu DB transaction rollback, Elasticsearch vẫn lưu sản phẩm mới gây lệch dữ liệu.
*   **Mức độ nghiêm trọng:** 🟡 **High** (Gây hỏng cấu trúc dữ liệu tìm kiếm, lỗi ứng dụng 404).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Elasticsearch không tham gia vào cơ chế transaction của cơ sở dữ liệu quan hệ nên không tự động rollback.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Sử dụng `TransactionSynchronizationManager.registerSynchronization` để đăng ký callback `afterCommit()`. Thao tác đồng bộ Elasticsearch chỉ được kích hoạt sau khi DB transaction đã commit thành công hoàn toàn.
*   **Đánh giá an toàn (Safety):** Đảm bảo tính nhất quán tuyệt đối giữa CSDL và Search Index.

---

### BUG-05: Bỏ qua Validation dữ liệu của Variant và Attribute do thiếu `@Valid` ở Request DTO
*   **Mô tả:** 
    Spring Boot không kiểm tra tính hợp lệ của các phần tử trong danh sách `variants` và `attributes` do thiếu `@Valid` ở tầng DTO.
*   **Mức độ nghiêm trọng:** 🟡 **High** (Dẫn tới ô nhiễm dữ liệu, lỗi giá bán âm).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Thiếu annotation chỉ định kiểm tra lồng nhau (nested validation) trên các List thuộc tính DTO.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Thêm annotation `@Valid` trước các danh sách `variants` trong `ProductCreateRequest` và `attributes` trong `ProductVariantCreateRequest`.
*   **Đánh giá an toàn (Safety):** Kích hoạt validate chuẩn xác ở tầng Controller, loại bỏ nguy cơ lưu dữ liệu rác vào CSDL.

---

### BUG-06: Concurrency Race Condition tạo dữ liệu trùng lặp trong Attribute Values gây sập API
*   **Mô tả:** 
    Nhiều luồng đồng thời tạo sản phẩm có chung thuộc tính mới dẫn đến tạo trùng lặp cặp `(attribute_id, value)` trong DB, gây crash `IncorrectResultSizeDataAccessException` khi query sau đó.
*   **Mức độ nghiêm trọng:** 🟡 **High** (Gây sập tính năng tạo sản phẩm do ô nhiễm dữ liệu).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Thiếu ràng buộc unique ở tầng DB trên cặp khóa thuộc tính động và thiếu cơ chế xử lý tranh chấp ở Java.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    1. Tạo chỉ mục unique trên hai cột `(attribute_id, value)` ở bảng `catalog.attribute_values` trong Flyway `V4`.
    2. Trong code Java (`ProductServiceImpl`), chuyển sang gọi `saveAndFlush()` để ném lỗi constraint vi phạm ngay lập tức, kết hợp bắt `DataIntegrityViolationException` để truy vấn lại bản ghi do luồng song song tạo trước đó.
*   **Đánh giá an toàn (Safety):** Đảm bảo an toàn đa luồng tối đa, loại bỏ hoàn toàn các lỗi crash luồng ghi khi quản trị viên tạo sản phẩm đồng thời.

---

### BUG-07: Bỏ lọt lỗ hổng Phân quyền sửa/xóa sản phẩm tại Product Service
*   **Mô tả:** 
    Downstream `product-service` thiếu Spring Security và các annotation kiểm tra vai trò nên bất kỳ user nào có JWT hợp lệ đều có thể POST/PUT/DELETE sản phẩm.
*   **Mức độ nghiêm trọng:** 🔴 **Critical** / 🟡 **High** (Lỗ hổng leo thang đặc quyền nghiêm trọng).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Downstream service tin cậy mù quáng các request từ Gateway mà không tự kiểm tra quyền của người dùng.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Xây dựng một `SecurityFilter` gọn nhẹ (Servlet Filter) trong `product-service` thực hiện kiểm tra header `X-User-Roles` được Gateway forward xuống. Đối với các phương thức thay đổi trạng thái (`POST`, `PUT`, `DELETE`), bộ lọc yêu cầu vai trò của người dùng phải chứa `ROLE_ADMIN`, ngược lại trả về `403 Forbidden` kèm cấu trúc JSON lỗi chuẩn.
*   **Đánh giá an toàn (Safety):** Đảm bảo phân quyền chặt chẽ ở cấp độ service mà không cần tích hợp framework Spring Security cồng kềnh, giảm thiểu overhead hiệu năng.

---

### BUG-08: Lỗi Header Spoofing (Giả mạo định danh) qua các endpoint Public trên Gateway
*   **Mô tả:** 
    API Gateway chuyển thẳng các request public xuống downstream mà không strip bỏ các header định danh do client tự đính kèm (như `X-User-Id`, `X-User-Roles`).
*   **Mức độ nghiêm trọng:** 🔴 **Critical** (Lỗ hổng bảo mật hạ tầng cho phép giả mạo danh tính trên diện rộng).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Thiếu bộ lọc dọn dẹp (sanitization) các header nhạy cảm tại cửa ngõ API Gateway đối với luồng bypass JWT.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Cập nhật [AuthenticationFilter.java](file:///Users/admin/IdeaProjects/E-CommercePlatform/gateway-service/src/main/java/com/ecommerce/gateway/filter/AuthenticationFilter.java) thực hiện loại bỏ tất cả các request headers bắt đầu bằng tiền tố `x-user-` (ví dụ sử dụng `removeIf(key -> key.toLowerCase().startsWith("x-user-"))`) ngay tại điểm bắt đầu của filter.
*   **Đánh giá an toàn (Safety):** Đảm bảo downstream service chỉ nhận các header định danh sạch được sinh và ký bởi chính API Gateway.

---

### BUG-09: Rate Limiting hoạt động sai lệch khi hệ thống chạy sau Reverse Proxy/Load Balancer
*   **Mô tả:** 
    `UserKeyResolver` chỉ lấy IP trực tiếp từ socket, khiến toàn bộ người dùng vãng lai đứng sau Load Balancer/Proxy bị gom chung vào một bucket và bị khóa 429 oan uổng.
*   **Mức độ nghiêm trọng:** ⚪ **Medium** / 🟡 **High** (Lỗi hạ tầng mạng gây từ chối dịch vụ cho khách hàng thật).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Không xử lý trích xuất IP client thực tế từ các header trung chuyển của proxy.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Cấu hình `UserKeyResolver` đọc địa chỉ IP client thực tế từ header `X-Forwarded-For` đầu tiên. Nếu không có header này mới fallback về địa chỉ IP socket kết nối trực tiếp.
*   **Đánh giá an toàn (Safety):** Giúp hệ thống hoạt động ổn định và chính xác trên mọi môi trường staging/production thực tế nằm sau Nginx, Cloudflare hoặc AWS ALB.

---

### BUG-10: Lọc sản phẩm theo danh mục bỏ qua việc quét đệ quy các danh mục con (Subcategories)
*   **Mô tả:** 
    Lọc danh mục chỉ so sánh bằng trực tiếp ID danh mục được truyền lên, bỏ qua tất cả sản phẩm của các danh mục con cháu.
*   **Mức độ nghiêm trọng:** ⚪ **Medium** (Lỗi nghiệp vụ hiển thị sai lệch danh mục sản phẩm).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Sử dụng phép toán so sánh bằng (`cb.equal` hoặc term query trong ES) trên một ID duy nhất thay vì quét cấu trúc cây thư mục.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    1. Viết thuật toán DFS đệ quy tại `ProductServiceImpl` để lấy ra toàn bộ ID của các danh mục con cháu từ danh mục cha được yêu cầu.
    2. Cập nhật `ProductSpecifications` (sử dụng `.in()`) và `ElasticsearchQueryBuilder` (sử dụng terms query) để tìm kiếm sản phẩm nằm trong bất kỳ ID danh mục nào thuộc danh sách trên.
*   **Đánh giá an toàn (Safety):** Hoàn toàn tương thích ngược và đáp ứng chính xác kỳ vọng trải nghiệm tìm kiếm của khách hàng.

---

### BUG-11: Nguy cơ tràn bộ nhớ JVM (OutOfMemoryError) khi khởi tạo chỉ mục Elasticsearch
*   **Mô tả:** 
    Tải toàn bộ sản phẩm bằng `findAll().stream()` lên RAM để lọc và đồng bộ sang Elasticsearch gây nguy cơ OOM crash khi số lượng sản phẩm lớn.
*   **Mức độ nghiêm trọng:** ⚪ **Medium** / 🟡 **High** (Ảnh hưởng khả năng mở rộng hệ thống).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Không áp dụng cơ chế phân trang hoặc streaming ở tầng database khi khởi tạo/bootstrap dữ liệu lớn.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Thay thế `productRepository.findAll()` bằng truy vấn phân trang cuốn chiếu `findByStatusAndDeletedAtIsNull(ProductStatus.ACTIVE, PageRequest.of(page, size))` với size = 100 trong `ElasticsearchIndexInitializer.java`.
*   **Đánh giá an toàn (Safety):** Đảm bảo heap memory của JVM luôn phẳng và ổn định bất kể dung lượng database tăng trưởng bao nhiêu.

---

### BUG-12: Tham số phân trang âm (`page < 0`, `size < 1`) gây lỗi 500 thay vì trả về 400
*   **Mô tả:** 
    Tham số phân trang âm khiến `PageRequest` ném `IllegalArgumentException` không được bắt đúng loại, dẫn đến lỗi 500 Internal Server Error lộ stacktrace.
*   **Mức độ nghiêm trọng:** 🟢 **Low** (Ảnh hưởng đến sự chuẩn hóa API và độ tin cậy bảo mật thông tin log).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Thiếu Exception Handler cụ thể cho `IllegalArgumentException` trong bộ điều khiển ngoại lệ tập trung.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Bổ sung phương thức `@ExceptionHandler(IllegalArgumentException.class)` trong `GlobalExceptionHandler.java` để chuyển đổi và phản hồi mã lỗi `400 Bad Request` sạch sẽ, đồng thời ẩn stacktrace thô khỏi client.
*   **Đánh giá an toàn (Safety):** Giúp chuẩn hóa chuẩn API RESTful và nâng cao độ an toàn thông tin log.

---

### BUG-13: Concurrency Race Condition khi xoay vòng Refresh Token (Token Rotation)
*   **Mô tả:** 
    Axios Frontend gửi đồng thời nhiều request refresh song song khi token hết hạn làm lệch Refresh Token trên Redis, dẫn tới logout người dùng oan uổng.
*   **Mức độ nghiêm trọng:** ⚪ **Medium** (Gây trải nghiệm logout giả lập đột ngột cực kỳ khó chịu cho người dùng).
*   **Trạng thái:** ✅ **DONE** (Đã vá lỗi thành công)
*   **Nguyên nhân gốc (Root Cause):** Việc rotation thu hồi token cũ ngay lập tức làm các luồng song song chạy chậm hơn vài phần trăm giây bị từ chối truy cập.
*   **Giải pháp vá lỗi (Production Hotfix):** 
    Thiết lập cơ chế **Grace Period (Khoảng thời gian ân hạn)** trong 15 giây. Khi token được xoay vòng, token cũ được tạm giữ trên Redis dưới key `old_refresh_token` trong 15 giây. Nếu một request concurrent gửi token cũ này lên, server sẽ xác thực và trả về cặp token mới đã sinh trước đó mà không thực hiện xoay vòng tiếp hay báo lỗi.
*   **Đánh giá an toàn (Safety):** Khắc phục triệt để lỗi trải nghiệm người dùng trên môi trường mạng thực tế mà không hạ thấp tính bảo mật của rotation ngoài khoảng thời gian 15 giây.

---

---

### BUG-14: Concurrency Race Condition (Lost Update) khi đặt trước tồn kho Warehouse Stock
*   **Mô tả:** 
    Trong [InventoryServiceImpl.java](file:///Users/admin/IdeaProjects/E-CommercePlatform/order-service/src/main/java/com/ecommerce/order/service/impl/InventoryServiceImpl.java#L42-L107) (hàm `reserveStock`), hệ thống thực hiện đọc và cập nhật `reserved_qty` cho các bản ghi trong `WarehouseStock`. Tuy nhiên, `WarehouseStock` không có trường `@Version` (Optimistic Lock) và repository `WarehouseStockRepository` không sử dụng pessimistic locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`). Do các khoá Redisson phân tán trong `CheckoutServiceImpl` chỉ khoá theo `userId` (`lock:checkout:{userId}`), các khách hàng khác nhau thực hiện đặt hàng đồng thời cho cùng một sản phẩm biến thể (variant) sẽ không bị chặn bởi Redisson lock. Hậu quả là hai giao dịch đồng thời cùng đọc một lượng `reserved_qty` cũ, tính toán cộng thêm số lượng mua, rồi cùng gọi `save` đè lên nhau, dẫn đến hiện tượng bán quá số lượng thực tế (Overselling) mà DB không báo lỗi.
*   **Điều kiện xảy ra:** Nhiều khách hàng cùng đặt mua một sản phẩm biến thể có số lượng tồn kho giới hạn tại cùng một thời điểm.
*   **Các bước tái hiện:**
    1. Khởi tạo tồn kho cho variant ID `9001` tại warehouse `1` có: `physicalQty = 10`, `reservedQty = 0`.
    2. Gửi đồng thời 2 request checkout từ hai tài khoản user khác nhau (`userA` và `userB`), mỗi user đặt mua `5` sản phẩm variant ID `9001`.
    3. Dùng công cụ bắn tải đồng thời cả 2 request này trong vòng vài mili giây.
*   **Kết quả mong đợi:** Hệ thống xử lý tuần tự hoặc chặn lỗi. Sau khi hoàn thành, `reservedQty` của variant `9001` phải là `10` và lượng tồn khả dụng là `0`.
*   **Kết quả thực tế:** Cả hai giao dịch đều ghi đè lẫn nhau, kết quả cuối cùng `reservedQty` chỉ bằng `5`. Tồn kho bị thất thoát khống, cho phép người dùng thứ ba tiếp tục checkout mặc dù thực tế kho đã hết hàng khả dụng.
*   **Mức độ nghiêm trọng:** 🔴 **Critical**

---

### BUG-15: Lỗ hổng hoàn tiền nhiều lần do thiếu kiểm tra trùng lặp yêu cầu đổi trả (RMA)
*   **Mô tả:** 
    Trong [RmaServiceImpl.java](file:///Users/admin/IdeaProjects/E-CommercePlatform/order-service/src/main/java/com/ecommerce/order/service/impl/RmaServiceImpl.java#L42-L117) (hàm `submitReturn`), hệ thống hoàn toàn không kiểm tra xem đơn hàng hay các phần tử đơn hàng đã có yêu cầu đổi trả (ReturnRequest) nào được tạo trước đó chưa. Do bảng `return_requests` không có ràng buộc unique trên `order_id`, một khách hàng có thể gửi liên tục nhiều yêu cầu đổi trả cho cùng một đơn hàng. Khi nhân viên QC phê duyệt và xác nhận QC thành công (`processQc`), hệ thống sẽ gọi hoàn tiền nhiều lần (`refundPayment`) cho cùng một giao dịch, gây thất thoát tài chính lớn cho doanh nghiệp.
*   **Điều kiện xảy ra:** Khách hàng cố tình gửi nhiều yêu cầu đổi trả cho một đơn hàng đã hoàn tất.
*   **Các bước tái hiện:**
    1. Chuẩn bị đơn hàng hoàn tất (`status = COMPLETED`) đã thanh toán qua VNPay.
    2. Gửi liên tiếp 3 request `POST /api/v1/returns` cho cùng một `orderId`.
    3. Hệ thống tạo ra 3 bản ghi `ReturnRequest` khác nhau ở trạng thái `PENDING`.
    4. Admin duyệt (`approve`) cả 3 yêu cầu và Staff xác nhận QC đạt (`qcPassed = true`).
*   **Kết quả mong đợi:** Hệ thống phải chặn ngay từ bước tạo yêu cầu đổi trả đầu tiên nếu phát hiện đơn hàng đã có yêu cầu đổi trả, hoặc cộng dồn số lượng đổi trả và kiểm tra không vượt quá số lượng đã mua.
*   **Kết quả thực tế:** Hệ thống tạo ra 3 yêu cầu đổi trả độc lập, phê duyệt cả 3 và hoàn lại 3 lần số tiền thanh toán của đơn hàng gốc.
*   **Mức độ nghiêm trọng:** 🔴 **Critical**

---

### BUG-16: Concurrency Race Condition (Lost Update) khi cập nhật số lần sử dụng Coupon
*   **Mô tả:** 
    Trong [OrderCancelledCouponListener.java](file:///Users/admin/IdeaProjects/E-CommercePlatform/order-service/src/main/java/com/ecommerce/order/listener/OrderCancelledCouponListener.java#L29-L49) (hàm `handleOrderCancelled`), khi giải phóng mã giảm giá khi đơn hàng bị huỷ, hệ thống thực hiện trừ lượt sử dụng: `coupon.setUsedCount(usedCount - 1)`. Tương tự thực thể `WarehouseStock`, thực thể [Coupon.java](file:///Users/admin/IdeaProjects/E-CommercePlatform/order-service/src/main/java/com/ecommerce/order/entity/Coupon.java) không được trang bị `@Version` hay cơ chế khóa bi quan nào. Khi có nhiều đơn hàng áp dụng cùng một coupon bị huỷ đồng thời, việc ghi đè trạng thái `usedCount` song song sẽ dẫn đến lỗi Lost Update, khiến số lượt coupon đã dùng không chính xác.
*   **Điều kiện xảy ra:** Nhiều đơn hàng áp dụng chung một mã giảm giá bị huỷ cùng một lúc.
*   **Các bước tái hiện:**
    1. Seed dữ liệu coupon ID `1` có `usedCount = 10`.
    2. Gửi đồng thời 2 tin nhắn sự kiện `order.cancelled` của hai đơn hàng khác nhau chứa `couponId = 1` vào RabbitMQ.
*   **Kết quả mong đợi:** Số lượt dùng `usedCount` giảm xuống còn `8`.
*   **Kết quả thực tế:** `usedCount` giảm xuống còn `9` do ghi đè song song.
*   **Mức độ nghiêm trọng:** ⚪ **Medium**

---

### BUG-17: Lỗi NonUniqueResultException khi tìm kiếm Order qua orderCode tại phân vùng khác nhau
*   **Mô tả:** 
    Bảng `orders` được phân vùng (partitioned) theo cột `created_at`. Vì vậy, ràng buộc duy nhất duy nhất chỉ có thể tạo dưới dạng khóa tổ hợp bao gồm cột phân vùng: `uq_orders_code ON orders(order_code, created_at)`. Ràng buộc này không đảm bảo `order_code` là duy nhất trên toàn bộ các phân vùng. Nếu có hai đơn hàng ở hai phân vùng thời gian khác nhau trùng `order_code`, phương thức `orderRepository.findByOrderCode(orderCode)` trả về `Optional<Order>` sẽ bị ném lỗi `NonUniqueResultException` (hoặc `IncorrectResultSizeDataAccessException`) và gây sập luồng callback IPN của cổng thanh toán.
*   **Điều kiện xảy ra:** Hai đơn hàng nằm ở hai phân vùng thời gian khác nhau trùng mã `order_code` (ví dụ do đồng hồ hệ thống chạy lùi hoặc trùng mã ngẫu nhiên).
*   **Các bước tái hiện:**
    1. Chèn thủ công hoặc giả lập hai đơn hàng ở hai phân vùng khác nhau có cùng `order_code = ORD-20260708-111111`.
    2. Gọi endpoint nhận callback IPN thanh toán `POST /api/v1/payments/vnpay-ipn` với `vnp_TxnRef = ORD-20260708-111111`.
*   **Kết quả mong đợi:** Tìm kiếm chính xác đơn hàng cần cập nhật (bằng cách kết hợp thêm thông tin ngày tạo trong mã đơn hàng) hoặc đảm bảo mã đơn hàng là duy nhất toàn cục.
*   **Kết quả thực tế:** Ném lỗi `500 Internal Server Error` do Spring JPA tìm thấy 2 bản ghi cho truy vấn đơn.
*   **Mức độ nghiêm trọng:** 🟡 **High**

---

### BUG-18: Bỏ qua validation dữ liệu lồng nhau do thiếu `@Valid` trong ReturnRequest và QC DTO
*   **Mô tả:** 
    Trong [ReturnRequestDto.java](file:///Users/admin/IdeaProjects/E-CommercePlatform/order-service/src/main/java/com/ecommerce/order/dto/ReturnRequestDto.java#L24) và [QcRequestDto.java](file:///Users/admin/IdeaProjects/E-CommercePlatform/order-service/src/main/java/com/ecommerce/order/dto/QcRequestDto.java#L29), các thuộc tính danh sách `items` không được đánh dấu bằng annotation `@Valid`. Do đó, Spring Boot Validation bỏ qua việc kiểm định các ràng buộc của từng phần tử bên trong danh sách (như `@NotNull` của `quantity`, `orderItemId`, hay `condition`). Khi client gửi dữ liệu lỗi (ví dụ `quantity` là null hoặc `condition` sai giá trị CHECK constraint của CSDL), hệ thống sẽ không chặn lại ở Controller mà chui sâu vào DB gây lỗi `NullPointerException` hoặc vi phạm Check Constraint, dẫn đến phản hồi lỗi `500 Internal Server Error` thay vì `400 Bad Request`.
*   **Điều kiện xảy ra:** Khách hàng gửi yêu cầu đổi trả hoặc kiểm định QC chứa các trường dữ liệu danh sách con bị lỗi hoặc rỗng.
*   **Các bước tái hiện:**
    1. Gửi request `POST /api/v1/returns` với payload chứa item mang `quantity: null`.
*   **Kết quả mong đợi:** Trả về `400 Bad Request` chỉ rõ lỗi validate.
*   **Kết quả thực tế:** Trả về `500 Internal Server Error` (NullPointerException).
*   **Mức độ nghiêm trọng:** 🟡 **High**

---

### BUG-19: Luồng đổi trả RMA bị đóng băng do thiếu cơ chế chuyển đơn hàng sang COMPLETED
*   **Mô tả:** 
    Trong `RmaServiceImpl.submitReturn`, hệ thống kiểm tra đơn hàng phải có trạng thái `"COMPLETED"` mới cho phép đổi trả. Tuy nhiên, rà soát toàn bộ source code của `order-service` thì không có bất kỳ logic nghiệp vụ, API, listener hay scheduler nào hỗ trợ cập nhật trạng thái đơn hàng sang `"COMPLETED"`. Đơn hàng sau khi thanh toán thành công chỉ dừng lại ở `"CONFIRMED"`. Điều này khiến tính năng đổi trả hàng (RMA) hoàn toàn bị tê liệt đối với tất cả khách hàng.
*   **Điều kiện xảy ra:** Khách hàng muốn đổi trả hàng sau khi đã nhận được hàng thực tế.
*   **Các bước tái hiện:**
    1. Đặt mua và thanh toán đơn hàng thành công (Order status = `"CONFIRMED"`).
    2. Không tìm thấy bất kỳ API nào (như Xác nhận đã nhận hàng / Giao hàng thành công) để chuyển trạng thái sang `"COMPLETED"`.
    3. Cố gắng gọi API gửi yêu cầu đổi trả, nhận về lỗi chặn `"Only completed orders can be returned."`.
*   **Kết quả mong đợi:** Có một cơ chế (ví dụ API giao nhận thành công hoặc Webhook vận chuyển) để chuyển trạng thái đơn hàng sang `COMPLETED`.
*   **Kết quả thực tế:** Trạng thái đơn hàng kẹt mãi ở `CONFIRMED` và không thể đổi trả.
*   **Mức độ nghiêm trọng:** 🟡 **High**

---

### BUG-20: Luồng giải phóng kho thiết kế kém hiệu quả và rủi ro lỗi mạng (Circular Call)
*   **Mô tả:** 
    Luồng đền bù giao dịch (Saga Rollback) giải phóng kho khi huỷ đơn hàng được thiết kế theo mô hình vòng lặp hồi quy qua mạng: `order-service` hủy đơn -> bắn event `order.cancelled` lên RabbitMQ -> `product-service` nhận event -> gọi ngược HTTP Feign Client (`orderClient.releaseStock`) về `order-service` để cập nhật bảng tồn kho `WarehouseStock` thuộc schema `inventory` nằm trong chính cơ sở dữ liệu của `order-service`! Đây là một anti-pattern nghiêm trọng về mặt kiến trúc microservices: tăng độ trễ mạng vô lý, tạo liên kết vòng quanh (circular dependency), và nếu `product-service` bị offline thì kho hàng sẽ không bao giờ được giải phóng.
*   **Điều kiện xảy ra:** Đơn hàng bị huỷ tự động do quá hạn thanh toán.
*   **Mức độ nghiêm trọng:** ⚪ **Medium**

---

### BUG-21: Bất nhất trạng thái giỏ hàng khi checkout thất bại (Redis Cart bị xóa trước khi DB commit)
*   **Mô tả:** 
    Trong [CheckoutTxHelper.java](file:///Users/admin/IdeaProjects/E-CommercePlatform/order-service/src/main/java/com/ecommerce/order/service/impl/CheckoutTxHelper.java#L123-L124), lệnh xoá giỏ hàng trên Redis (`redisTemplate.delete(cartKey)`) được gọi trực tiếp bên trong phương thức `@Transactional`. Nếu quá trình commit database sau đó thất bại (do lỗi khóa ngoại, lỗi kết nối hoặc xung đột khóa lạc quan ở luồng khác), database transaction sẽ rollback (đơn hàng không được tạo), nhưng giỏ hàng trên Redis đã bị xoá vĩnh viễn và không thể phục hồi, khiến người dùng bị mất sạch giỏ hàng.
*   **Điều kiện xảy ra:** DB transaction gặp lỗi và thực hiện rollback trong quá trình checkout đơn hàng.
*   **Các bước tái hiện:**
    1. Thêm sản phẩm vào giỏ hàng.
    2. Thực hiện checkout. Cố tình gây lỗi DB (ví dụ: vi phạm ràng buộc dữ liệu đơn hàng).
    3. Đơn hàng thất bại, giỏ hàng bị mất sạch.
*   **Kết quả mong đợi:** Giỏ hàng chỉ được xoá trên Redis sau khi DB transaction đã commit thành công (sử dụng callback `afterCommit`).
*   **Kết quả thực tế:** Giỏ hàng bị xoá ngay lập tức dù đơn hàng tạo lỗi.
*   **Mức độ nghiêm trọng:** 🟡 **High**

---

### BUG-22: Nguy cơ trùng lặp ID đơn hàng dưới tải cao (ID Collision)
*   **Mô tả:** 
    ID đơn hàng được sinh thủ công bằng công thức: `System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(1000)`. Dưới tải cao, tỷ lệ hai luồng đồng thời chạy trong cùng một mili giây và sinh ra cùng số ngẫu nhiên từ 0-999 là hoàn toàn có thể xảy ra (xác suất trùng tăng cao do Birthday Paradox). Khi đó, giao dịch chèn dữ liệu đơn hàng sẽ bị ném lỗi `UniqueConstraintViolationException` (Primary Key violation) và làm hỏng luồng thanh toán của khách hàng.
*   **Điều kiện xảy ra:** Hệ thống chạy dưới tải lớn với hàng ngàn request checkout đồng thời trong cùng một mili giây.
*   **Mức độ nghiêm trọng:** ⚪ **Medium**

---

### BUG-23: Lỗ hổng bảo mật do phơi bày cổng trực tiếp và bỏ qua xác thực token của order-service
*   **Mô tả:** 
    Giống như `product-service`, dịch vụ `order-service` chạy trên cổng `8083` không cấu hình bảo mật Spring Security và không thực hiện bất kỳ việc xác thực chữ ký JWT nào ở tầng ứng dụng. Nó hoàn toàn tin cậy các headers `X-User-Id` và `X-User-Roles` được truyền tới. Kẻ tấn công có thể bypass API Gateway, gọi trực tiếp vào IP và cổng `8083` của dịch vụ này, tự đính kèm header `X-User-Id` và `X-User-Roles: ROLE_ADMIN` để tự do thanh toán, huỷ đơn hàng, hoặc duyệt hoàn tiền trái phép.
*   **Điều kiện xảy ra:** Cổng port `8083` của `order-service` bị phơi ra bên ngoài mạng internet (hoặc kẻ tấn công nội bộ).
*   **Các bước tái hiện:**
    1. Sử dụng Postman gửi request trực tiếp `POST http://localhost:8083/api/v1/returns/{id}/approve?orderCreatedAt=...` kèm header `X-User-Roles: ROLE_ADMIN`.
*   **Kết quả mong đợi:** Service phải từ chối truy cập và yêu cầu token JWT hợp lệ được xác thực từ dịch vụ Auth.
*   **Kết quả thực tế:** API chấp nhận request và thực hiện phê duyệt hoàn tiền thành công.
*   **Mức độ nghiêm trọng:** 🔴 **Critical**

---

## 🚀 Đánh giá tổng thể từ Principal Software Engineer & QA Lead

Hệ thống đã hoàn thành 20 task cốt lõi tạo nên bộ khung microservices mạnh mẽ. Tuy nhiên, qua phân tích sâu các task mới (từ 12 đến 20) liên quan đến Giỏ hàng Redis, Thanh toán VNPay, Quản lý kho đa chi nhánh, và Đổi trả RMA, chúng tôi phát hiện thêm **10 lỗi tiềm ẩn (BUG-14 đến BUG-23)** liên quan đến race condition tồn kho, lỗ hổng tài chính đổi trả, an ninh mạng trực tiếp, và bất nhất trạng thái giỏ hàng khi rollback giao dịch.

Các lỗi thuộc nhóm 🔴 **Critical** và 🟡 **High** cần được ưu tiên khắc phục bằng các biện pháp vá lỗi mức production (như bổ sung `@Version` vào `WarehouseStock`/`Coupon`, tích hợp kiểm tra trùng lặp ReturnRequest, xử lý `@Valid` lồng nhau, và đồng bộ hóa Redis Cart Clear bằng `TransactionSynchronizationManager`) để đảm bảo hệ thống đạt độ tin cậy vận hành thực tế.
