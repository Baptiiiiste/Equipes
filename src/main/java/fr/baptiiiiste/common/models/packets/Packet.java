package fr.baptiiiiste.common.models.packets;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.enums.PacketType;
import lombok.Getter;

import java.io.Serializable;

@Getter
public abstract class Packet implements Serializable {

    private long timestamp;
    private String senderId;
    private String roomId;
    private PacketType type;

    public Packet(long timestamp, String senderId, String roomId, PacketType type) {
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.roomId = roomId;
        this.type = type;
    }

    public abstract void execute(PacketHandler handler);
}