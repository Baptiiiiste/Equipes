package fr.baptiiiiste.client.ui.panels;

import fr.baptiiiiste.client.ui.listeners.RoomSelectionListener;
import fr.baptiiiiste.common.config.Config;
import fr.baptiiiiste.server.models.Room;

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
        JLabel titleLabel = new JLabel("Rooms", SwingConstants.CENTER);
        titleLabel.setOpaque(false); // Le label laisse passer le fond du panel
        titleLabel.setFont(titleLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(15, 0, 15, 0)
        ));

        // List
        DefaultListModel<Room> roomsModel = new DefaultListModel<>();
        roomsModel.addAll(Config.getServer().getRooms());
        JList<Room> myList = new JList<>(roomsModel);
        myList.setBackground(background);
        myList.setFocusable(false);

        myList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listener != null) {
                listener.onRoomSelected(myList.getSelectedValue());
            }
        });

        // Scrollpane
        JScrollPane scrollPane = new JScrollPane(myList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0 , 0, 0));
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