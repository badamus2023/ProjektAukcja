package client;

import client.network.ConnectionManager;
import client.views.LoginView;

import java.io.IOException;

public class Client {

    public static void main(String[] args) {

        ConnectionManager manager = new ConnectionManager();
        try {
            manager.connect();
            LoginView loginView = new LoginView(manager.getClient());
            loginView.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
