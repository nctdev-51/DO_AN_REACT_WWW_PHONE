CREATE TABLE chat_memory_messages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  conversation_id VARCHAR(128) NOT NULL,
  message_type VARCHAR(16) NOT NULL,
  message_text LONGTEXT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_chat_memory_conversation (conversation_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
