package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.enums.PacketType;
import fr.baptiiiiste.common.interfaces.PacketHandler;

public class JoinMeetingPacket extends Packet {

    public JoinMeetingPacket(long timestamp, String senderId, String roomId) {
        super(timestamp, senderId, roomId, PacketType.JOIN_MEETING);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
