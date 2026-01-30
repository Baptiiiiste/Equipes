package fr.baptiiiiste.client.ui;

import com.formdev.flatlaf.util.SystemInfo;
import fr.baptiiiiste.client.ui.panels.MainContentPanel;
import fr.baptiiiiste.client.ui.panels.RoomSidebarPanel;
import fr.baptiiiiste.client.ui.core.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MainFrame extends JFrame {

    public MainFrame() {

        setTitle("Equipes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);

        // Icon
        URL iconURL = getClass().getResource("/images/logo.png");
        ImageIcon icon = new ImageIcon(iconURL);
        if (SystemInfo.isMacOS) {
            Taskbar.getTaskbar().setIconImage(icon.getImage());
        } else {
            setIconImage(icon.getImage());
        }

        // Components
        initUi();

    }

    public void initUi() {
        RoomSidebarPanel sidebar = new RoomSidebarPanel();
        MainContentPanel content = new MainContentPanel();

        sidebar.setOnRoomSelected(room -> {
            SessionManager.setSelectedRoom(room);
            content.updateDisplay();
        });

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

}