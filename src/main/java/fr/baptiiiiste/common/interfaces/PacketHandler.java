package fr.baptiiiiste.common.interfaces;

import fr.baptiiiiste.common.models.packets.*;

public interface PacketHandler {
    // Room join/left packets
    void handle(JoinRoomPacket packet);
    void handle(LeaveRoomPacket packet);

    // Text message packets
    void handle(SendTextPacket packet);

    // Screen sharing packets
    void handle(StartScreenSharePacket packet);
    void handle(StopScreenSharePacket packet);
    void handle(SendScreenSharePacket packet);

    // Camera control packets
    // TODO

    // File transfer packets
    // TODO

    // Voice communication packets
    // TODO
}
