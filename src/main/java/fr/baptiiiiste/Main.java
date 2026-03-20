package fr.baptiiiiste;

import com.formdev.flatlaf.FlatDarculaLaf;
import fr.baptiiiiste.client.ui.MainFrame;
import fr.baptiiiiste.server.models.Server;
import fr.baptiiiiste.server.persistence.ChatRepository;
import fr.baptiiiiste.server.persistence.DatabaseConfig;
import fr.baptiiiiste.server.persistence.JdbcChatRepository;
import fr.baptiiiiste.server.persistence.JdbcRoomRepository;
import fr.baptiiiiste.server.persistence.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Scanner;

@Slf4j
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    ///  Main function, select whether you're a server or a client and start the corresponding part of the application.
    public static void main(String[] args) {

        // TODO: Temporary: fast launch
        if (args.length > 0) {
            launchFromArgs(args);
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Teams-like Application ===");
        System.out.println("1. Start server");
        System.out.println("2. Start client");
        System.out.print("Choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            startServer();
        } else if (choice == 2) {
            startClient();
        } else {
            System.out.println("Invalid choice");
        }

        scanner.close();
    }

    /// TODO: Temporary: fast launch
    private static void launchFromArgs(String[] args) {
        String mode = args[0].trim().toLowerCase();

        if ("server".equals(mode)) {
            startServer();
            return;
        }

        if ("client".equals(mode)) {
            if (args.length >= 4) {
                String username = args[1];
                String host = args[2];
                int port;

                try {
                    port = Integer.parseInt(args[3]);
                } catch (NumberFormatException exception) {
                    logger.error("[launchFromArgs] Invalid client port: {}", args[3]);
                    return;
                }

                startClient(username, host, port);
            } else {
                startClient();
            }
            return;
        }

        logger.error("[launchFromArgs] Unknown mode: {} (expected 'server' or 'client')", args[0]);
    }

    /// Start the server
    private static void startServer() {
        System.out.println("\n=== Starting server ===");
        int serverPort = readServerPort();

        Server server;

        try {
            DatabaseConfig databaseConfig = DatabaseConfig.fromEnvironment();
            databaseConfig.migrate();

            RoomRepository roomRepository = new JdbcRoomRepository(databaseConfig);
            ChatRepository chatRepository = new JdbcChatRepository(databaseConfig);
            server = new Server(serverPort, roomRepository, chatRepository);
            server.loadRoomsFromStorage();
            logger.info("[startServer] PostgreSQL connected");
        } catch (Exception exception) {
            logger.error("[startServer] PostgresSQL unavailable", exception);
            return;
        }

        seedDefaultRoomsIfMissing(server);

        logger.info("[startServer] Server started, listening on port {}", serverPort);
        server.start();
    }

    /// Reads the server port from the APP_SERVER_PORT environment variable, with validation and fallback to 8080.
    private static int readServerPort() {
        String rawPort = System.getenv("APP_SERVER_PORT");
        if (rawPort == null || rawPort.isBlank()) {
            return 8080;
        }

        try {
            int parsedPort = Integer.parseInt(rawPort);
            if (parsedPort < 1 || parsedPort > 65535) {
                logger.error("[readServerPort] Invalid port number (APP_SERVER_PORT), using default port 8080");
                return 8080;
            }
            return parsedPort;
        } catch (NumberFormatException exception) {
            logger.error("[readServerPort] Invalid format (APP_SERVER_PORT), using default port 8080");
            return 8080;
        }
    }

    /// Generate some rooms 
    private static void seedDefaultRoomsIfMissing(Server server) {
        server.createRoom("room1", "Général");
        server.createRoom("room2", "Support Technique");
        server.createRoom("room3", "Random");
    }

    /// Client initialization with FlatLaf for better UI appearance
    private static void startClient() {
        startClient(null, null, -1);
    }

    private static void startClient(String username, String host, int port) {
        System.out.println("\n=== Starting client ===");

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception e) {
            logger.error("[startClient] Failed to initialize FlatLaf, using default Look & Feel", e);
        }

        SwingUtilities.invokeLater(() -> new MainFrame(username, host, port));
    }
}