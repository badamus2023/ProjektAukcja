package client.network;

import client.network.threads.ClientThread;
import utils.config.Config;

import java.io.IOException;
import java.net.Socket;

public class ConnectionManager {
    private ClientThread client;
    private Socket socket;

    public void connect() throws IOException {
        this.socket = new Socket(Config.host, Config.port);
        this.client = new ClientThread(socket);

        client.start();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public ClientThread getClient() {
        return client;
    }
}
