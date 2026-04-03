package fr.baptiiiiste.common.interfaces;

import fr.baptiiiiste.common.models.packets.*;

public interface PacketHandler {
    void handle(TextPacket packet);
    void handle(JoinRoomPacket packet);
    void handle(LeaveRoomPacket packet);
    void handle(JoinMeetingPacket packet);
    void handle(LeaveMeetingPacket packet);
    void handle(MeetingStartPacket packet);
    void handle(MeetingStopPacket packet);
    void handle(AudioUdpOfferPacket packet);
    void handle(AudioUdpAcceptPacket packet);
    void handle(AudioStartPacket packet);
    void handle(AudioStopPacket packet);
    void handle(ScreenShareStartPacket packet);
    void handle(ScreenShareStopPacket packet);
    void handle(ScreenShareFramePacket packet);
}