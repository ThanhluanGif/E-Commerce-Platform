#!/bin/bash
set -e

echo "Starting database initialization..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE DATABASE ecommerce_auth' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce_auth')\gexec
    SELECT 'CREATE DATABASE ecommerce_product' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce_product')\gexec
    SELECT 'CREATE DATABASE ecommerce_order' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce_order')\gexec
EOSQL

echo "Database initialization completed successfully."
