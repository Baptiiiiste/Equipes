package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.enums.PacketType;
import fr.baptiiiiste.common.interfaces.PacketHandler;

public class MeetingStartPacket extends Packet {

    public MeetingStartPacket(long timestamp, String senderId, String roomId) {
        super(timestamp, senderId, roomId, PacketType.MEETING_STARTED);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
