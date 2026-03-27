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
import java.util.List;
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

    @Override
    public void handle(JoinMeetingPacket packet) {
        if (currentRoom == null || !currentRoom.getRoomId().equals(packet.getRoomId())) {
            logger.warn("[handle] JoinMeeting ignored: client {} not in room {}", clientId, packet.getRoomId());
            return;
        }

        // A client can only be in one meeting at a time, even across rooms.
        leaveMeetingsExcept(currentRoom);

        if (currentRoom.getClientsInMeeting().contains(this)) {
            return;
        }

        boolean firstInMeeting = currentRoom.getClientsInMeeting().isEmpty();
        currentRoom.getClientsInMeeting().add(this);

        sendPacket(new JoinMeetingPacket(System.currentTimeMillis(), clientId, currentRoom.getRoomId()));
        currentRoom.broadcast(new JoinMeetingPacket(System.currentTimeMillis(), clientId, currentRoom.getRoomId()), this);

        var audioSession = server.getAudioSessionRegistry().registerParticipant(currentRoom.getRoomId(), clientId);
        sendPacket(new AudioUdpOfferPacket(
                System.currentTimeMillis(),
                "server",
                currentRoom.getRoomId(),
                server.getAudioUdpPort(),
                audioSession.getToken()
        ));

        if (firstInMeeting) {
            currentRoom.broadcast(new MeetingStartPacket(System.currentTimeMillis(), clientId, currentRoom.getRoomId()), null);
            sendPacket(new MeetingStartPacket(System.currentTimeMillis(), clientId, currentRoom.getRoomId()));
        }
    }


    @Override
    public void handle(LeaveMeetingPacket packet) {
        Room targetRoom = server.getRoom(packet.getRoomId());
        if (targetRoom == null) {
            logger.warn("[handle] LeaveMeeting ignored: room {} does not exist for client {}", packet.getRoomId(), clientId);
            return;
        }

        if (!targetRoom.getClientsInMeeting().contains(this)) {
            return;
        }

        leaveMeetingInRoom(targetRoom);
    }

    @Override
    public void handle(MeetingStartPacket packet) {
        if (!isClientInPacketRoom(packet.getRoomId())) {
            return;
        }

        sendPacket(packet);
        currentRoom.broadcast(packet, this);
    }

    @Override
    public void handle(MeetingStopPacket packet) {
        if (!isClientInPacketRoom(packet.getRoomId())) {
            return;
        }

        sendPacket(packet);
        currentRoom.broadcast(packet, this);
    }

    @Override
    public void handle(AudioUdpOfferPacket packet) {
        logger.warn("[handle] Client {} sent unexpected AudioUdpOfferPacket", clientId);
    }

    @Override
    public void handle(AudioUdpAcceptPacket packet) {
        if (!isClientInPacketRoom(packet.getRoomId())) {
            return;
        }

        if (packet.getSessionToken() == null || packet.getSessionToken().isBlank()) {
            logger.warn("[handle] AudioUdpAccept rejected for client {}: missing token", clientId);
            return;
        }

        if (packet.getUdpPort() < 1 || packet.getUdpPort() > 65535) {
            logger.warn("[handle] AudioUdpAccept rejected for client {}: invalid udp port {}", clientId, packet.getUdpPort());
        }
    }

    @Override
    public void handle(AudioStartPacket packet) {
        if (!isClientInPacketRoom(packet.getRoomId()) || !isClientInMeeting(packet.getRoomId())) {
            return;
        }

        sendPacket(packet);
        currentRoom.broadcast(packet, this);
    }

    @Override
    public void handle(AudioStopPacket packet) {
        if (!isClientInPacketRoom(packet.getRoomId()) || !isClientInMeeting(packet.getRoomId())) {
            return;
        }

        sendPacket(packet);
        currentRoom.broadcast(packet, this);
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
        leaveMeetingsExcept(null);
        leaveCurrentRoom();
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            socket.close();
        } catch (Exception e) {
            logger.error("[closeConnection] {}", e.getMessage());
        }
    }

    private void leaveMeetingsExcept(Room excludedRoom) {
        for (Room room : server.getRooms().values()) {
            if (room == excludedRoom) {
                continue;
            }
            leaveMeetingInRoom(room);
        }
    }

    private void leaveMeetingInRoom(Room room) {
        if (room == null || !room.getClientsInMeeting().remove(this)) {
            return;
        }

        server.getAudioSessionRegistry().removeParticipant(room.getRoomId(), clientId);

        AudioStopPacket audioStopPacket = new AudioStopPacket(System.currentTimeMillis(), clientId, room.getRoomId());
        sendPacket(audioStopPacket);
        room.broadcast(audioStopPacket, this);

        LeaveMeetingPacket leavePacket = new LeaveMeetingPacket(System.currentTimeMillis(), clientId, room.getRoomId());
        sendPacket(leavePacket);
        room.broadcast(leavePacket, this);

        if (room.getClientsInMeeting().isEmpty()) {
            MeetingStopPacket stopPacket = new MeetingStopPacket(System.currentTimeMillis(), clientId, room.getRoomId());
            room.broadcast(stopPacket, null);
            sendPacket(stopPacket);
        }
    }

    private boolean isClientInPacketRoom(String roomId) {
        return currentRoom != null && currentRoom.getRoomId().equals(roomId);
    }

    private boolean isClientInMeeting(String roomId) {
        Room room = server.getRoom(roomId);
        if (room == null) {
            return false;
        }

        List<ClientHandler> participants = room.getClientsInMeeting();
        return participants.contains(this);
    }
}