-- Seed warehouses
INSERT INTO inventory.warehouses (id, name, code, address, province_city, is_active) VALUES
(1, 'Hà Nội Warehouse', 'WH-HN', 'Cầu Giấy, Hà Nội', 'Hà Nội', true),
(2, 'Hồ Chí Minh Warehouse', 'WH-HCM', 'Quận 5, TP Hồ Chí Minh', 'TP Hồ Chí Minh', true)
ON CONFLICT (id) DO NOTHING;

SELECT setval('inventory.warehouses_id_seq', (SELECT COALESCE(MAX(id), 1) FROM inventory.warehouses));

-- Seed warehouse stocks
INSERT INTO inventory.warehouse_stocks (id, warehouse_id, product_variant_id, physical_qty, reserved_qty) VALUES
(801, 1, 9001, 50, 5),
(802, 2, 9001, 30, 0)
ON CONFLICT (id) DO NOTHING;

SELECT setval('inventory.warehouse_stocks_id_seq', (SELECT COALESCE(MAX(id), 1) FROM inventory.warehouse_stocks));

-- Seed carts
INSERT INTO orders.carts (id, user_id, session_token) VALUES
(8001, 1001, 'sess-uuid-889911')
ON CONFLICT (id) DO NOTHING;

SELECT setval('orders.carts_id_seq', (SELECT COALESCE(MAX(id), 1) FROM orders.carts));

-- Seed cart items
INSERT INTO orders.cart_items (id, cart_id, product_variant_id, quantity) VALUES
(9501, 8001, 9001, 1)
ON CONFLICT (id) DO NOTHING;

SELECT setval('orders.cart_items_id_seq', (SELECT COALESCE(MAX(id), 1) FROM orders.cart_items));

-- Seed coupons
INSERT INTO orders.coupons (id, code, discount_type, discount_value, min_order_value, max_discount_amount, start_date, end_date, usage_limit, used_count, is_active) VALUES
(1, 'WELCOME100', 'FIXED_AMOUNT', 1000000.00, 20000000.00, 1000000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1000, 1, true)
ON CONFLICT (id) DO NOTHING;

SELECT setval('orders.coupons_id_seq', (SELECT COALESCE(MAX(id), 1) FROM orders.coupons));

-- Seed orders
INSERT INTO orders.orders (id, user_id, order_code, total_amount, shipping_fee, discount_amount, final_amount, status, payment_method, payment_status, recipient_name, recipient_phone, shipping_address, created_at, updated_at) VALUES
(30001, 1001, 'ORD-20260706-99A', 28000000.00, 50000.00, 1000000.00, 27050000.00, 'CONFIRMED', 'CREDIT_CARD', 'PAID', 'Nguyễn Hoàng Nam', '0987654321', 'Số 12 Ngõ 34 Cầu Giấy, Hà Nội', '2026-07-06 23:10:00', '2026-07-06 23:10:00')
ON CONFLICT (id, created_at) DO NOTHING;

-- Seed order items
INSERT INTO orders.order_items (id, order_id, product_variant_id, product_name, variant_sku, unit_price, discount_amount, quantity, total_price, order_created_at) VALUES
(45001, 30001, 9001, 'iPhone 15 Pro - Black - 128GB', 'IP15P-BLK-128', 28000000.00, 1000000.00, 1, 27000000.00, '2026-07-06 23:10:00')
ON CONFLICT (id, order_created_at) DO NOTHING;

-- Seed order coupon map
INSERT INTO orders.order_coupons (order_id, coupon_id, order_created_at) VALUES
(30001, 1, '2026-07-06 23:10:00')
ON CONFLICT (order_id, coupon_id, order_created_at) DO NOTHING;
