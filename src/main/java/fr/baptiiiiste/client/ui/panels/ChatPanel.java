package fr.baptiiiiste.client.ui.panels;

import fr.baptiiiiste.client.models.ClientRoom;
import fr.baptiiiiste.client.ui.core.SessionManager;
import fr.baptiiiiste.common.models.packets.TextPacket;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private ClientRoom currentRoom;

    public ChatPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JLabel headerLabel = new JLabel("Chat");
        headerLabel.setFont(headerLabel.getFont().deriveFont(18f).deriveFont(Font.BOLD));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        messageField = new JTextField();
        sendButton = new JButton("Envoyer");

        messageField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(headerLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    public void setCurrentRoom(ClientRoom room) {
        this.currentRoom = room;
        chatArea.setText("");
        chatArea.append("=== Bienvenue dans " + room.getRoomName() + " ===\n\n");
    }

    private void sendMessage() {
        String message = messageField.getText().trim();

        if (message.isEmpty() || currentRoom == null) {
            return;
        }

        // Envoyer le message au serveur
        TextPacket packet = new TextPacket(
                System.currentTimeMillis(),
                SessionManager.getUsername(),
                currentRoom.getRoomId(),
                message
        );
        SessionManager.getClient().sendPacket(packet);

        // Afficher le message localement
        appendMessage(SessionManager.getUsername(), message);

        messageField.setText("");
    }

    public void appendMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(sender + ": " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
}