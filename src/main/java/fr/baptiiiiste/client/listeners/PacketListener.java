package fr.baptiiiiste.client.listeners;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.models.packets.*;

public class PacketListener implements PacketHandler, Runnable {

    @Override
    public void handle(SendTextPacket p) {
        // TODO: Display message in chat UI
    }

    @Override
    public void handle(JoinRoomPacket packet) {
        // TODO: Display new user joined message in chat UI
        // TODO: Display the new user in the user list UI
    }

    @Override
    public void handle(LeaveRoomPacket packet) {
        // TODO: Display user left message in chat UI
        // TODO: Remove the user in the user list UI
    }

    @Override
    public void handle(StartScreenSharePacket packet) {
        // TODO: if current user is streaming, make him stop
        // TODO: Display streamer's screen in the UI
    }

    @Override
    public void handle(StopScreenSharePacket packet) {
        // TODO: Remove streamer's screen from the UI
    }

    @Override
    public void handle(SendScreenSharePacket packet) {
        // TODO: Update streamer's screen in the UI
    }

    @Override
    public void run() {
        // TODO: Continuously listen for incoming packets and handle them
    }
}