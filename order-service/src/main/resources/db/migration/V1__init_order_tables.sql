CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS inventory;

-- =========================================================================
-- 1. SCHEMA: orders (CART, ORDER, PROMOTION & PAYMENT)
-- =========================================================================

CREATE TABLE orders.carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT, -- Raw identifier (cross-db)
    session_token VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders.cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT REFERENCES orders.carts(id) ON DELETE CASCADE,
    product_variant_id BIGINT, -- Raw identifier (cross-db)
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders.coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_type VARCHAR(20) CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    discount_value DECIMAL(15, 2) NOT NULL,
    min_order_value DECIMAL(15, 2) DEFAULT 0.00,
    max_discount_amount DECIMAL(15, 2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    usage_limit INT DEFAULT 1,
    used_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_coupon_limit CHECK (used_count <= usage_limit)
);

CREATE TABLE orders.orders (
    id BIGINT NOT NULL,
    user_id BIGINT, -- Raw identifier (cross-db)
    order_code VARCHAR(50) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    shipping_fee DECIMAL(15, 2) NOT NULL,
    discount_amount DECIMAL(15, 2) DEFAULT 0.00,
    final_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELLED', 'REFUNDED')),
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    recipient_name VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    tracking_number VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE UNIQUE INDEX uq_orders_code ON orders.orders(order_code, created_at);

CREATE TABLE orders.order_items (
    id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    product_variant_id BIGINT, -- Raw identifier (cross-db)
    product_name VARCHAR(200) NOT NULL,
    variant_sku VARCHAR(50) NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    discount_amount DECIMAL(15, 2) DEFAULT 0.00,
    quantity INT NOT NULL CHECK (quantity > 0),
    total_price DECIMAL(15, 2) NOT NULL,
    order_created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id, order_created_at),
    FOREIGN KEY (order_id, order_created_at) REFERENCES orders.orders(id, created_at) ON DELETE CASCADE
) PARTITION BY RANGE (order_created_at);

CREATE TABLE orders.order_status_history (
    id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    order_created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id, order_created_at),
    FOREIGN KEY (order_id, order_created_at) REFERENCES orders.orders(id, created_at) ON DELETE CASCADE
) PARTITION BY RANGE (order_created_at);

CREATE TABLE orders.order_coupons (
    order_id BIGINT NOT NULL,
    coupon_id BIGINT REFERENCES orders.coupons(id) ON DELETE CASCADE,
    order_created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (order_id, coupon_id, order_created_at),
    FOREIGN KEY (order_id, order_created_at) REFERENCES orders.orders(id, created_at) ON DELETE CASCADE
) PARTITION BY RANGE (order_created_at);

CREATE TABLE orders.return_requests (
    id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT, -- Raw identifier (cross-db)
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'ITEM_RECEIVED', 'REFUNDED', 'REJECTED')),
    refund_amount DECIMAL(15, 2) NOT NULL,
    refund_status VARCHAR(20) DEFAULT 'PENDING',
    return_tracking_number VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    order_created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id, order_created_at),
    FOREIGN KEY (order_id, order_created_at) REFERENCES orders.orders(id, created_at) ON DELETE RESTRICT
) PARTITION BY RANGE (order_created_at);

CREATE TABLE orders.return_items (
    id BIGSERIAL,
    return_request_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    refund_price DECIMAL(15, 2) NOT NULL,
    condition VARCHAR(50) CHECK (condition IN ('UNOPENED', 'OPENED_GOOD', 'DAMAGED')),
    inspected_by BIGINT, -- Raw identifier (cross-db)
    inspection_notes TEXT,
    order_created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id, order_created_at),
    FOREIGN KEY (return_request_id, order_created_at) REFERENCES orders.return_requests(id, order_created_at) ON DELETE CASCADE,
    FOREIGN KEY (order_item_id, order_created_at) REFERENCES orders.order_items(id, order_created_at) ON DELETE RESTRICT
);

CREATE TABLE orders.payment_transactions (
    id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    transaction_code VARCHAR(100) NOT NULL,
    payment_gateway VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    status VARCHAR(30) NOT NULL,
    raw_response TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    order_created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id, order_created_at),
    FOREIGN KEY (order_id, order_created_at) REFERENCES orders.orders(id, created_at) ON DELETE RESTRICT
) PARTITION BY RANGE (order_created_at);

CREATE UNIQUE INDEX uq_payment_transactions_code ON orders.payment_transactions(transaction_code, order_created_at);

-- =========================================================================
-- 2. SCHEMA: inventory (INVENTORY MODULE)
-- =========================================================================

CREATE TABLE inventory.warehouses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    address VARCHAR(255) NOT NULL,
    province_city VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory.warehouse_stocks (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT REFERENCES inventory.warehouses(id) ON DELETE RESTRICT,
    product_variant_id BIGINT, -- Raw identifier (cross-db)
    physical_qty INT DEFAULT 0 CHECK (physical_qty >= 0),
    reserved_qty INT DEFAULT 0 CHECK (reserved_qty >= 0),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_warehouse_variant UNIQUE (warehouse_id, product_variant_id)
);

CREATE TABLE inventory.inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT REFERENCES inventory.warehouses(id) ON DELETE RESTRICT,
    product_variant_id BIGINT, -- Raw identifier (cross-db)
    type VARCHAR(20) CHECK (type IN ('STOCK_IN', 'STOCK_OUT', 'RESERVE', 'RELEASE')),
    quantity INT NOT NULL CHECK (quantity > 0),
    reference_type VARCHAR(30),
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_warehouse_stocks_variant ON inventory.warehouse_stocks(product_variant_id);

-- =========================================================================
-- 3. OUTBOX EVENTS
-- =========================================================================

CREATE TABLE IF NOT EXISTS public.outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_status ON public.outbox_events(status, created_at);

-- =========================================================================
-- 4. PARTITION SAFETY NET & H1 2026 PARTITIONS
-- =========================================================================

-- 2026 H1 Partitions
CREATE TABLE orders.orders_2026_h1 PARTITION OF orders.orders
    FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-07-01 00:00:00');

CREATE TABLE orders.order_items_2026_h1 PARTITION OF orders.order_items
    FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-07-01 00:00:00');

CREATE TABLE orders.order_status_history_2026_h1 PARTITION OF orders.order_status_history
    FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-07-01 00:00:00');

CREATE TABLE orders.order_coupons_2026_h1 PARTITION OF orders.order_coupons
    FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-07-01 00:00:00');

CREATE TABLE orders.return_requests_2026_h1 PARTITION OF orders.return_requests
    FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-07-01 00:00:00');

CREATE TABLE orders.payment_transactions_2026_h1 PARTITION OF orders.payment_transactions
    FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-07-01 00:00:00');

-- Default Partitions (Safety Net)
CREATE TABLE orders.orders_default PARTITION OF orders.orders DEFAULT;
CREATE TABLE orders.order_items_default PARTITION OF orders.order_items DEFAULT;
CREATE TABLE orders.order_status_history_default PARTITION OF orders.order_status_history DEFAULT;
CREATE TABLE orders.order_coupons_default PARTITION OF orders.order_coupons DEFAULT;
CREATE TABLE orders.return_requests_default PARTITION OF orders.return_requests DEFAULT;
CREATE TABLE orders.payment_transactions_default PARTITION OF orders.payment_transactions DEFAULT;
