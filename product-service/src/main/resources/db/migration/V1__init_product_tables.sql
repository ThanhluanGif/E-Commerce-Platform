CREATE SCHEMA IF NOT EXISTS catalog;

CREATE TABLE catalog.categories (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES catalog.categories(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    level INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    deleted_at TIMESTAMP
);

CREATE TABLE catalog.brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    logo_url VARCHAR(255),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    deleted_at TIMESTAMP
);

CREATE TABLE catalog.products (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES catalog.categories(id) ON DELETE RESTRICT,
    brand_id BIGINT REFERENCES catalog.brands(id) ON DELETE RESTRICT,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) UNIQUE NOT NULL,
    sku VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    short_description TEXT,
    thumbnail_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    view_count INT DEFAULT 0,
    rating_avg DECIMAL(3, 2) DEFAULT 0.00,
    rating_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE catalog.product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES catalog.products(id) ON DELETE CASCADE,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    compare_at_price DECIMAL(15, 2),
    low_stock_threshold INT DEFAULT 5,
    weight_grams INT DEFAULT 0,
    length_cm INT DEFAULT 0,
    width_cm INT DEFAULT 0,
    height_cm INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'OUT_OF_STOCK', 'INACTIVE')),
    version INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE catalog.product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES catalog.products(id) ON DELETE CASCADE,
    variant_id BIGINT REFERENCES catalog.product_variants(id) ON DELETE SET NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE catalog.attributes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    is_filterable BOOLEAN DEFAULT TRUE
);

CREATE TABLE catalog.attribute_values (
    id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT REFERENCES catalog.attributes(id) ON DELETE CASCADE,
    value VARCHAR(100) NOT NULL
);

CREATE TABLE catalog.variant_attribute_mappings (
    variant_id BIGINT REFERENCES catalog.product_variants(id) ON DELETE CASCADE,
    attribute_value_id BIGINT REFERENCES catalog.attribute_values(id) ON DELETE CASCADE,
    PRIMARY KEY (variant_id, attribute_value_id)
);

CREATE TABLE catalog.reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES catalog.products(id) ON DELETE CASCADE,
    variant_id BIGINT REFERENCES catalog.product_variants(id) ON DELETE SET NULL,
    user_id BIGINT,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(150),
    comment TEXT,
    image_urls TEXT,
    is_approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category ON catalog.products(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_brand ON catalog.products(brand_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_variants_product ON catalog.product_variants(product_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_variant_mappings_value ON catalog.variant_attribute_mappings(attribute_value_id);
