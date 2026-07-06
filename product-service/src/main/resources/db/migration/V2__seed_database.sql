-- Seed hierarchical categories
INSERT INTO catalog.categories (id, parent_id, name, slug, level, sort_order, is_active) VALUES
(10, NULL, 'Thiết Bị Điện Tử', 'thiet-bi-dien-tu', 0, 1, true),
(11, 10, 'Điện Thoại Di Động', 'dien-thoai-di-dong', 1, 1, true)
ON CONFLICT (id) DO NOTHING;

SELECT setval('catalog.categories_id_seq', (SELECT COALESCE(MAX(id), 1) FROM catalog.categories));

-- Seed brands
INSERT INTO catalog.brands (id, name, slug, logo_url, description, is_active) VALUES
(1, 'Apple', 'apple', 'apple_logo.png', 'Apple Inc. products', true)
ON CONFLICT (id) DO NOTHING;

SELECT setval('catalog.brands_id_seq', (SELECT COALESCE(MAX(id), 1) FROM catalog.brands));

-- Seed attributes
INSERT INTO catalog.attributes (id, name, is_filterable) VALUES
(1, 'Màu sắc', true),
(2, 'Dung lượng', true)
ON CONFLICT (id) DO NOTHING;

SELECT setval('catalog.attributes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM catalog.attributes));

-- Seed attribute values
INSERT INTO catalog.attribute_values (id, attribute_id, value) VALUES
(50, 1, 'Black'),
(51, 1, 'Gold'),
(52, 2, '128GB'),
(53, 2, '256GB')
ON CONFLICT (id) DO NOTHING;

SELECT setval('catalog.attribute_values_id_seq', (SELECT COALESCE(MAX(id), 1) FROM catalog.attribute_values));

-- Seed products
INSERT INTO catalog.products (id, category_id, brand_id, name, slug, sku, description, short_description, thumbnail_url, status, view_count, rating_avg, rating_count) VALUES
(2001, 11, 1, 'iPhone 15 Pro', 'iphone-15-pro', 'IP15P-BASE', 'Latest iPhone 15 Pro from Apple', 'iPhone 15 Pro', 'iphone15pro.png', 'ACTIVE', 1500, 4.85, 120)
ON CONFLICT (id) DO NOTHING;

SELECT setval('catalog.products_id_seq', (SELECT COALESCE(MAX(id), 1) FROM catalog.products));

-- Seed product variants
INSERT INTO catalog.product_variants (id, product_id, sku, name, price, compare_at_price, low_stock_threshold, weight_grams, status, version) VALUES
(9001, 2001, 'IP15P-BLK-128', 'iPhone 15 Pro - Black - 128GB', 28000000.00, 29000000.00, 5, 187, 'ACTIVE', 2),
(9002, 2001, 'IP15P-GLD-256', 'iPhone 15 Pro - Gold - 256GB', 32000000.00, 33000000.00, 5, 187, 'ACTIVE', 5)
ON CONFLICT (id) DO NOTHING;

SELECT setval('catalog.product_variants_id_seq', (SELECT COALESCE(MAX(id), 1) FROM catalog.product_variants));

-- Seed variant mappings
INSERT INTO catalog.variant_attribute_mappings (variant_id, attribute_value_id) VALUES
(9001, 50),
(9001, 52),
(9002, 51),
(9002, 53)
ON CONFLICT (variant_id, attribute_value_id) DO NOTHING;
