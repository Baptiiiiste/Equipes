package fr.baptiiiiste.server.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Server {

    private int port;
    private Map<String, Room> rooms;

}
