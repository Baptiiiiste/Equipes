package fr.baptiiiiste;

import com.formdev.flatlaf.FlatDarculaLaf;
import fr.baptiiiiste.client.ui.MainFrame;

import javax.swing.*;

public class Main {
    static void main() {

        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });

    }
}
