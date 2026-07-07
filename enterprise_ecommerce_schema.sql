-- ============================================================================
-- DATABASE SCHEMA FOR ENTERPRISE MULTI-VENDOR E-COMMERCE (SHOPEE-LIKE)
-- Database Platform: PostgreSQL 15+
-- Author: Antigravity AI
-- Description: Lược đồ cơ sở dữ liệu hoàn chỉnh, chuẩn doanh nghiệp, 
--              hỗ trợ phân tách đơn hàng, hệ thống Ledger, khóa tồn kho,
--              phân vùng bảng lịch sử và tối ưu hóa chỉ mục.
-- ============================================================================

-- BẬT TIỆN ÍCH MỞ RỘNG (EXTENSIONS) CẦN THIẾT
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- Hỗ trợ tìm kiếm Full-Text cơ bản qua LIKE/ILIKLE

-- ==========================================
-- 1. ĐỊNH NGHĨA CÁC KIỂU ENUM
-- ==========================================

CREATE TYPE user_role AS ENUM ('ADMIN', 'SELLER', 'CUSTOMER', 'STAFF');

CREATE TYPE shop_status AS ENUM ('ACTIVE', 'INACTIVE', 'BANNED', 'PENDING_APPROVAL');

CREATE TYPE staff_role AS ENUM ('MANAGER', 'PACKER', 'CHAT_SUPPORT', 'FINANCIAL');

CREATE TYPE product_status AS ENUM ('DRAFT', 'ACTIVE', 'BANNED', 'OUT_OF_STOCK');

CREATE TYPE reservation_status AS ENUM ('PENDING', 'COMPLETED', 'EXPIRED', 'RELEASED');

CREATE TYPE voucher_discount_type AS ENUM ('PERCENTAGE', 'FIXED_AMOUNT', 'FREE_SHIPPING');

CREATE TYPE voucher_scope AS ENUM ('PLATFORM_WIDE', 'SHOP_SPECIFIC');

CREATE TYPE order_status AS ENUM ('PENDING_PAYMENT', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED');

CREATE TYPE payment_method AS ENUM ('COD', 'MOMO', 'VNPAY', 'WALLET');

CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');

CREATE TYPE shipping_status AS ENUM ('CREATED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'RETURNED', 'FAILED');

CREATE TYPE wallet_type AS ENUM ('BUYER', 'SELLER', 'PLATFORM_ESCROW', 'PLATFORM_REVENUE');

CREATE TYPE transaction_type AS ENUM (
    'DEPOSIT',          -- Nạp tiền vào ví
    'WITHDRAW',         -- Rút tiền về ngân hàng
    'PURCHASE',         -- Mua hàng (trừ tiền)
    'REFUND',           -- Hoàn tiền (cộng tiền)
    'ESCROW_HOLD',      -- Tạm giữ tiền hàng (vào Escrow)
    'ESCROW_RELEASE',   -- Giải ngân tiền hàng (từ Escrow về ví Seller)
    'COMMISSION',       -- Phí hoa hồng nền tảng khấu trừ
    'SHIPPING_FEE',     -- Khấu trừ phí vận chuyển
    'PLATFORM_FEE'      -- Phí dịch vụ thanh toán cổng
);

-- ==========================================
-- 2. PHÂN HỆ NGƯỜI DÙNG & GIAN HÀNG
-- ==========================================

-- Bảng Người dùng (users)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    role user_role NOT NULL DEFAULT 'CUSTOMER',
    phone VARCHAR(20) UNIQUE,
    avatar_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Bảo mật nâng cao (2FA)
    two_fa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_fa_secret VARCHAR(100),
    
    -- Thống kê khách hàng thân thiết
    membership_tier VARCHAR(30) NOT NULL DEFAULT 'BRONZE', -- BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
    loyalty_points INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE users IS 'Bảng chứa thông tin tài khoản người dùng của toàn hệ thống';
COMMENT ON COLUMN users.role IS 'Phân quyền người dùng: ADMIN, SELLER, CUSTOMER, STAFF';
COMMENT ON COLUMN users.membership_tier IS 'Hạng thành viên ảo phục vụ loyalty program';

-- Bảng Gian hàng (shops)
CREATE TABLE shops (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    name VARCHAR(150) NOT NULL UNIQUE,
    slug VARCHAR(150) NOT NULL UNIQUE,
    logo_url VARCHAR(500),
    cover_url VARCHAR(500),
    description TEXT,
    status shop_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    rating_average NUMERIC(3, 2) NOT NULL DEFAULT 5.00,
    
    -- Địa chỉ kho lấy hàng mặc định
    pickup_address TEXT,
    pickup_phone VARCHAR(20),
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_shops_owner ON shops(owner_id);
CREATE INDEX idx_shops_slug ON shops(slug);
COMMENT ON TABLE shops IS 'Bảng thông tin các gian hàng người bán (Sellers)';

-- Bảng Nhân viên shop (shop_staffs)
CREATE TABLE shop_staffs (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role staff_role NOT NULL DEFAULT 'PACKER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_shop_user UNIQUE (shop_id, user_id)
);
COMMENT ON TABLE shop_staffs IS 'Bảng phân quyền nhân viên phụ giúp việc vận hành cho từng shop';

-- Bảng Sổ địa chỉ người dùng (user_addresses)
CREATE TABLE user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_name VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    province_city VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    ward VARCHAR(100) NOT NULL,
    detail_address TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_addresses_user ON user_addresses(user_id);

-- ==========================================
-- 3. PHÂN HỆ SẢN PHẨM & BIẾN THỂ (SKU)
-- ==========================================

-- Bảng Danh mục (categories)
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    icon_url VARCHAR(500),
    sort_order INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1, -- Độ sâu của danh mục
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_categories_parent ON categories(parent_id);

-- Bảng Sản phẩm (products)
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT NOT NULL REFERENCES shops(id) ON DELETE RESTRICT,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    
    -- Thông số kỹ thuật động (Dynamic Specification)
    -- Thay cho cấu hình EAV cồng kềnh, sử dụng JSONB của Postgres
    specifications JSONB DEFAULT '{}'::jsonb,
    
    status product_status NOT NULL DEFAULT 'DRAFT',
    main_image_url VARCHAR(500),
    rating_average NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    review_count INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_products_shop ON products(shop_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_specifications ON products USING gin (specifications); -- GIN Index cho JSONB
COMMENT ON COLUMN products.specifications IS 'Lưu trữ thông số kỹ thuật động dưới dạng JSON (ví dụ: {"ram": "8GB", "color": "Red"})';

-- Bảng Album ảnh sản phẩm (product_images)
CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_images_product ON product_images(product_id);

-- Bảng Biến thể sản phẩm - SKU (product_variants)
-- Đây là bảng cực kỳ quan trọng lưu trữ các đơn vị lưu kho thực tế (Stock Keeping Unit)
CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL, -- Ví dụ: "Màu Đỏ, Size L"
    sku_code VARCHAR(100) UNIQUE, -- Mã SKU nội bộ
    barcode VARCHAR(50), -- Mã vạch nhà sản xuất
    
    -- Thông tin giá cả
    price DECIMAL(15, 2) NOT NULL, -- Giá bán hiện tại
    original_price DECIMAL(15, 2) NOT NULL, -- Giá bán gốc trước khuyến mãi
    
    -- Quản lý tồn kho
    stock_quantity INT NOT NULL DEFAULT 0, -- Tồn kho vật lý thực tế
    reserved_quantity INT NOT NULL DEFAULT 0, -- Tồn kho bị khóa do đang thanh toán/chờ đóng hàng
    available_quantity INT GENERATED ALWAYS AS (stock_quantity - reserved_quantity) STORED, -- Tồn kho khả dụng để đặt tiếp
    
    -- Kích thước vận chuyển (rất quan trọng để tính phí ship tự động)
    weight_grams INT NOT NULL DEFAULT 0, -- Cân nặng tính bằng gram
    length_cm INT NOT NULL DEFAULT 0,
    width_cm INT NOT NULL DEFAULT 0,
    height_cm INT NOT NULL DEFAULT 0,
    
    image_url VARCHAR(500), -- Ảnh riêng cho biến thể này
    version INT NOT NULL DEFAULT 0, -- Dùng cho cơ chế Optimistic Locking tránh race condition
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_stock_valid CHECK (stock_quantity >= reserved_quantity)
);
CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_variants_sku ON product_variants(sku_code);
COMMENT ON TABLE product_variants IS 'Lưu trữ các biến thể cụ thể (SKUs) của sản phẩm';

-- ==========================================
-- 4. PHÂN HỆ QUẢN LÝ KHO (WMS)
-- ==========================================

-- Bảng Kho bãi vật lý (warehouses)
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    contact_phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Tồn kho chi tiết theo từng kho bãi (warehouse_inventories)
CREATE TABLE warehouse_inventories (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    physical_stock INT NOT NULL DEFAULT 0,
    reserved_stock INT NOT NULL DEFAULT 0,
    available_stock INT GENERATED ALWAYS AS (physical_stock - reserved_stock) STORED,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_warehouse_variant UNIQUE (warehouse_id, variant_id),
    CONSTRAINT check_warehouse_stock CHECK (physical_stock >= reserved_stock)
);
CREATE INDEX idx_warehouse_inv_variant ON warehouse_inventories(variant_id);

-- Bảng Đặt trước tồn kho tạm thời (stock_reservations)
-- Hỗ trợ khóa kho tạm thời khi khách thanh toán để chống overselling
CREATE TABLE stock_reservations (
    id BIGSERIAL PRIMARY KEY,
    variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    order_id BIGINT, -- Có thể NULL nếu khóa trước khi tạo đơn hàng (quá trình thanh toán giỏ hàng)
    quantity INT NOT NULL,
    status reservation_status NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_reservations_variant ON stock_reservations(variant_id);
CREATE INDEX idx_reservations_expires ON stock_reservations(expires_at) WHERE status = 'PENDING';
COMMENT ON TABLE stock_reservations IS 'Bảng quản lý khóa kho tạm thời tránh tranh chấp hàng tồn kho';

-- ==========================================
-- 5. PHÂN HỆ KHUYẾN MÃI (MARKETING ENGINE)
-- ==========================================

-- Bảng Mã giảm giá (vouchers)
CREATE TABLE vouchers (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT REFERENCES shops(id) ON DELETE CASCADE, -- NULL nghĩa là Platform Voucher (áp dụng toàn sàn)
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    scope voucher_scope NOT NULL DEFAULT 'SHOP_SPECIFIC',
    discount_type voucher_discount_type NOT NULL DEFAULT 'FIXED_AMOUNT',
    discount_value DECIMAL(15, 2) NOT NULL, -- Giá trị giảm (Ví dụ: 10% hoặc 50.000 VND)
    max_discount_amount DECIMAL(15, 2), -- Giới hạn giảm tối đa (nếu là dạng %)
    min_spend DECIMAL(15, 2) NOT NULL DEFAULT 0.00, -- Chi tiêu tối thiểu để được áp dụng
    
    total_usage_limit INT NOT NULL DEFAULT 0, -- Số lượng mã có thể phát hành tối đa
    per_user_limit INT NOT NULL DEFAULT 1, -- Số lần sử dụng tối đa của 1 user
    used_count INT NOT NULL DEFAULT 0, -- Số lần đã được áp dụng thực tế
    
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_vouchers_shop ON vouchers(shop_id);
CREATE INDEX idx_vouchers_code ON vouchers(code);

-- Bảng Ví Voucher của khách hàng (user_vouchers)
CREATE TABLE user_vouchers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    voucher_id BIGINT NOT NULL REFERENCES vouchers(id) ON DELETE CASCADE,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    claimed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_user_voucher UNIQUE (user_id, voucher_id)
);
CREATE INDEX idx_user_vouchers_user ON user_vouchers(user_id);

-- Bảng Sự kiện Flash Sale (flash_sales)
CREATE TABLE flash_sales (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng sản phẩm tham gia Flash Sale (flash_sale_items)
CREATE TABLE flash_sale_items (
    id BIGSERIAL PRIMARY KEY,
    flash_sale_id BIGINT NOT NULL REFERENCES flash_sales(id) ON DELETE CASCADE,
    variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    flash_sale_price DECIMAL(15, 2) NOT NULL,
    flash_sale_stock INT NOT NULL DEFAULT 0, -- Tồn kho phân bổ riêng cho Flash Sale
    flash_sale_sold INT NOT NULL DEFAULT 0, -- Đã bán trong đợt sale
    user_buy_limit INT NOT NULL DEFAULT 1, -- Giới hạn mua của 1 user
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_flashsale_items_event ON flash_sale_items(flash_sale_id);
CREATE INDEX idx_flashsale_items_variant ON flash_sale_items(variant_id);

-- ==========================================
-- 6. PHÂN HỆ GIỎ HÀNG (CART MODULE)
-- ==========================================

-- Bảng chi tiết giỏ hàng của User (cart_items)
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_cart_item UNIQUE (user_id, variant_id)
);
CREATE INDEX idx_cart_user ON cart_items(user_id);

-- ==========================================
-- 7. PHÂN HỆ ĐƠN HÀNG (ORDER MANAGEMENT SYSTEM)
-- ==========================================

-- BẢNG ĐƠN HÀNG MẸ (orders) - Lưu thông tin thanh toán tổng hợp
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    checkout_code VARCHAR(100) NOT NULL UNIQUE, -- Mã hóa hóa đơn chung để thanh toán
    
    -- Tài chính tổng quan đơn hàng
    total_items_price DECIMAL(15, 2) NOT NULL, -- Tổng tiền hàng gốc
    discount_platform DECIMAL(15, 2) NOT NULL DEFAULT 0.00, -- Giảm giá do mã Platform
    discount_shipping DECIMAL(15, 2) NOT NULL DEFAULT 0.00, -- Giảm giá phí ship do Platform
    total_shipping_fee DECIMAL(15, 2) NOT NULL, -- Tổng phí ship của tất cả các đơn con
    grand_total_payment DECIMAL(15, 2) NOT NULL, -- Tiền khách hàng phải trả thực tế
    
    -- Thông tin thanh toán
    payment_method payment_method NOT NULL DEFAULT 'COD',
    payment_status payment_status NOT NULL DEFAULT 'PENDING',
    
    -- Thông tin nhận hàng
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_checkout ON orders(checkout_code);
COMMENT ON TABLE orders IS 'Bảng Đơn Hàng Mẹ - Quản lý thanh toán và thông tin khách hàng chung';

-- BẢNG ĐƠN HÀNG CON (sub_orders) - Tách đơn theo từng Shop
CREATE TABLE sub_orders (
    id BIGSERIAL PRIMARY KEY,
    parent_order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE RESTRICT,
    shop_id BIGINT NOT NULL REFERENCES shops(id) ON DELETE RESTRICT,
    sub_order_code VARCHAR(100) NOT NULL UNIQUE, -- Định dạng: ORD-YYYYMMDD-XXXXXX
    
    -- Trạng thái riêng biệt của đơn này
    status order_status NOT NULL DEFAULT 'PENDING_PAYMENT',
    
    -- Tài chính riêng cho shop
    total_price DECIMAL(15, 2) NOT NULL, -- Tổng tiền hàng của shop
    discount_shop DECIMAL(15, 2) NOT NULL DEFAULT 0.00, -- Giảm giá do Voucher của Shop
    shipping_fee DECIMAL(15, 2) NOT NULL DEFAULT 0.00, -- Phí ship riêng của đơn này
    grand_total DECIMAL(15, 2) NOT NULL, -- Tổng thu hồi (total_price - discount_shop + shipping_fee)
    
    -- Thông tin vận chuyển
    shipping_carrier VARCHAR(100), -- Hãng vận chuyển (GHN, GHTK, J&T...)
    tracking_number VARCHAR(100), -- Mã vận đơn
    estimated_delivery TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_sub_orders_parent ON sub_orders(parent_order_id);
CREATE INDEX idx_sub_orders_shop ON sub_orders(shop_id);
CREATE INDEX idx_sub_orders_status ON sub_orders(status);
COMMENT ON TABLE sub_orders IS 'Bảng Đơn Hàng Con - Được chia tách theo gian hàng người bán';

-- Bảng chi tiết sản phẩm mua trong đơn hàng (order_items)
-- Cần sao lưu lại thông tin tại thời điểm mua (Snapshot) tránh việc Product thay đổi giá/tên sau này
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    sub_order_id BIGINT NOT NULL REFERENCES sub_orders(id) ON DELETE RESTRICT,
    variant_id BIGINT REFERENCES product_variants(id) ON DELETE SET NULL,
    
    -- Thông tin snapshotted
    product_name VARCHAR(255) NOT NULL,
    variant_name VARCHAR(150),
    sku_code VARCHAR(100),
    image_url VARCHAR(500),
    
    price DECIMAL(15, 2) NOT NULL, -- Giá tại thời điểm mua
    quantity INT NOT NULL,
    discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00, -- Giảm giá phân bổ trên sản phẩm này
    total_price DECIMAL(15, 2) GENERATED ALWAYS AS (price * quantity - discount_amount) STORED,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_order_items_sub ON order_items(sub_order_id);
COMMENT ON TABLE order_items IS 'Chi tiết các sản phẩm được đặt mua. Snapshotted toàn bộ giá và tên';

-- Bảng dòng lịch sử trạng thái đơn hàng (order_status_histories)
CREATE TABLE order_status_histories (
    id BIGSERIAL PRIMARY KEY,
    sub_order_id BIGINT NOT NULL REFERENCES sub_orders(id) ON DELETE CASCADE,
    status order_status NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_status_history_sub ON order_status_histories(sub_order_id);

-- Bảng yêu cầu trả hàng, hoàn tiền (return_requests)
CREATE TABLE return_requests (
    id BIGSERIAL PRIMARY KEY,
    sub_order_id BIGINT NOT NULL REFERENCES sub_orders(id) ON DELETE RESTRICT,
    reason TEXT NOT NULL,
    refund_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    evidence_urls TEXT[], -- Mảng ảnh/video làm bằng chứng
    admin_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_returns_sub ON return_requests(sub_order_id);

-- ==========================================
-- 8. PHÂN HỆ VÍ TIỀN & SỔ CÁI TÀI CHÍNH (LEDGER)
-- ==========================================

-- Bảng ví tiền (wallets)
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE RESTRICT, -- NULL đối với ví hệ thống
    shop_id BIGINT REFERENCES shops(id) ON DELETE RESTRICT,   -- NULL đối với ví user thông thường
    type wallet_type NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_wallet UNIQUE (user_id, type),
    CONSTRAINT uq_shop_wallet UNIQUE (shop_id, type),
    CONSTRAINT check_balance CHECK (balance >= 0.00) -- Ràng buộc không bao giờ ví bị âm tiền
);
CREATE INDEX idx_wallets_user ON wallets(user_id);
CREATE INDEX idx_wallets_shop ON wallets(shop_id);

-- Bảng Sổ Cái Kế Toán Hai Bên (ledger_entries)
-- Áp dụng cơ chế phân vùng bảng (Table Partitioning) theo thời gian của Postgres
CREATE TABLE ledger_entries (
    id BIGSERIAL,
    debit_wallet_id BIGINT NOT NULL REFERENCES wallets(id),  -- Ví nguồn (trừ tiền)
    credit_wallet_id BIGINT NOT NULL REFERENCES wallets(id), -- Ví đích (nhận tiền)
    amount DECIMAL(15, 2) NOT NULL,
    type transaction_type NOT NULL,
    reference_id VARCHAR(100), -- Có thể trỏ tới sub_order_id hoặc payment_transaction_id
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

COMMENT ON TABLE ledger_entries IS 'Sổ cái kép. Mọi biến động số dư phải ghi nhận dưới dạng trừ ví này cộng ví kia';

-- Ví dụ khai báo phân vùng cho ledger_entries theo thời gian năm 2026
CREATE TABLE ledger_entries_2026_q1 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-01-01 00:00:00+00') TO ('2026-04-01 00:00:00+00');
CREATE TABLE ledger_entries_2026_q2 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-04-01 00:00:00+00') TO ('2026-07-01 00:00:00+00');
CREATE TABLE ledger_entries_2026_q3 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-07-01 00:00:00+00') TO ('2026-10-01 00:00:00+00');
CREATE TABLE ledger_entries_2026_q4 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-10-01 00:00:00+00') TO ('2027-01-01 00:00:00+00');

-- Bảng chi tiết giao dịch cổng thanh toán (payment_transactions)
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    parent_order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE RESTRICT,
    transaction_code VARCHAR(150) NOT NULL UNIQUE, -- Mã từ MoMo/VNPay
    amount DECIMAL(15, 2) NOT NULL,
    payment_method payment_method NOT NULL,
    status payment_status NOT NULL DEFAULT 'PENDING',
    raw_response JSONB, -- Lưu log thô từ cổng để Audit khi có sự cố
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_payment_tx_parent ON payment_transactions(parent_order_id);

-- ==========================================
-- 9. PHÂN HỆ TƯƠNG TÁC (REVIEWS & CHAT)
-- ==========================================

-- Bảng đánh giá sản phẩm (reviews)
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id BIGINT REFERENCES product_variants(id) ON DELETE SET NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    media_urls TEXT[], -- Lưu danh sách link ảnh/video review
    
    -- Phản hồi từ người bán
    seller_reply TEXT,
    replied_at TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_reviews_product ON reviews(product_id);

-- Bảng sản phẩm yêu thích (wishlists)
CREATE TABLE wishlists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_wishlist UNIQUE (user_id, product_id)
);

-- Bảng phòng chat (chat_conversations)
CREATE TABLE chat_conversations (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shop_id BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_chat_party UNIQUE (buyer_id, shop_id)
);

-- Bảng tin nhắn chat chi tiết (chat_messages)
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    message_text TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_chat_msgs_conv ON chat_messages(conversation_id);

-- ==========================================
-- 10. PHÂN HỆ HÀNH VI & THEO DÕI HỆ THỐNG
-- ==========================================

-- Bảng Lịch sử Tìm kiếm (search_histories)
CREATE TABLE search_histories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    query_text TEXT NOT NULL,
    result_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_search_histories_user ON search_histories(user_id);

-- Bảng Log Hành vi người dùng phục vụ phân tích / gợi ý sản phẩm (user_activities)
-- Phân vùng bảng theo thời gian thực tế
CREATE TABLE user_activities (
    id BIGSERIAL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL, -- VIEW, ADD_TO_CART, PURCHASE, SHARE
    duration_seconds INT DEFAULT 0,
    user_agent VARCHAR(500),
    ip_address VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE TABLE user_activities_2026_q1 PARTITION OF user_activities
    FOR VALUES FROM ('2026-01-01 00:00:00+00') TO ('2026-04-01 00:00:00+00');
CREATE TABLE user_activities_2026_q2 PARTITION OF user_activities
    FOR VALUES FROM ('2026-04-01 00:00:00+00') TO ('2026-07-01 00:00:00+00');
CREATE TABLE user_activities_2026_q3 PARTITION OF user_activities
    FOR VALUES FROM ('2026-07-01 00:00:00+00') TO ('2026-10-01 00:00:00+00');
CREATE TABLE user_activities_2026_q4 PARTITION OF user_activities
    FOR VALUES FROM ('2026-10-01 00:00:00+00') TO ('2027-01-01 00:00:00+00');
