package fr.baptiiiiste.client.ui.panels;

import lombok.Setter;

import javax.swing.*;
import java.awt.*;

public class MeetingControlsPanel extends JPanel {

    private final JLabel statusLabel;
    private final JButton primaryActionButton;
    private final JButton shareButton;
    private final JButton leaveButton;

    @Setter
    private Runnable onPrimaryAction;
    @Setter
    private Runnable onShareAction;
    @Setter
    private Runnable onLeaveAction;

    public MeetingControlsPanel() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Participants: 0");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        primaryActionButton = new JButton("Lancer la reunion");
        shareButton = new JButton("Partager");
        leaveButton = new JButton("Quitter la reunion");

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.add(primaryActionButton);
        actionsPanel.add(shareButton);
        actionsPanel.add(leaveButton);

        add(statusLabel, BorderLayout.WEST);
        add(actionsPanel, BorderLayout.EAST);

        primaryActionButton.addActionListener(e -> {
            if (onPrimaryAction != null) {
                onPrimaryAction.run();
            }
        });

        shareButton.addActionListener(e -> {
            if (onShareAction != null) {
                onShareAction.run();
            }
        });

        leaveButton.addActionListener(e -> {
            if (onLeaveAction != null) {
                onLeaveAction.run();
            }
        });

        setRoomSelected(false);
    }

    public void setRoomSelected(boolean roomSelected) {
        setVisible(roomSelected);
        if (!roomSelected) {
            primaryActionButton.setEnabled(false);
            shareButton.setEnabled(false);
            leaveButton.setEnabled(false);
            primaryActionButton.setVisible(true);
            shareButton.setVisible(false);
            leaveButton.setVisible(false);
            statusLabel.setText("Participants: 0");
        }
    }

    public void updateState(boolean meetingActive, boolean currentUserInMeeting, int participantCount, String activeSharerId, String currentUsername) {
        int safeCount = Math.max(0, participantCount);
        String shareStatus = activeSharerId == null || activeSharerId.isBlank()
                ? "Aucun partage"
                : "Partage: " + activeSharerId;
        statusLabel.setText("Participants: " + safeCount + " | " + shareStatus);

        if (currentUserInMeeting) {
            primaryActionButton.setVisible(false);
            shareButton.setVisible(true);
            leaveButton.setVisible(true);
            shareButton.setEnabled(true);
            leaveButton.setEnabled(true);
            boolean currentUserSharing = currentUsername != null && currentUsername.equals(activeSharerId);
            shareButton.setText(currentUserSharing ? "Arreter le partage" : "Partager");
            return;
        }

        primaryActionButton.setVisible(true);
        shareButton.setVisible(false);
        leaveButton.setVisible(false);
        shareButton.setEnabled(false);
        leaveButton.setEnabled(false);
        primaryActionButton.setEnabled(true);
        primaryActionButton.setText(meetingActive ? "Rejoindre la reunion" : "Lancer la reunion");
    }
}


