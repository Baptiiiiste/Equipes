package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.enums.PacketType;
import fr.baptiiiiste.common.interfaces.PacketHandler;

public class LeaveMeetingPacket extends Packet {

    public LeaveMeetingPacket(long timestamp, String senderId, String roomId) {
        super(timestamp, senderId, roomId, PacketType.LEAVE_MEETING);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
