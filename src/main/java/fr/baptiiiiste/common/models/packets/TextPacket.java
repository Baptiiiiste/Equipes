package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.enums.PacketType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextPacket extends Packet {

    private String message;

    public TextPacket(long timestamp, String senderId, String roomId, String message) {
        super(timestamp, senderId, roomId, PacketType.TEXT);
        this.message = message;
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}