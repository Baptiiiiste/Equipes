package fr.baptiiiiste.server.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Server {

    private int port;
    private List<Room> rooms;

    public Server(int port, List<Room> rooms) {
        this.port = port;
        this.rooms = rooms;
    }
}
