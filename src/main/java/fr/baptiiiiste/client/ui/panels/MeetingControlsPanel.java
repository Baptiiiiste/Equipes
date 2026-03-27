package fr.baptiiiiste.client.ui.panels;

import lombok.Setter;

import javax.swing.*;
import java.awt.*;

public class MeetingControlsPanel extends JPanel {

    private final JLabel statusLabel;
    private final JButton primaryActionButton;
    private final JButton leaveButton;

    @Setter
    private Runnable onPrimaryAction;
    @Setter
    private Runnable onLeaveAction;

    public MeetingControlsPanel() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Participants: 0");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        primaryActionButton = new JButton("Lancer la reunion");
        leaveButton = new JButton("Quitter la reunion");

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.add(primaryActionButton);
        actionsPanel.add(leaveButton);

        add(statusLabel, BorderLayout.WEST);
        add(actionsPanel, BorderLayout.EAST);

        primaryActionButton.addActionListener(e -> {
            if (onPrimaryAction != null) {
                onPrimaryAction.run();
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
            leaveButton.setEnabled(false);
            primaryActionButton.setVisible(true);
            leaveButton.setVisible(false);
            statusLabel.setText("Participants: 0");
        }
    }

    public void updateState(boolean meetingActive, boolean currentUserInMeeting, int participantCount) {
        int safeCount = Math.max(0, participantCount);
        statusLabel.setText("Participants: " + safeCount);

        if (currentUserInMeeting) {
            primaryActionButton.setVisible(false);
            leaveButton.setVisible(true);
            leaveButton.setEnabled(true);
            return;
        }

        primaryActionButton.setVisible(true);
        leaveButton.setVisible(false);
        leaveButton.setEnabled(false);
        primaryActionButton.setEnabled(true);
        primaryActionButton.setText(meetingActive ? "Rejoindre la reunion" : "Lancer la reunion");
    }
}


