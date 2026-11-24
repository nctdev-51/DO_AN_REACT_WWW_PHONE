package iuh.fit.www_phone_ver1.service.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcChatMemory implements ChatMemory {
  private static final String INSERT_MESSAGE =
      "INSERT INTO chat_memory_messages (conversation_id, message_type, message_text) "
          + "VALUES (?, ?, ?)";

  private final JdbcTemplate jdbcTemplate;

  public JdbcChatMemory(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  @Transactional
  public void add(String conversationId, List<Message> messages) {
    validateConversationId(conversationId);
    for (Message message : messages) {
      jdbcTemplate.update(
          INSERT_MESSAGE,
          conversationId,
          message.getMessageType().getValue(),
          message.getText());
    }
    jdbcTemplate.update(
        "DELETE FROM chat_memory_messages WHERE conversation_id = ? AND id NOT IN "
            + "(SELECT id FROM (SELECT id FROM chat_memory_messages "
            + "WHERE conversation_id = ? ORDER BY id DESC LIMIT 20) recent_messages)",
        conversationId,
        conversationId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Message> get(String conversationId, int lastN) {
    validateConversationId(conversationId);
    if (lastN <= 0) {
      return List.of();
    }

    List<Message> messages =
        jdbcTemplate.query(
            "SELECT message_type, message_text FROM chat_memory_messages "
                + "WHERE conversation_id = ? ORDER BY id DESC LIMIT ?",
            (resultSet, rowNumber) ->
                toMessage(resultSet.getString("message_type"), resultSet.getString("message_text")),
            conversationId,
            lastN);
    Collections.reverse(messages);
    return messages;
  }

  @Override
  @Transactional
  public void clear(String conversationId) {
    validateConversationId(conversationId);
    jdbcTemplate.update(
        "DELETE FROM chat_memory_messages WHERE conversation_id = ?", conversationId);
  }

  private Message toMessage(String type, String text) {
    return switch (MessageType.fromValue(type)) {
      case USER -> new UserMessage(text);
      case ASSISTANT -> new AssistantMessage(text);
      case SYSTEM -> new SystemMessage(text);
      case TOOL ->
          throw new IllegalStateException("Tool messages are not supported in chat history.");
    };
  }

  private void validateConversationId(String conversationId) {
    if (conversationId == null || conversationId.isBlank() || conversationId.length() > 128) {
      throw new IllegalArgumentException("conversationId must contain 1 to 128 characters.");
    }
  }
}
