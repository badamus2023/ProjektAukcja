package server.threads;

import server.Server;
import server.database.UserRepository;
import utils.data.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import server.database.AuctionRepository;
import utils.message.Message;
import utils.message.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private AuctionRepository auctionRepository = new AuctionRepository();
    private UserRepository userRepository = new UserRepository();
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ServerClientThread(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream())), true);
    }

    public void sendMessage(Message message) throws JsonProcessingException {
        String output = message.serialize();
        out.println(output);
    }

    @Override
    public void run() {
        try {
            sendMessage(new Message<List<AuctionItem>>(MessageType.AUCTION_LIST, auctionRepository.getAuctionsFromFile()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (true) {
                String message = in.readLine();
                Message<?> base = Message.deserialize(message, new TypeReference<Message<Object>>() {
                });
                System.out.println(message);

                if (message == null) {
                    break;
                }

                switch (base.getType()) {
                    // Dodanie nowej aukcji. Oraz wysłanie informacji do wszystkich klientów
                    case NEW_AUCTION -> {
                        Message<AuctionItem> deserialized = Message.deserialize(message, new TypeReference<Message<AuctionItem>>() {
                        });
                        AuctionItem item = deserialized.getMessage();
                        auctionRepository.writeAuctionToFile(item);

                        for (ServerClientThread client : Server.clientList) {
                            client.sendMessage(new Message<List<AuctionItem>>(MessageType.AUCTION_LIST, auctionRepository.getAuctionsFromFile()));
                        }
                    }
                    // Autoryzacja użytkownika.
                    case AUTHENTICATE -> {
                        Message<LoginRequest> deserialized = Message.deserialize(message, new TypeReference<Message<LoginRequest>>() {
                        });
                        LoginRequest request = deserialized.getMessage();

                        String username = request.getUsername();
                        String password = request.getPassword();

                        List<User> users = userRepository.getUsersFromFile();

                        System.out.println(users);

                        User matchedUser = users.stream()
                                .filter(user ->
                                        username.equals(user.getName()) &&
                                                password.equals(user.getPassword())
                                )
                                .findFirst()
                                .orElse(null);

                        PublicUser publicUser = new PublicUser(matchedUser.getName(), matchedUser.getValet());

                        if (matchedUser != null) {
                            setUserName(matchedUser.getName());
                            sendMessage(new Message<PublicUser>(MessageType.AUTHENTICATE, publicUser));
                        } else {
                            sendMessage(new Message<>(MessageType.UNAUTHORIZED, "Unauthorized"));
                        }
                    }
                    // - Aktualizuje aukcję o nową kwotę i licytującego
                    // - Odejmuje środki nowemu licytującemu
                    // - Zwraca środki poprzedniemu licytującemu (jeśli istnieje)
                    case BID -> {
                        Message<BidRequest> deserialized = Message.deserialize(message, new TypeReference<>() {
                        });
                        BidRequest request = deserialized.getMessage();

                        int auctionId = request.getAuctionId();
                        float newAmount = request.getAmount();
                        String bidder = request.getBidderName();

                        List<AuctionItem> auctions = auctionRepository.getAuctionsFromFile();
                        List<User> users = userRepository.getUsersFromFile();

                        AuctionItem auctionItem = auctions.stream()
                                .filter(a -> a.getId().equals(auctionId))
                                .findFirst()
                                .orElse(null);

                        if (auctionItem == null) {
                            return;
                        }

                        String prevBidder = auctionItem.getHighestBidder();
                        Float prevPrice = auctionItem.getActualPrice();

                        auctionItem.setActualPrice(newAmount);
                        auctionItem.setHighestBidder(bidder);

                        auctionRepository.editAuctionAndWrite(auctionItem);

                        List<AuctionItem> updatedList = auctionRepository.getAuctionsFromFile();

                        User user = users.stream().filter(u -> u.getName().equals(bidder)).findFirst().orElse(null);
                        float updatedValet = user.getValet() - newAmount;
                        user.setValet(updatedValet);
                        userRepository.editUserAndWrite(user);

                        if (prevBidder != null && !prevBidder.isBlank() && !prevBidder.equals(bidder)) {
                            User highestBidderUser = users.stream()
                                    .filter(u -> u.getName().equals(prevBidder))
                                    .findFirst()
                                    .orElse(null);

                            if (highestBidderUser != null) {
                                float highestBidderValet = highestBidderUser.getValet() + prevPrice;
                                highestBidderUser.setValet(highestBidderValet);
                                userRepository.editUserAndWrite(highestBidderUser);

                                for (ServerClientThread client : Server.clientList) {
                                    try {
                                        if (client.getUserName().equals(prevBidder)) {
                                            client.sendMessage(new Message<>(MessageType.BID, new BidResponse(updatedList, highestBidderValet)));
                                        }
                                    } catch (JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        sendMessage(new Message<>(MessageType.BID, new BidResponse(updatedList, updatedValet)));

                        for (ServerClientThread client : Server.clientList) {
                            client.sendMessage(new Message<>(MessageType.AUCTION_LIST, updatedList));
                        }
                    }
                }
            }
        } catch (IOException e) {
            out.close();
            try {
                in.close();
            } catch (IOException e1) {
            }
            try {
                socket.close();
            } catch (IOException e2) {
            }
        }
    }
}
