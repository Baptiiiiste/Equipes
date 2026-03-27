package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.enums.PacketType;
import fr.baptiiiiste.common.interfaces.PacketHandler;
import lombok.Getter;

@Getter
public class AudioUdpOfferPacket extends Packet {

    private final int udpPort;
    private final String sessionToken;

    public AudioUdpOfferPacket(long timestamp, String senderId, String roomId, int udpPort, String sessionToken) {
        super(timestamp, senderId, roomId, PacketType.AUDIO_UDP_OFFER);
        this.udpPort = udpPort;
        this.sessionToken = sessionToken;
    }

    @Override
    public void execute(PacketHandler handler) {
        handler.handle(this);
    }
}

