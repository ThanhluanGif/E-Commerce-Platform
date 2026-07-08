-- 1. Table: orders.orders
ALTER TABLE orders.orders DETACH PARTITION orders.orders_default;
CREATE TABLE orders.orders_2026_h2 PARTITION OF orders.orders
    FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2027-01-01 00:00:00');
INSERT INTO orders.orders SELECT * FROM orders.orders_default WHERE created_at >= '2026-07-01 00:00:00' AND created_at < '2027-01-01 00:00:00';
DELETE FROM orders.orders_default WHERE created_at >= '2026-07-01 00:00:00' AND created_at < '2027-01-01 00:00:00';
ALTER TABLE orders.orders ATTACH PARTITION orders.orders_default DEFAULT;

-- 2. Table: orders.order_items
ALTER TABLE orders.order_items DETACH PARTITION orders.order_items_default;
CREATE TABLE orders.order_items_2026_h2 PARTITION OF orders.order_items
    FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2027-01-01 00:00:00');
INSERT INTO orders.order_items SELECT * FROM orders.order_items_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
DELETE FROM orders.order_items_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
ALTER TABLE orders.order_items ATTACH PARTITION orders.order_items_default DEFAULT;

-- 3. Table: orders.order_status_history
ALTER TABLE orders.order_status_history DETACH PARTITION orders.order_status_history_default;
CREATE TABLE orders.order_status_history_2026_h2 PARTITION OF orders.order_status_history
    FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2027-01-01 00:00:00');
INSERT INTO orders.order_status_history SELECT * FROM orders.order_status_history_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
DELETE FROM orders.order_status_history_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
ALTER TABLE orders.order_status_history ATTACH PARTITION orders.order_status_history_default DEFAULT;

-- 4. Table: orders.order_coupons
ALTER TABLE orders.order_coupons DETACH PARTITION orders.order_coupons_default;
CREATE TABLE orders.order_coupons_2026_h2 PARTITION OF orders.order_coupons
    FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2027-01-01 00:00:00');
INSERT INTO orders.order_coupons SELECT * FROM orders.order_coupons_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
DELETE FROM orders.order_coupons_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
ALTER TABLE orders.order_coupons ATTACH PARTITION orders.order_coupons_default DEFAULT;

-- 5. Table: orders.return_requests
ALTER TABLE orders.return_requests DETACH PARTITION orders.return_requests_default;
CREATE TABLE orders.return_requests_2026_h2 PARTITION OF orders.return_requests
    FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2027-01-01 00:00:00');
INSERT INTO orders.return_requests SELECT * FROM orders.return_requests_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
DELETE FROM orders.return_requests_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
ALTER TABLE orders.return_requests ATTACH PARTITION orders.return_requests_default DEFAULT;

-- 6. Table: orders.payment_transactions
ALTER TABLE orders.payment_transactions DETACH PARTITION orders.payment_transactions_default;
CREATE TABLE orders.payment_transactions_2026_h2 PARTITION OF orders.payment_transactions
    FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2027-01-01 00:00:00');
INSERT INTO orders.payment_transactions SELECT * FROM orders.payment_transactions_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
DELETE FROM orders.payment_transactions_default WHERE order_created_at >= '2026-07-01 00:00:00' AND order_created_at < '2027-01-01 00:00:00';
ALTER TABLE orders.payment_transactions ATTACH PARTITION orders.payment_transactions_default DEFAULT;
