package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.enums.PacketType;
import fr.baptiiiiste.common.interfaces.PacketHandler;

public class AudioStartPacket extends Packet {

    public AudioStartPacket(long timestamp, String senderId, String roomId) {
        super(timestamp, senderId, roomId, PacketType.AUDIO_START);
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}

