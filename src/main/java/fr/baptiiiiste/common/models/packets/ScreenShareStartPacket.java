package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.enums.PacketType;
import fr.baptiiiiste.common.interfaces.PacketHandler;

public class ScreenShareStartPacket extends Packet {

    public ScreenShareStartPacket(long timestamp, String senderId, String roomId) {
        super(timestamp, senderId, roomId, PacketType.SCREEN_SHARE_START);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
