package fr.baptiiiiste.common.config;

import fr.baptiiiiste.server.models.Room;
import fr.baptiiiiste.server.models.Server;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Config {

    @Getter
    private static Server server = new Server(5000, List.of(
            new Room("General"),
            new Room("General 2"),
            new Room("General 3"),
            new Room("General 4"),
            new Room("General 5")
    ));

}
