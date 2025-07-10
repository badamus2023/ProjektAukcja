package server;

import server.threads.AuctionWatcherThread;

public class ServerMain {
    public static void main(String[] args) {
        AuctionWatcherThread watcher = new AuctionWatcherThread();
        watcher.start();

        Server server = new Server();
        server.run();
    }
}
