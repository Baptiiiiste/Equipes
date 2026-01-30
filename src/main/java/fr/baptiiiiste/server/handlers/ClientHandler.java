package fr.baptiiiiste.server.handlers;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.models.packets.*;
import fr.baptiiiiste.server.models.Room;
import lombok.Getter;
import lombok.Setter;

import java.net.Socket;

@Setter
@Getter
public class ClientHandler implements PacketHandler, Runnable {

    private Socket socket;
    private Room currentRoom;

    public ClientHandler(Socket socket, Room currentRoom) {
        this.socket = socket;
        this.currentRoom = currentRoom;
    }

    public void run() {}
    public void sendPacket(Packet packet) {}

    @Override
    public void handle(JoinRoomPacket packet) {
        currentRoom.addClient(this);
    }

    @Override
    public void handle(LeaveRoomPacket packet) {
        currentRoom.removeClient(this);
    }

    @Override
    public void handle(SendTextPacket packet) {
        currentRoom.broadcast(packet, this);
    }

    @Override
    public void handle(StartScreenSharePacket packet) {
        currentRoom.tryStartStreaming(this);
    }

    @Override
    public void handle(StopScreenSharePacket packet) {
        currentRoom.stopStreaming(this);
    }

    @Override
    public void handle(SendScreenSharePacket packet) {

    }
}
