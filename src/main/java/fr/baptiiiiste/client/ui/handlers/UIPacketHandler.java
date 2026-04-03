package fr.baptiiiiste.client.ui.handlers;

import fr.baptiiiiste.client.ui.core.SessionManager;
import fr.baptiiiiste.client.ui.panels.MainContentPanel;
import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.models.packets.*;
import lombok.Setter;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

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

    @Override
    public void handle(AudioUdpOfferPacket packet) {
        if (SessionManager.getAudioCallManager() == null) {
            return;
        }

        SessionManager.getAudioCallManager().handleOffer(packet);
    }

    @Override
    public void handle(AudioUdpAcceptPacket packet) {
        // TODO: see if we display sth
    }

    @Override
    public void handle(AudioStartPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.onMeetingAudioStarted(packet.getRoomId(), packet.getSenderId());
        });
    }

    @Override
    public void handle(AudioStopPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.onMeetingAudioStopped(packet.getRoomId(), packet.getSenderId());
        });
    }

    @Override
    public void handle(ScreenShareStartPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.onScreenShareStarted(packet.getRoomId(), packet.getSenderId());
        });
    }

    @Override
    public void handle(ScreenShareStopPacket packet) {
        if (contentPanel == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            contentPanel.onScreenShareStopped(packet.getRoomId(), packet.getSenderId());
        });
    }

    @Override
    public void handle(ScreenShareFramePacket packet) {
        if (contentPanel == null || packet.getImageData() == null || packet.getImageData().length == 0) {
            return;
        }

        try {
            BufferedImage frame = ImageIO.read(new ByteArrayInputStream(packet.getImageData()));
            if (frame == null) {
                return;
            }

            SwingUtilities.invokeLater(() -> {
                contentPanel.onScreenShareFrameReceived(packet.getRoomId(), packet.getSenderId(), frame);
            });
        } catch (Exception ignored) {
            // Ignore malformed frame payloads.
        }
    }

}
