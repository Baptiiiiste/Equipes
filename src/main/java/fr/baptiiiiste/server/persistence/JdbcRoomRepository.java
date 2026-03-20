package fr.baptiiiiste.server.persistence;

import fr.baptiiiiste.server.models.Room;
import fr.baptiiiiste.server.models.Server;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcRoomRepository implements RoomRepository {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final String SELECT_ALL_ROOMS_SQL = """
            SELECT room_id, room_name
            FROM rooms
            ORDER BY room_id
            """;

    private static final String UPSERT_ROOM_SQL = """
            INSERT INTO rooms (room_id, room_name)
            VALUES (?, ?)
            ON CONFLICT (room_id)
            DO UPDATE SET room_name = EXCLUDED.room_name
            """;

    private final DatabaseConfig databaseConfig;

    public JdbcRoomRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public List<Room> findAllRooms() {
        List<Room> rooms = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_ROOMS_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String roomId = resultSet.getString("room_id");
                String roomName = resultSet.getString("room_name");
                rooms.add(new Room(roomId, roomName));
            }

            return rooms;
        } catch (SQLException e) {
            logger.error("Failed to load rooms from PostgreSQL", e);
            throw new IllegalStateException("Failed to load rooms from PostgreSQL", e);
        }
    }

    @Override
    public Room saveRoom(Room room) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT_ROOM_SQL)) {

            statement.setString(1, room.getRoomId());
            statement.setString(2, room.getRoomName());
            statement.executeUpdate();
            return room;
        } catch (SQLException e) {
            logger.error("Failed to save room {} to PostgreSQL", room.getRoomId(), e);
            throw new IllegalStateException("Failed to save room '" + room.getRoomId() + "'", e);
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

