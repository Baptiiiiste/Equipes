package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.enums.PacketType;
import fr.baptiiiiste.common.interfaces.PacketHandler;

public class ScreenShareStopPacket extends Packet {

    public ScreenShareStopPacket(long timestamp, String senderId, String roomId) {
        super(timestamp, senderId, roomId, PacketType.SCREEN_SHARE_STOP);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
