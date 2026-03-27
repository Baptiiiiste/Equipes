package fr.baptiiiiste.client.ui.panels;

import fr.baptiiiiste.client.models.ClientRoom;
import fr.baptiiiiste.client.ui.core.SessionManager;
import fr.baptiiiiste.common.models.packets.TextPacket;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {

    private final JTextArea chatArea;
    private final JTextField messageField;
    private final JButton sendButton;
    private final JLabel headerLabel;
    private final JScrollPane scrollPane;
    private final JPanel inputPanel;
    private ClientRoom currentRoom;

    public ChatPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        headerLabel = new JLabel("Chat");

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        scrollPane = new JScrollPane(chatArea);

        // Input panel
        inputPanel = new JPanel(new BorderLayout(5, 0));
        messageField = new JTextField();
        sendButton = new JButton("Send");

        messageField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(headerLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setMeetingStyle(false);
    }

    public void setCurrentRoom(ClientRoom room) {
        this.currentRoom = room;
        chatArea.setText("");
    }

    private void sendMessage() {
        String message = messageField.getText().trim();

        if (message.isEmpty() || currentRoom == null) {
            return;
        }

        // Send the message packet to the server
        TextPacket packet = new TextPacket(
                System.currentTimeMillis(),
                SessionManager.getUsername(),
                currentRoom.getRoomId(),
                message
        );
        SessionManager.getClient().sendPacket(packet);

        // Display the message in the chat area
        appendMessage(SessionManager.getUsername(), message);

        messageField.setText("");
    }

    public void appendMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(sender + ": " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void setMeetingStyle(boolean meetingStyle) {
        if (meetingStyle) {
            Color background = UIManager.getColor("ScrollBar.track");
            Color borderColor = UIManager.getColor("Component.borderColor");

            setBorder(BorderFactory.createEmptyBorder());
            setBackground(background);

            headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            headerLabel.setOpaque(false);
            headerLabel.setFont(headerLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
            headerLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(2, 0, 1, 0, borderColor),
                    BorderFactory.createEmptyBorder(15, 0, 15, 0)
            ));

            chatArea.setBackground(background);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            inputPanel.setOpaque(false);
            return;
        }

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(UIManager.getColor("Panel.background"));

        headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        headerLabel.setOpaque(false);
        headerLabel.setFont(headerLabel.getFont().deriveFont(18f).deriveFont(Font.BOLD));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        chatArea.setBackground(UIManager.getColor("TextArea.background"));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setOpaque(true);
        inputPanel.setBorder(BorderFactory.createEmptyBorder());
        inputPanel.setOpaque(true);
    }
}