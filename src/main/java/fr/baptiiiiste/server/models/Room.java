package fr.baptiiiiste.server.models;

import fr.baptiiiiste.common.models.packets.Packet;
import fr.baptiiiiste.server.handlers.ClientHandler;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Room {

    private static final Logger logger = LoggerFactory.getLogger(Room.class);

    private String roomId;
    private String roomName;
    private List<ClientHandler> clients;
    private boolean isInMeeting;

    public Room(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.clients = new ArrayList<>();
        this.isInMeeting = false;
    }

    public void broadcast(Packet packet, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendPacket(packet);
            }
        }
    }

    public void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
        logger.info("[" + roomId + "] Client " + clientHandler.getClientId() + " joined the room");
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        logger.info("[" + roomId + "] Client " + clientHandler.getClientId() + " left the room");
    }

    @Override
    public String toString() {
        return roomName;
    }
}