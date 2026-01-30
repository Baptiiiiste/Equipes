package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.enums.PacketType;

public class LeaveRoomPacket extends Packet {

    public LeaveRoomPacket(long timestamp, String senderId, PacketType type) {
        super(timestamp, senderId, type);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
