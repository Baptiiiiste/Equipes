package fr.baptiiiiste.server.models;

import fr.baptiiiiste.common.models.packets.Packet;
import fr.baptiiiiste.server.handlers.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Room {

    private List<ClientHandler> clientHandlers;
    private String roomName;
    private ClientHandler currentStreamer;

    public Room(String roomName) {
        this.roomName = roomName;
        this.clientHandlers = new ArrayList<>();
        this.currentStreamer = null;
    }

    public void broadcast(Packet packet, ClientHandler sender) {}

    public void addClient(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
    }

    public void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    public synchronized boolean tryStartStreaming(ClientHandler requester) {
        if (currentStreamer == null) {
            currentStreamer = requester;
            return true;
        }
        return false;
    }

    public synchronized void stopStreaming(ClientHandler requester) {
        if (currentStreamer == requester) {
            currentStreamer = null;
        }
    }


    @Override
    public String toString() {
        return roomName;
    }
}
