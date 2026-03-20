package fr.baptiiiiste.server.models;

import fr.baptiiiiste.common.models.packets.JoinRoomPacket;
import fr.baptiiiiste.common.models.packets.LeaveRoomPacket;
import fr.baptiiiiste.common.models.packets.Packet;
import fr.baptiiiiste.common.models.packets.TextPacket;
import fr.baptiiiiste.server.handlers.ClientHandler;
import fr.baptiiiiste.server.persistence.ChatRepository;
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
    private ChatRepository chatRepository;
    private boolean isInMeeting;

    public Room(String roomId, String roomName) {
        this(roomId, roomName, null);
    }

    public Room(String roomId, String roomName, ChatRepository chatRepository) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.clients = new ArrayList<>();
        this.chatRepository = chatRepository;
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

        // Send current room roster to the newly joined client, including self.
        for (ClientHandler client : clients) {
            clientHandler.sendPacket(new JoinRoomPacket(System.currentTimeMillis(), client.getClientId(), roomId));
        }

        // Load previous messages
        for (ChatMessage chatMessage : this.getChatRepository().findMessagesByRoomId(roomId, 150)) {
            clientHandler.sendPacket(new TextPacket(System.currentTimeMillis(), chatMessage.getSenderId(), roomId, chatMessage.getMessage()));
        }

        this.broadcast(new JoinRoomPacket(System.currentTimeMillis(), clientHandler.getClientId(), roomId), clientHandler);
        logger.info("[{}] Client {} joined the room", roomId, clientHandler.getClientId());
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        this.broadcast(new LeaveRoomPacket(System.currentTimeMillis(), clientHandler.getClientId(), roomId), clientHandler);
        logger.info("[{}] Client {} left the room", roomId, clientHandler.getClientId());
    }

    @Override
    public String toString() {
        return roomName;
    }
}