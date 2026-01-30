package fr.baptiiiiste.common.interfaces;

import fr.baptiiiiste.common.models.packets.*;

public interface PacketHandler {
    void handle(TextPacket packet);
    void handle(JoinRoomPacket packet);
    void handle(LeaveRoomPacket packet);
}