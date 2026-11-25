ALTER TABLE orders ADD COLUMN payment_channel VARCHAR(30);
ALTER TABLE orders ADD COLUMN payment_expires_at DATETIME(6);
ALTER TABLE orders ADD COLUMN qr_token VARCHAR(32);

CREATE INDEX idx_orders_payment_expiry ON orders (status, payment_status, payment_expires_at);
