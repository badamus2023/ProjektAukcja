package server;

import server.threads.ServerClientThread;
import utils.config.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    static public List<ServerClientThread> clientList;

    static {
        clientList = new ArrayList<>();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(Config.port);

            while(true) {
                Socket socket = serverSocket.accept();
                ServerClientThread serverClient = new ServerClientThread(socket);

                serverClient.start();
                clientList.add(serverClient);

                System.out.println("Nowy klient połączony");
            }
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }
}

