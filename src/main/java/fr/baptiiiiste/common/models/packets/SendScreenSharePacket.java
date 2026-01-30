package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.enums.PacketType;

public class SendScreenSharePacket extends Packet {

    private byte[] data;

    public SendScreenSharePacket(long timestamp, String senderId, PacketType type) {
        super(timestamp, senderId, type);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
