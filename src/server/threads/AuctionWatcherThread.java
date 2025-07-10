package server.threads;

import server.database.AuctionRepository;
import server.database.UserRepository;
import server.Server;
import utils.status.AuctionStatus;
import utils.message.Message;
import utils.message.MessageType;
import utils.data.AuctionItem;
import utils.data.EndAuctionResponse;
import utils.data.User;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionWatcherThread extends Thread {
    private final AuctionRepository auctionRepo = new AuctionRepository();
    private final UserRepository userRepo = new UserRepository();

    @Override
    public void run() {
        while (true) {
            try {
                List<AuctionItem> auctions = auctionRepo.getAuctionsFromFile();
                List<User> users = userRepo.getUsersFromFile();
                boolean changed = false;

                for (AuctionItem item : auctions) {
                    if (AuctionStatus.ACTIVE.equals(item.getStatus())) {
                        LocalDateTime endTime = LocalDateTime.parse(item.getTimeToEnd());

                        // Jeśli czas zakończenia aukcji minął ustawia status aukcji na zakończono.
                        if (LocalDateTime.now().isAfter(endTime)) {
                            item.setStatus(AuctionStatus.FINISHED);
                            changed = true;

                            String ownerName = item.getOwnerName();
                            String highestBidder = item.getHighestBidder();
                            float finalPrice = item.getActualPrice();

                            if (highestBidder != null && !highestBidder.isBlank()) {
                                User owner = users.stream()
                                        .filter(u -> u.getName().equals(ownerName))
                                        .findFirst()
                                        .orElse(null);

                                // Dodaje pieniądze do portfela właściciela
                                if (owner != null) {
                                    Float updatedValet = owner.getValet() + finalPrice;
                                    owner.setValet(updatedValet);
                                    userRepo.editUserAndWrite(owner);

                                    // Wysyła wiadomość o zakończonej aukcji do właściciela, dodaje bilans do konta
                                    for (ServerClientThread client : Server.clientList) {
                                        try {
                                            if(client.getUserName().equals(ownerName)) {
                                                client.sendMessage(new Message<>(MessageType.AUCTION_END, new EndAuctionResponse(updatedValet)));
                                            }
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Jeśli jakaś aukcja została zakończona, zapisuje zaktualizowaną listę aukcji.
                // Wysyła informacje do wszystkich klientów.
                if (changed) {
                    auctionRepo.writeAuctionsToFile(auctions);

                    for (ServerClientThread client : Server.clientList) {
                        try {
                            client.sendMessage(new Message<>(MessageType.AUCTION_LIST, auctions));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
