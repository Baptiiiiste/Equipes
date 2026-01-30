package fr.baptiiiiste.client.ui;

import com.formdev.flatlaf.util.SystemInfo;
import fr.baptiiiiste.client.ui.dialogs.LoginDialog;
import fr.baptiiiiste.client.ui.panels.MainContentPanel;
import fr.baptiiiiste.client.ui.panels.RoomSidebarPanel;
import fr.baptiiiiste.client.ui.core.SessionManager;
import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.models.packets.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

public class MainFrame extends JFrame {

    private MainContentPanel contentPanel;

    public MainFrame() {
        setTitle("Equipes");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);

        // Icon
        URL iconURL = getClass().getResource("/images/logo.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            if (SystemInfo.isMacOS) {
                Taskbar.getTaskbar().setIconImage(icon.getImage());
            } else {
                setIconImage(icon.getImage());
            }
        }

        // Window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });

        // Login
        if (!showLoginDialog()) {
            System.exit(0);
            return;
        }

        // Components
        initUi();
        setVisible(true);
    }

    private boolean showLoginDialog() {
        LoginDialog dialog = new LoginDialog(this);
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            return false;
        }

        try {
            SessionManager.initialize(
                    dialog.getUsername(),
                    dialog.getHost(),
                    dialog.getPort()
            );

            // Configurer le UI handler pour recevoir les messages
            PacketHandler uiHandler = new PacketHandler() {
                @Override
                public void handle(TextPacket packet) {
                    if (contentPanel != null) {
                        contentPanel.getChatPanel().appendMessage(
                                packet.getSenderId(),
                                packet.getMessage()
                        );
                    }
                }

                @Override
                public void handle(JoinRoomPacket packet) {
                    // Rien pour l'instant
                }

                @Override
                public void handle(LeaveRoomPacket packet) {
                    // Rien pour l'instant
                }
            };

            SessionManager.getClient().connect(uiHandler);
            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erreur de connexion: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    public void initUi() {
        RoomSidebarPanel sidebar = new RoomSidebarPanel();
        contentPanel = new MainContentPanel();

        sidebar.setOnRoomSelected(room -> {
            SessionManager.setSelectedRoom(room);
            contentPanel.updateDisplay();
        });

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void disconnect() {
        SessionManager.disconnect();
        dispose();
        System.exit(0);
    }
}