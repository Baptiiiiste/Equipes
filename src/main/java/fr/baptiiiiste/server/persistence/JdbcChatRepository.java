package fr.baptiiiiste.server.persistence;

import fr.baptiiiiste.server.models.ChatMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcChatRepository implements ChatRepository {

    private static final String INSERT_MESSAGE_SQL = """
            INSERT INTO chat_messages (room_id, sender_id, message, sent_at)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_MESSAGES_BY_ROOM_SQL = """
            SELECT id, room_id, sender_id, message, sent_at
            FROM (
                SELECT id, room_id, sender_id, message, sent_at
                FROM chat_messages
                WHERE room_id = ?
                ORDER BY sent_at DESC
                LIMIT ?
            ) recent_messages
            ORDER BY sent_at ASC
            """;

    private final DatabaseConfig databaseConfig;

    public JdbcChatRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_MESSAGE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, chatMessage.getRoomId());
            statement.setString(2, chatMessage.getSenderId());
            statement.setString(3, chatMessage.getMessage());
            statement.setLong(4, chatMessage.getSentAt());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    chatMessage.setId(generatedKeys.getLong(1));
                }
            }

            return chatMessage;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save chat message", e);
        }
    }

    @Override
    public List<ChatMessage> findMessagesByRoomId(String roomId, int limit) {
        int safeLimit = Math.max(limit, 1);
        List<ChatMessage> messages = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_MESSAGES_BY_ROOM_SQL)) {

            statement.setString(1, roomId);
            statement.setInt(2, safeLimit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ChatMessage message = new ChatMessage(
                            resultSet.getLong("id"),
                            resultSet.getString("room_id"),
                            resultSet.getString("sender_id"),
                            resultSet.getString("message"),
                            resultSet.getLong("sent_at")
                    );
                    messages.add(message);
                }
            }

            return messages;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load chat messages for room '" + roomId + "'", e);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                databaseConfig.getJdbcUrl(),
                databaseConfig.getUsername(),
                databaseConfig.getPassword()
        );
    }
}

