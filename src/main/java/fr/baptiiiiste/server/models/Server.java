package fr.baptiiiiste.server.models;

import fr.baptiiiiste.server.handlers.ClientHandler;
import fr.baptiiiiste.server.audio.AudioRelayServer;
import fr.baptiiiiste.server.audio.AudioSessionRegistry;
import fr.baptiiiiste.server.persistence.ChatRepository;
import fr.baptiiiiste.server.persistence.RoomRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private int port;
    private Map<String, Room> rooms;
    private RoomRepository roomRepository;
    private ChatRepository chatRepository;
    private ExecutorService threadPool;
    private boolean running;
    private int audioUdpPort;
    private AudioSessionRegistry audioSessionRegistry;
    private AudioRelayServer audioRelayServer;
    private Thread audioRelayThread;

    public Server(int port) {
        this(port, null, null);
    }

    public Server(int port, RoomRepository roomRepository) {
        this(port, roomRepository, null);
    }

    public Server(int port, RoomRepository roomRepository, ChatRepository chatRepository) {
        this.port = port;
        this.rooms = new HashMap<>();
        this.roomRepository = roomRepository;
        this.chatRepository = chatRepository;
        this.threadPool = Executors.newCachedThreadPool();
        this.running = false;
        this.audioUdpPort = resolveAudioUdpPort(port);
        this.audioSessionRegistry = new AudioSessionRegistry();
    }

    public void loadRoomsFromStorage() {
        if (roomRepository == null) {
            return;
        }

        List<Room> persistedRooms = roomRepository.findAllRooms();
        for (Room room : persistedRooms) {
            room.setChatRepository(chatRepository);
            rooms.put(room.getRoomId(), room);
        }
        logger.info("[loadRoomsFromStorage] Loaded {} room(s) from storage", persistedRooms.size());
    }

    public void start() {
        running = true;
        startAudioRelay();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("[start] Server started on port {}", port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                logger.info("[start] New client connected: {}", clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            logger.error("[start] {}", e.getMessage());
        }
    }

    public void stop() {
        running = false;
        if (audioRelayServer != null) {
            audioRelayServer.stop();
        }
        if (audioRelayThread != null) {
            audioRelayThread.interrupt();
        }
        threadPool.shutdown();
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Room createRoom(String roomId, String roomName) {
        if (roomExists(roomId)) {
            return rooms.get(roomId);
        }

        Room room = new Room(roomId, roomName, chatRepository);

        if (roomRepository != null) {
            roomRepository.saveRoom(room);
        }

        rooms.put(roomId, room);
        return room;
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    private int resolveAudioUdpPort(int serverPort) {
        String rawPort = System.getenv("APP_AUDIO_UDP_PORT");
        if (rawPort != null && !rawPort.isBlank()) {
            try {
                int parsed = Integer.parseInt(rawPort);
                if (parsed >= 1 && parsed <= 65535) {
                    return parsed;
                }
            } catch (NumberFormatException exception) {
                logger.warn("[resolveAudioUdpPort] Invalid APP_AUDIO_UDP_PORT, using fallback");
            }
        }

        int fallback = serverPort + 1;
        if (fallback > 65535) {
            return 65535;
        }
        return fallback;
    }

    private void startAudioRelay() {
        audioRelayServer = new AudioRelayServer(audioUdpPort, audioSessionRegistry);
        audioRelayThread = new Thread(audioRelayServer, "audio-udp-relay");
        audioRelayThread.start();
    }
}