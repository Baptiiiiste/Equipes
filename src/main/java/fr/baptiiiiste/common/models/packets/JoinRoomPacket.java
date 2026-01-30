package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.enums.PacketType;
import lombok.Getter;

@Getter
public class JoinRoomPacket extends Packet {

    public JoinRoomPacket(long timestamp, String senderId, String roomId) {
        super(timestamp, senderId, roomId, PacketType.JOIN_ROOM);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}