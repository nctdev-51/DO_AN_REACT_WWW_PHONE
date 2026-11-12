-- Indexes support the store's most frequent catalog and order-history access paths.
CREATE INDEX idx_orders_user_order_date ON orders (user_id, order_date);
CREATE INDEX idx_orders_status_order_date ON orders (status, order_date);
CREATE INDEX idx_order_details_product_id ON order_details (product_id);
CREATE INDEX idx_product_variants_catalog ON product_variants (product_id, active, stock);
CREATE INDEX idx_products_catalog ON products (status, kind, brand);
CREATE INDEX idx_reviews_product_created_at ON reviews (product_id, created_at);
