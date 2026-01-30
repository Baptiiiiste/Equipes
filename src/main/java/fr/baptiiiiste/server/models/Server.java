package fr.baptiiiiste.server.models;

import fr.baptiiiiste.server.handlers.ClientHandler;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private int port;
    private Map<String, Room> rooms;
    private ExecutorService threadPool;
    private boolean running;

    public Server(int port) {
        this.port = port;
        this.rooms = new HashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.running = false;
    }

    public void start() {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("[start] Server started on port " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                logger.info("[start] New client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            logger.error("[start] " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Room createRoom(String roomId, String roomName) {
        Room room = new Room(roomId, roomName);
        rooms.put(roomId, room);
        return room;
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }
}