package client.views;

import client.network.threads.ClientThread;
import com.fasterxml.jackson.core.JsonProcessingException;
import utils.message.Message;
import utils.message.MessageType;
import utils.data.LoginRequest;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private ClientThread client;

    public LoginView(ClientThread client) {
        this.client = client;

        setTitle("Logowanie");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JLabel userLabel = new JLabel("Login:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Hasło:");
        JPasswordField passField = new JPasswordField();
        JButton loginButton = new JButton("Zaloguj");

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            client.setLoginSuccessCallback(this::openMainView);
            client.setLoginFailureCallback(() ->
                    JOptionPane.showMessageDialog(this, "Nieprawidłowy login lub hasło")
            );

            try {
                client.sendMessage(new Message<LoginRequest>(MessageType.AUTHENTICATE, new LoginRequest(username, password)));
            } catch(JsonProcessingException j) {
                j.printStackTrace();
            }
        });

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(new JLabel());
        panel.add(loginButton);

        setContentPane(panel);
    }

    private void openMainView() {
        this.dispose();

        SwingUtilities.invokeLater(() -> {
            MainView mainView = new MainView(client);
            mainView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainView.setVisible(true);
        });
    }
}
