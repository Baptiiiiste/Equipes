package fr.baptiiiiste.client.ui.core;

import fr.baptiiiiste.client.models.Client;
import fr.baptiiiiste.client.models.ClientRoom;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    @Getter
    private static Client client;

    @Getter
    private static ClientRoom selectedRoom;

    @Getter
    private static String username;

    @Getter
    private static List<ClientRoom> availableRooms = new ArrayList<>();

    public static void initialize(String username, String host, int port) throws Exception {
        SessionManager.username = username;

        availableRooms.add(new ClientRoom("room1", "Général"));
        availableRooms.add(new ClientRoom("room2", "Support Technique"));
        availableRooms.add(new ClientRoom("room3", "Random"));

        client = new Client(host, port);
    }

    public static void setSelectedRoom(ClientRoom room) {
        selectedRoom = room;
    }

    public static void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }
}