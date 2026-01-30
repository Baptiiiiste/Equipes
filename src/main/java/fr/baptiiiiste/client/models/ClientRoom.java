package fr.baptiiiiste.client.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRoom {
    private String roomId;
    private String roomName;
    private int memberCount;

    public ClientRoom(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.memberCount = 0;
    }

    @Override
    public String toString() {
        return roomName;
    }
}