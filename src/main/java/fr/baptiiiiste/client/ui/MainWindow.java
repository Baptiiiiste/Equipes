package fr.baptiiiiste.client.ui;

import com.formdev.flatlaf.util.SystemInfo;
import fr.baptiiiiste.client.ui.components.RoomSidebarPanel;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MainWindow extends JFrame {

    public MainWindow() {

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
        add(new RoomSidebarPanel(), BorderLayout.WEST);
    }

}