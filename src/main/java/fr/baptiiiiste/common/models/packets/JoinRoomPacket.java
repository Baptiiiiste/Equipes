package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.enums.PacketType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRoomPacket extends Packet {

    private String roomName;

    public JoinRoomPacket(long timestamp, String senderId, PacketType type, String roomName) {
        super(timestamp, senderId, type);
        this.roomName = roomName;
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
