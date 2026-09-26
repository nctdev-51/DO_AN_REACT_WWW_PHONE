USE webshop_db;

INSERT INTO categories (name, description)
SELECT 'Apple', 'Điện thoại Apple iPhone.'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Apple');

INSERT INTO categories (name, description)
SELECT 'Samsung', 'Điện thoại Samsung Galaxy.'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Samsung');

INSERT INTO categories (name, description)
SELECT 'Xiaomi', 'Điện thoại Xiaomi.'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Xiaomi');

INSERT INTO categories (name, description)
SELECT 'OPPO', 'Điện thoại OPPO.'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'OPPO');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'iPhone 16 Pro Max 256GB', 'Apple', NULL, 34990000, 'Màn hình Super Retina XDR, chip A18 Pro.', 1,
       (SELECT id FROM categories WHERE name = 'Apple' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'iPhone 16 Pro Max 256GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'iPhone 16 Pro 128GB', 'Apple', NULL, 28990000, 'Thiết kế titan, camera chuyên nghiệp.', 1,
       (SELECT id FROM categories WHERE name = 'Apple' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'iPhone 16 Pro 128GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'iPhone 16 128GB', 'Apple', NULL, 22990000, 'Hiệu năng mạnh mẽ cùng Apple Intelligence.', 1,
       (SELECT id FROM categories WHERE name = 'Apple' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'iPhone 16 128GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'iPhone 15 128GB', 'Apple', NULL, 17990000, 'Camera 48MP, thiết kế Dynamic Island.', 1,
       (SELECT id FROM categories WHERE name = 'Apple' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'iPhone 15 128GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'Galaxy S25 Ultra 256GB', 'Samsung', NULL, 33990000, 'Galaxy AI, camera 200MP, S Pen.', 1,
       (SELECT id FROM categories WHERE name = 'Samsung' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Galaxy S25 Ultra 256GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'Galaxy S25+ 256GB', 'Samsung', NULL, 26990000, 'Màn hình Dynamic AMOLED 2X sắc nét.', 1,
       (SELECT id FROM categories WHERE name = 'Samsung' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Galaxy S25+ 256GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'Galaxy S25 256GB', 'Samsung', NULL, 22990000, 'Flagship nhỏ gọn, hiệu năng Galaxy AI.', 1,
       (SELECT id FROM categories WHERE name = 'Samsung' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Galaxy S25 256GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'Galaxy A56 5G 256GB', 'Samsung', NULL, 10990000, 'Màn hình lớn, pin bền bỉ, kết nối 5G.', 1,
       (SELECT id FROM categories WHERE name = 'Samsung' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Galaxy A56 5G 256GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'Xiaomi 15 Ultra 512GB', 'Xiaomi', NULL, 29990000, 'Hệ thống camera Leica cao cấp.', 1,
       (SELECT id FROM categories WHERE name = 'Xiaomi' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Xiaomi 15 Ultra 512GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'Xiaomi 15 256GB', 'Xiaomi', NULL, 19990000, 'Hiệu năng flagship trong thiết kế nhỏ gọn.', 1,
       (SELECT id FROM categories WHERE name = 'Xiaomi' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Xiaomi 15 256GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'Redmi Note 14 Pro+ 5G', 'Xiaomi', NULL, 10990000, 'Camera độ phân giải cao, sạc nhanh.', 1,
       (SELECT id FROM categories WHERE name = 'Xiaomi' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Redmi Note 14 Pro+ 5G');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'Redmi Note 14 256GB', 'Xiaomi', NULL, 6490000, 'Màn hình AMOLED, pin dung lượng lớn.', 1,
       (SELECT id FROM categories WHERE name = 'Xiaomi' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Redmi Note 14 256GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'OPPO Find X8 Pro 512GB', 'OPPO', NULL, 29990000, 'Camera Hasselblad và sạc nhanh.', 1,
       (SELECT id FROM categories WHERE name = 'OPPO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'OPPO Find X8 Pro 512GB');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'OPPO Reno13 Pro 5G', 'OPPO', NULL, 18990000, 'Thiết kế mỏng nhẹ, camera chân dung.', 1,
       (SELECT id FROM categories WHERE name = 'OPPO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'OPPO Reno13 Pro 5G');

INSERT INTO products (name, brand, image, price, description, status, category_id)
SELECT 'OPPO A5 Pro 5G', 'OPPO', NULL, 7990000, 'Điện thoại 5G bền bỉ cho nhu cầu hằng ngày.', 1,
       (SELECT id FROM categories WHERE name = 'OPPO' ORDER BY id LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'OPPO A5 Pro 5G');
