package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.enums.PacketType;
import fr.baptiiiiste.common.interfaces.PacketHandler;
import lombok.Getter;

@Getter
public class ScreenShareFramePacket extends Packet {

    private final byte[] imageData;
    private final int imageWidth;
    private final int imageHeight;

    public ScreenShareFramePacket(long timestamp, String senderId, String roomId, byte[] imageData, int imageWidth, int imageHeight) {
        super(timestamp, senderId, roomId, PacketType.SCREEN_SHARE_FRAME);
        this.imageData = imageData;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}
