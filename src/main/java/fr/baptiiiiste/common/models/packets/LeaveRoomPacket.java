package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.enums.PacketType;
import lombok.Getter;

@Getter
public class LeaveRoomPacket extends Packet {

    public LeaveRoomPacket(long timestamp, String senderId, String roomId) {
        super(timestamp, senderId, roomId, PacketType.LEAVE_ROOM);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}