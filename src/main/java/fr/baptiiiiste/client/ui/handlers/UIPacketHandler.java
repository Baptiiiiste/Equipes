package fr.baptiiiiste.client.ui.handlers;

import fr.baptiiiiste.client.ui.panels.MainContentPanel;
import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.models.packets.*;
import lombok.Setter;

import javax.swing.*;

@Setter
public class UIPacketHandler implements PacketHandler {

    private MainContentPanel contentPanel;

    @Override
    public void handle(TextPacket packet) {
        if (contentPanel != null) {
            SwingUtilities.invokeLater(() -> {
                contentPanel.getChatPanel().appendMessage(
                        packet.getSenderId(),
                        packet.getMessage()
                );
            });
        }
    }

    @Override
    public void handle(JoinRoomPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.getConnectedUsersSidebarPanel().addUser(packet.getSenderId());
        });
    }

    @Override
    public void handle(LeaveRoomPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.getConnectedUsersSidebarPanel().removeUser(packet.getSenderId());
        });
    }

    @Override
    public void handle(JoinMeetingPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.onMeetingParticipantJoined(packet.getRoomId(), packet.getSenderId());
        });
    }

    @Override
    public void handle(LeaveMeetingPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.onMeetingParticipantLeft(packet.getRoomId(), packet.getSenderId());
        });
    }

    @Override
    public void handle(MeetingStartPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.onMeetingStarted(packet.getRoomId());
        });
    }

    @Override
    public void handle(MeetingStopPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.onMeetingStopped(packet.getRoomId());
        });
    }

}
