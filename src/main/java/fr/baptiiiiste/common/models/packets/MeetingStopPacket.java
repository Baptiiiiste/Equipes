package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.enums.PacketType;
import fr.baptiiiiste.common.interfaces.PacketHandler;

public class MeetingStopPacket extends Packet {

    public MeetingStopPacket(long timestamp, String senderId, String roomId) {
        super(timestamp, senderId, roomId, PacketType.MEETING_STOPPED);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
