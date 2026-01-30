package fr.baptiiiiste.client.ui.panels;

import fr.baptiiiiste.client.models.ClientRoom;
import fr.baptiiiiste.client.ui.listeners.RoomSelectionListener;
import fr.baptiiiiste.client.ui.core.SessionManager;

import javax.swing.*;
import java.awt.*;

public class RoomSidebarPanel extends JPanel {

    private RoomSelectionListener listener;

    public RoomSidebarPanel() {
        setPreferredSize(new Dimension(250, 0));
        setLayout(new BorderLayout());

        // Background
        Color background = UIManager.getColor("ScrollBar.track");
        setBackground(background);

        // Title
        JLabel titleLabel = new JLabel("Salles", SwingConstants.CENTER);
        titleLabel.setOpaque(false);
        titleLabel.setFont(titleLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(15, 0, 15, 0)
        ));

        // List
        DefaultListModel<ClientRoom> roomsModel = new DefaultListModel<>();
        roomsModel.addAll(SessionManager.getAvailableRooms());
        JList<ClientRoom> myList = new JList<>(roomsModel);
        myList.setBackground(background);
        myList.setFocusable(false);

        myList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listener != null) {
                listener.onRoomSelected(myList.getSelectedValue());
            }
        });

        // Scrollpane
        JScrollPane scrollPane = new JScrollPane(myList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Add components
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setOnRoomSelected(RoomSelectionListener listener) {
        this.listener = listener;
    }
}