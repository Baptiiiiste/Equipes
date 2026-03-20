package fr.baptiiiiste.server.handlers;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.models.packets.*;
import fr.baptiiiiste.server.models.ChatMessage;
import fr.baptiiiiste.server.models.Room;
import fr.baptiiiiste.server.models.Server;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

@Setter
@Getter
public class ClientHandler implements PacketHandler, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private Socket socket;
    private Server server;
    private Room currentRoom;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientId;
    private boolean running = true;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.clientId = UUID.randomUUID().toString();
    }

    @Override
    public void run() {
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            while (running) {
                Object obj = in.readObject();
                if (obj instanceof Packet packet) {
                    packet.execute(this);
                }
            }
        } catch (Exception e) {
            logger.error("[run] {}", e.getMessage());
        } finally {
            closeConnection();
        }
    }

    @Override
    public void handle(TextPacket packet) {
        if (currentRoom == null || !currentRoom.getRoomId().equals(packet.getRoomId())) {
            logger.error("[handle] Client {} tried to send TEXT to room {} but is in room {}", clientId, packet.getRoomId(), currentRoom != null ? currentRoom.getRoomId() : "none");
            return;
        }
        
        this.currentRoom.getChatRepository().saveMessage(
                new ChatMessage(packet.getRoomId(), packet.getSenderId(), packet.getMessage(), packet.getTimestamp())
        );

        logger.info("[{}] {}: {}", packet.getRoomId(), clientId, packet.getMessage());
        currentRoom.broadcast(packet, this);
    }

    @Override
    public void handle(JoinRoomPacket packet) {
        String roomId = packet.getRoomId();

        if (packet.getSenderId() != null && !packet.getSenderId().isBlank()) {
            this.clientId = packet.getSenderId();
        }

        if (currentRoom != null) {
            leaveCurrentRoom();
        }

        Room room = server.getRoom(roomId);
        if (room != null) {
            joinRoom(room);
            logger.info("[{}] {} joined the room", packet.getRoomId(), clientId);
        } else {
            logger.error("[handle] Room {} does not exist for client {}", roomId, clientId);
        }
    }

    @Override
    public void handle(LeaveRoomPacket packet) {
        leaveCurrentRoom();
    }

    public void joinRoom(Room room) {
        this.currentRoom = room;
        room.addClient(this);
    }

    public void leaveCurrentRoom() {
        if (currentRoom != null) {
            currentRoom.removeClient(this);
            this.currentRoom = null;
        }
    }

    public void sendPacket(Packet packet) {
        try {
            out.writeObject(packet);
            out.flush();
        } catch (Exception e) {
            logger.error("[sendPacket] {}", e.getMessage());
        }
    }

    private void closeConnection() {
        running = false;
        leaveCurrentRoom();
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            socket.close();
        } catch (Exception e) {
            logger.error("[closeConnection] {}", e.getMessage());
        }
    }
}