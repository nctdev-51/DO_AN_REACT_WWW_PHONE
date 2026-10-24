-- PTV Mobile schema. Import before sample-data.sql.
CREATE DATABASE IF NOT EXISTS webshop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE webshop_db;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_tokens` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `consumed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `digest` varchar(64) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `purpose` enum('RESET_PASSWORD','VERIFY_EMAIL') NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfa5k19gx82xl0s5p7rkht9mfb` (`digest`),
  KEY `idx_token_user_purpose` (`user_id`,`purpose`,`created_at`),
  CONSTRAINT `FK19h8y4psfcpn5ct8b9mm2o2na` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `default_address` bit(1) NOT NULL,
  `line` varchar(255) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `recipient_name` varchar(100) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `province` varchar(100) DEFAULT NULL,
  `province_code` varchar(10) DEFAULT NULL,
  `ward` varchar(100) DEFAULT NULL,
  `ward_code` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1fa36y2oqhao3wgg2rw1pi459` (`user_id`),
  CONSTRAINT `FK1fa36y2oqhao3wgg2rw1pi459` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `campaign_banners` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action_label` varchar(40) NOT NULL,
  `active` bit(1) NOT NULL,
  `ends_at` datetime(6) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `image_alt` varchar(160) DEFAULT NULL,
  `placement` varchar(10) NOT NULL,
  `position` int(11) NOT NULL,
  `starts_at` datetime(6) NOT NULL,
  `subtitle` varchar(240) DEFAULT NULL,
  `target` varchar(20) NOT NULL,
  `theme` varchar(10) NOT NULL,
  `title` varchar(120) NOT NULL,
  `version` bigint(20) NOT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK16yjcints4j1q2ldm0wknx37r` (`product_id`),
  CONSTRAINT `FK16yjcints4j1q2ldm0wknx37r` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` text DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contact_name` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `message` varchar(1500) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `quoted_amount` decimal(19,0) DEFAULT NULL,
  `response` varchar(1500) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `type` varchar(20) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdjrad38uhenyhfslyikjck3n3` (`product_id`),
  KEY `FK96gr1fy33fqe11s3fi2ynyq24` (`user_id`),
  CONSTRAINT `FK96gr1fy33fqe11s3fi2ynyq24` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKdjrad38uhenyhfslyikjck3n3` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `loyalty_entries` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(200) NOT NULL,
  `points` bigint(20) NOT NULL,
  `reference` varchar(100) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfsjipmlvw5ps0vkv8k1ti8r7c` (`reference`),
  KEY `FKs7oihpkey1pu4oe8jjobjrkvd` (`order_id`),
  KEY `FKs9bwwsfs4rmsxryvfpv2n2c5u` (`user_id`),
  CONSTRAINT `FKs7oihpkey1pu4oe8jjobjrkvd` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKs9bwwsfs4rmsxryvfpv2n2c5u` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `price` decimal(19,0) NOT NULL,
  `quantity` int(11) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `sku` varchar(120) DEFAULT NULL,
  `variant_label` varchar(200) DEFAULT NULL,
  `variant_id` bigint(20) DEFAULT NULL,
  `warranty_months` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjyu2qbqt8gnvno9oe9j2s2ldk` (`order_id`),
  KEY `FK4q98utpd73imf4yhttm3w0eax` (`product_id`),
  KEY `FK63mqrfva3vf2gx2sublm4m51w` (`variant_id`),
  CONSTRAINT `FK4q98utpd73imf4yhttm3w0eax` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FK63mqrfva3vf2gx2sublm4m51w` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`),
  CONSTRAINT `FKjyu2qbqt8gnvno9oe9j2s2ldk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_events` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `actor` varchar(30) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `note` varchar(500) NOT NULL,
  `order_status` varchar(50) NOT NULL,
  `payment_status` varchar(30) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjkqdh6opohx4rx9719gyn31aa` (`order_id`),
  CONSTRAINT `FKjkqdh6opohx4rx9719gyn31aa` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_date` datetime(6) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `shipping_address` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `total_amount` decimal(19,0) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `cancellation_reason` varchar(500) DEFAULT NULL,
  `gateway_transaction` varchar(100) DEFAULT NULL,
  `payment_method` varchar(20) DEFAULT 'COD',
  `payment_reference` varchar(80) DEFAULT NULL,
  `payment_status` varchar(30) DEFAULT 'UNPAID',
  `recipient_name` varchar(100) DEFAULT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `discount_amount` decimal(19,0) NOT NULL DEFAULT 0,
  `promotion_code` varchar(40) DEFAULT NULL,
  `shipping_fee` decimal(19,0) NOT NULL DEFAULT 0,
  `subtotal` decimal(19,0) DEFAULT NULL,
  `promotion_id` bigint(20) DEFAULT NULL,
  `carrier` varchar(80) DEFAULT NULL,
  `earn_every_snapshot` decimal(19,0) DEFAULT NULL,
  `estimated_delivery_from` date DEFAULT NULL,
  `estimated_delivery_to` date DEFAULT NULL,
  `points_discount` decimal(19,0) NOT NULL DEFAULT 0,
  `points_earned` bigint(20) NOT NULL DEFAULT 0,
  `points_restored` bigint(20) NOT NULL DEFAULT 0,
  `points_reversed` bigint(20) NOT NULL DEFAULT 0,
  `points_spent` bigint(20) NOT NULL DEFAULT 0,
  `province` varchar(100) DEFAULT NULL,
  `tracking_code` varchar(80) DEFAULT NULL,
  `ward` varchar(100) DEFAULT NULL,
  `carrier_status` varchar(40) DEFAULT NULL,
  `carrier_updated_at` datetime(6) DEFAULT NULL,
  `ghn_environment` varchar(12) DEFAULT NULL,
  `ghn_request_json` text DEFAULT NULL,
  `province_code` varchar(10) DEFAULT NULL,
  `ward_code` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3btd9er62i5mxl92w5ihi5kau` (`payment_reference`),
  KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`),
  KEY `FK42bki7v5u9s62olp5is82sd74` (`promotion_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK42bki7v5u9s62olp5is82sd74` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_media` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `caption` varchar(300) DEFAULT NULL,
  `color` varchar(80) DEFAULT NULL,
  `kind` varchar(10) NOT NULL,
  `path` varchar(500) NOT NULL,
  `sort_order` int(11) NOT NULL,
  `source` varchar(1000) DEFAULT NULL,
  `product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_media_product` (`product_id`,`kind`,`sort_order`),
  CONSTRAINT `FK6d0dleyyjhq9qg62vto0kn01f` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `color` varchar(80) NOT NULL,
  `price` decimal(19,0) NOT NULL,
  `ram` varchar(40) DEFAULT NULL,
  `sku` varchar(120) NOT NULL,
  `stock` int(11) NOT NULL,
  `storage_capacity` varchar(40) NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKq935p2d1pbjm39n0063ghnfgn` (`sku`),
  KEY `FKosqitn4s405cynmhb87lkvuau` (`product_id`),
  CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` text DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `price` decimal(19,0) NOT NULL,
  `status` int(11) NOT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `battery` varchar(120) DEFAULT NULL,
  `camera` varchar(200) DEFAULT NULL,
  `chipset` varchar(200) DEFAULT NULL,
  `display` varchar(200) DEFAULT NULL,
  `operating_system` varchar(100) DEFAULT NULL,
  `original_price` decimal(19,0) DEFAULT NULL,
  `sku` varchar(80) DEFAULT NULL,
  `warranty_months` int(11) DEFAULT NULL,
  `compatible_skus` varchar(1500) DEFAULT NULL,
  `kind` varchar(12) NOT NULL DEFAULT 'PHONE',
  `usage_tags` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfhmd06dsmj6k0n90swsh8ie9g` (`sku`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `amount` decimal(19,0) NOT NULL,
  `code` varchar(40) NOT NULL,
  `description` varchar(200) NOT NULL,
  `discount_type` varchar(10) NOT NULL,
  `ends_at` datetime(6) NOT NULL,
  `maximum_discount` decimal(19,0) DEFAULT NULL,
  `minimum_subtotal` decimal(19,0) NOT NULL,
  `starts_at` datetime(6) NOT NULL,
  `usage_limit` int(11) NOT NULL,
  `version` bigint(20) NOT NULL,
  `required_skus` varchar(1000) DEFAULT NULL,
  `scope_brand` varchar(100) DEFAULT NULL,
  `scope_skus` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKjdho73ymbyu46p2hh562dk4kk` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `refunds` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(19,0) NOT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `reason` varchar(500) NOT NULL,
  `settlement_reference` varchar(100) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `version` bigint(20) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsk9rqm7f6y8b1g0qob018hdm7` (`order_id`),
  CONSTRAINT `FKsk9rqm7f6y8b1g0qob018hdm7` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(2000) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `rating` int(11) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1nv3auyahyyy79hvtrcqgtfo9` (`user_id`,`product_id`),
  KEY `FKpl51cejpw4gy5swfar8br9ngi` (`product_id`),
  CONSTRAINT `FKcgy7qjc1r99dp117y9en6lxye` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpl51cejpw4gy5swfar8br9ngi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKofx66keruapi6vyqpv6f2or37` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_experience` (
  `id` bigint(20) NOT NULL,
  `delivery_max_days` int(11) NOT NULL DEFAULT 5 CHECK (`delivery_max_days` <= 60 and `delivery_max_days` >= 1),
  `delivery_min_days` int(11) NOT NULL DEFAULT 2 CHECK (`delivery_min_days` <= 30 and `delivery_min_days` >= 1),
  `earn_every` decimal(19,0) NOT NULL DEFAULT 100000,
  `featured_skus` varchar(1000) DEFAULT NULL,
  `headline` varchar(120) NOT NULL,
  `hero_sku` varchar(80) DEFAULT NULL,
  `hotline` varchar(30) DEFAULT NULL,
  `introduction` varchar(240) DEFAULT NULL,
  `loyalty_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `point_value` decimal(19,0) NOT NULL DEFAULT 1000,
  `redeem_percent` int(11) NOT NULL DEFAULT 10 CHECK (`redeem_percent` >= 1 and `redeem_percent` <= 50),
  `second_hero_sku` varchar(80) DEFAULT NULL,
  `store_address` varchar(255) DEFAULT NULL,
  `support_email` varchar(150) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_policy` (
  `id` bigint(20) NOT NULL,
  `free_shipping_threshold` decimal(19,0) NOT NULL,
  `return_window_days` int(11) NOT NULL DEFAULT 7,
  `shipping_fee` decimal(19,0) NOT NULL,
  `version` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `support_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `quantity` int(11) NOT NULL,
  `reason` varchar(2000) NOT NULL,
  `response` varchar(2000) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `type` varchar(12) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `order_detail_id` bigint(20) NOT NULL,
  `inspected` tinyint(1) NOT NULL DEFAULT 0,
  `inspection_note` varchar(500) DEFAULT NULL,
  `restocked` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FK6a9bfpara08xkm2q41tleltmd` (`order_detail_id`),
  CONSTRAINT `FK6a9bfpara08xkm2q41tleltmd` FOREIGN KEY (`order_detail_id`) REFERENCES `order_details` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `email_verified` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlist_entries` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6huf3w7k27h873n7y4pjodm8m` (`user_id`,`product_id`),
  KEY `FKfiqe6u47k9llar6sp0r5w3j28` (`product_id`),
  CONSTRAINT `FKfiqe6u47k9llar6sp0r5w3j28` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKrpr3hnic930fnxtm1kew3qy7f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

