package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.enums.PacketType;
import lombok.Getter;

@Getter
public abstract class Packet {

    private long timestamp;
    private String senderId;
    private PacketType type;

    public Packet(long timestamp, String senderId, PacketType type) {
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.type = type;
    }

    public abstract void execute(PacketHandler handler);
}
