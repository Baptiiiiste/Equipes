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

}
