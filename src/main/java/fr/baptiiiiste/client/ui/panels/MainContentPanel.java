package fr.baptiiiiste.client.ui.panels;

import fr.baptiiiiste.client.models.ClientRoom;
import fr.baptiiiiste.client.ui.core.SessionManager;
import fr.baptiiiiste.common.models.packets.JoinRoomPacket;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class MainContentPanel extends JPanel {
    private JLabel titleLabel;

    @Getter
    private ChatPanel chatPanel;

    @Getter
    private ConnectedUsersSidebarPanel connectedUsersSidebarPanel;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public MainContentPanel() {
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Empty panel
        JPanel emptyPanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("Choose a room", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
        emptyPanel.add(titleLabel, BorderLayout.CENTER);

        // Chat panel
        chatPanel = new ChatPanel();
        connectedUsersSidebarPanel = new ConnectedUsersSidebarPanel();
        connectedUsersSidebarPanel.setVisible(false);

        contentPanel.add(emptyPanel, "EMPTY");
        contentPanel.add(chatPanel, "CHAT");

        add(contentPanel, BorderLayout.CENTER);
        add(connectedUsersSidebarPanel, BorderLayout.EAST);
        cardLayout.show(contentPanel, "EMPTY");
    }

    public void updateDisplay() {
        ClientRoom selectedRoom = SessionManager.getSelectedRoom();

        if (selectedRoom != null) {
            connectedUsersSidebarPanel.clearUsers();

            JoinRoomPacket joinPacket = new JoinRoomPacket(
                    System.currentTimeMillis(),
                    SessionManager.getUsername(),
                    selectedRoom.getRoomId()
            );
            SessionManager.getClient().sendPacket(joinPacket);

            chatPanel.setCurrentRoom(selectedRoom);
            cardLayout.show(contentPanel, "CHAT");
            connectedUsersSidebarPanel.setVisible(true);
        } else {
            cardLayout.show(contentPanel, "EMPTY");
            connectedUsersSidebarPanel.setVisible(false);
        }

        revalidate();
        repaint();
    }
}