package fr.baptiiiiste.client.ui.panels;

import fr.baptiiiiste.client.ui.core.SessionManager;
import fr.baptiiiiste.server.models.Room;

import javax.swing.*;
import java.awt.*;

public class MainContentPanel extends JPanel {
    private JLabel titleLabel;

    public MainContentPanel() {
        setLayout(new BorderLayout());
        titleLabel = new JLabel("Sélectionnez une salle", SwingConstants.CENTER);
        add(titleLabel, BorderLayout.CENTER);
    }

    public void updateDisplay() {
        if (SessionManager.getSelectedRoom() != null) {
            titleLabel.setText("Salle actuelle : " + SessionManager.getSelectedRoom().getRoomName());
        }

        revalidate();
        repaint();
    }

    private void displayChat(Room room) {
        // TODO: Display chat panel
    }

    private void displayMeeting(Room room) {
        // TODO: Display meeting panel

    }

}
