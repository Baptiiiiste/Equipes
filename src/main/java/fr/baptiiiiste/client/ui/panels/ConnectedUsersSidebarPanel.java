package fr.baptiiiiste.client.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ConnectedUsersSidebarPanel extends JPanel {

    private final DefaultListModel<String> usersModel;
    private final JLabel titleLabel;

    public ConnectedUsersSidebarPanel() {
        setPreferredSize(new Dimension(250, 0));
        setLayout(new BorderLayout());

        // Background
        Color background = UIManager.getColor("ScrollBar.track");
        setBackground(background);

        // Title
        titleLabel = new JLabel("Connected users (0)", SwingConstants.CENTER);
        titleLabel.setOpaque(false);
        titleLabel.setFont(titleLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(15, 0, 15, 0)
        ));

        // List
        usersModel = new DefaultListModel<>();
        JList<String> usersList = new JList<>(usersModel);
        usersList.setBackground(background);
        usersList.setFocusable(false);

        // Scrollpane
        JScrollPane scrollPane = new JScrollPane(usersList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Add components
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setUsers(Collection<String> users) {
        usersModel.clear();
        for (String user : users) {
            addUser(user);
        }
        refreshTitle();
    }

    public void addUser(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        if (!usersModel.contains(username)) {
            usersModel.addElement(username);
            refreshTitle();
        }
    }

    public void removeUser(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        if (usersModel.removeElement(username)) {
            refreshTitle();
        }
    }

    public void clearUsers() {
        usersModel.clear();
        refreshTitle();
    }

    private void refreshTitle() {
        titleLabel.setText("Connected users (" + usersModel.size() + ")");
    }
}