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

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public MainContentPanel() {
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Panel vide
        JPanel emptyPanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("Sélectionnez une salle", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
        emptyPanel.add(titleLabel, BorderLayout.CENTER);

        // Chat panel
        chatPanel = new ChatPanel();

        contentPanel.add(emptyPanel, "EMPTY");
        contentPanel.add(chatPanel, "CHAT");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "EMPTY");
    }

    public void updateDisplay() {
        ClientRoom selectedRoom = SessionManager.getSelectedRoom();

        if (selectedRoom != null) {
            // Envoyer le packet JOIN_ROOM au serveur
            JoinRoomPacket joinPacket = new JoinRoomPacket(
                    System.currentTimeMillis(),
                    SessionManager.getUsername(),
                    selectedRoom.getRoomId()
            );
            SessionManager.getClient().sendPacket(joinPacket);

            // Afficher le chat
            chatPanel.setCurrentRoom(selectedRoom);
            cardLayout.show(contentPanel, "CHAT");
        } else {
            cardLayout.show(contentPanel, "EMPTY");
        }

        revalidate();
        repaint();
    }
}