package fr.baptiiiiste.client.ui.core;

import fr.baptiiiiste.client.audio.AudioCallManager;
import fr.baptiiiiste.client.models.Client;
import fr.baptiiiiste.client.models.ClientRoom;
import fr.baptiiiiste.client.screen.ScreenShareManager;
import fr.baptiiiiste.client.ui.handlers.UIPacketHandler;
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

    @Getter
    private static UIPacketHandler uiHandler;

    @Getter
    private static AudioCallManager audioCallManager;

    @Getter
    private static ScreenShareManager screenShareManager;


    public static void initialize(String username, String host, int port) throws Exception {
        SessionManager.username = username;
        availableRooms.clear();

        availableRooms.add(new ClientRoom("room1", "Général"));
        availableRooms.add(new ClientRoom("room2", "Support Technique"));
        availableRooms.add(new ClientRoom("room3", "Random"));

        uiHandler = new UIPacketHandler();

        client = new Client(host, port);
        client.connect(uiHandler);
        audioCallManager = new AudioCallManager(client, username);
        screenShareManager = new ScreenShareManager(client, username);
    }

    public static void setSelectedRoom(ClientRoom room) {
        selectedRoom = room;
    }

    public static void disconnect() {
        if (audioCallManager != null) {
            audioCallManager.shutdown();
        }
        if (screenShareManager != null) {
            screenShareManager.shutdown();
        }
        if (client != null) {
            client.disconnect();
        }
    }
}