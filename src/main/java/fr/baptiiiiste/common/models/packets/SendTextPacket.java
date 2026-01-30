package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.enums.PacketType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendTextPacket extends Packet {

    String message;

    public SendTextPacket(long timestamp, String senderId, PacketType type, String message) {
        super(timestamp, senderId, type);
        this.message = message;
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
