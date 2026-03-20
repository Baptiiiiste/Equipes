package fr.baptiiiiste.server.persistence;

import fr.baptiiiiste.server.models.Room;

import java.util.List;

public interface RoomRepository {

	List<Room> findAllRooms();

	Room saveRoom(Room room);
}

