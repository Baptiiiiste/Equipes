package fr.baptiiiiste.client.ui.dialogs;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JTextField hostField;
    private JTextField portField;
    @Getter
    private boolean confirmed = false;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);

        // Host
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Password:"), gbc);

        hostField = new JTextField("localhost", 20);
        gbc.gridx = 1;
        mainPanel.add(hostField, gbc);

        // Port
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Port:"), gbc);

        portField = new JTextField("8080", 20);
        gbc.gridx = 1;
        mainPanel.add(portField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton connectButton = new JButton("Log in");
        JButton cancelButton = new JButton("Cancel");

        connectButton.addActionListener(e -> {
            if (validateInputs()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(connectButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private boolean validateInputs() {
        if (usernameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid port number",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getHost() {
        return hostField.getText().trim();
    }

    public int getPort() {
        return Integer.parseInt(portField.getText().trim());
    }
}