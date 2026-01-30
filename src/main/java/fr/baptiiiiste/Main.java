package fr.baptiiiiste;

import com.formdev.flatlaf.FlatDarculaLaf;
import fr.baptiiiiste.client.ui.MainFrame;
import fr.baptiiiiste.server.models.Server;

import javax.swing.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
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

    private static void startServer() {
        System.out.println("\n=== Starting server ===");

        Server server = new Server(8080);
        server.createRoom("room1", "Général");
        server.createRoom("room2", "Support Technique");
        server.createRoom("room3", "Random");

        System.out.println("Rooms created:");
        server.getRooms().values().forEach(room ->
                System.out.println("  - " + room.getRoomName() + " (ID: " + room.getRoomId() + ")")
        );

        System.out.println("\nServer listening on port 8080...");
        server.start();
    }

    private static void startClient() {
        System.out.println("\n=== Starting client ===");

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf, using default Look & Feel");
        }

        SwingUtilities.invokeLater(() -> {
            new MainFrame();
        });
    }
}