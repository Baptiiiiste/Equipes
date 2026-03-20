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

    ///  TODO: Temporary: fast launch
    public MainFrame() {
        this(null, null, -1);
    }

    public MainFrame(String autoUsername, String autoHost, int autoPort) {
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

        boolean autoLoginRequested = autoUsername != null && !autoUsername.isBlank();

        // Login
        if (autoLoginRequested) {
            String host = (autoHost == null || autoHost.isBlank()) ? "localhost" : autoHost;
            int port = autoPort > 0 ? autoPort : 8080;
            if (!initializeSession(autoUsername, host, port)) {
                System.exit(0);
                return;
            }
        } else if (!showLoginDialog()) {
            System.exit(0);
            return;
        }

        // Components
        initUi();

        SessionManager.getUiHandler().setContentPanel(contentPanel);

        setVisible(true);
    }

    private boolean showLoginDialog() {
        LoginDialog dialog = new LoginDialog(this);
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            return false;
        }

        return initializeSession(dialog.getUsername(), dialog.getHost(), dialog.getPort());
    }

    private boolean initializeSession(String username, String host, int port) {
        try {
            SessionManager.initialize(username, host, port);
            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Login error: " + e.getMessage(),
                    "Error",
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