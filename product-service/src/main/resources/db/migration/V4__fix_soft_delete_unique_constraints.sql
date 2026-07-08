-- Drop inline absolute unique constraints that block soft-deleted duplicates
ALTER TABLE catalog.products DROP CONSTRAINT IF EXISTS products_slug_key;
ALTER TABLE catalog.products DROP CONSTRAINT IF EXISTS products_sku_key;
ALTER TABLE catalog.product_variants DROP CONSTRAINT IF EXISTS product_variants_sku_key;
ALTER TABLE catalog.categories DROP CONSTRAINT IF EXISTS categories_slug_key;
ALTER TABLE catalog.brands DROP CONSTRAINT IF EXISTS brands_slug_key;

-- Create partial unique indexes that filter out soft-deleted items (where deleted_at is not null)
CREATE UNIQUE INDEX uq_products_slug ON catalog.products(slug) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_products_sku ON catalog.products(sku) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_product_variants_sku ON catalog.product_variants(sku) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_categories_slug ON catalog.categories(slug) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_brands_slug ON catalog.brands(slug) WHERE deleted_at IS NULL;

-- Prevent duplicate attribute values for the same attribute (BUG-06)
CREATE UNIQUE INDEX uq_attribute_values_attr_val ON catalog.attribute_values(attribute_id, value);
