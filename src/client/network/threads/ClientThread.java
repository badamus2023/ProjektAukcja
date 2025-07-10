package client.network.threads;

import utils.data.AuctionItem;
import client.models.AuctionTableModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import utils.message.Message;
import utils.data.BidResponse;
import utils.data.EndAuctionResponse;
import utils.data.PublicUser;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private AuctionTableModel tableModel = new AuctionTableModel();
    private PublicUser user = null;
    private Runnable loginSuccessCallback;
    private Runnable loginFailureCallback;
    private JLabel amountLabel = new JLabel("0.0");

    public AuctionTableModel getTableModel() {
        return tableModel;
    }
    public JLabel getAmount() {
        return amountLabel;
    }

    public PublicUser getUser() {
        return user;
    }
    public void setUser(PublicUser user) {
        this.user = user;
    }

    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream()
        )), true);
    }

    public void sendMessage(Message message) throws JsonProcessingException {
        String output = message.serialize();

        out.println(output);
    }

    // Ustawia callback, który ma zostać wywołany po udanym logowaniu
    public void setLoginSuccessCallback(Runnable callback) {
        this.loginSuccessCallback = callback;
    }

    // Ustawia callback, który ma zostać wywołany po nieudanym logowaniu
    public void setLoginFailureCallback(Runnable callback) {
        this.loginFailureCallback = callback;
    }

    @Override
    public void run() {
        try {
            while(true) {
                String message = in.readLine();
                Message<?> base = Message.deserialize(message, new TypeReference<Message<Object>>() {});

                if(message == null) {
                    break;
                }

                switch(base.getType()) {
                    // Aktualizacja listy aukcji w GUI
                    case AUCTION_LIST -> {
                        System.out.println(message);
                        Message<List<AuctionItem>> items = Message.deserialize(message, new TypeReference<Message<List<AuctionItem>>>() {});
                        List<AuctionItem> itemsList = items.getMessage();

                        SwingUtilities.invokeLater(() -> tableModel.setItems(itemsList));
                    }
                    // Po udanym logowaniu: ustawienie użytkownika i wywołanie callbacku
                    case AUTHENTICATE -> {
                        System.out.println(message);
                        Message<PublicUser> userMessage = Message.deserialize(message, new TypeReference<Message<PublicUser>>() {});
                        PublicUser user = userMessage.getMessage();

                        setUser(user);

                        if(loginSuccessCallback != null) {
                            SwingUtilities.invokeLater(loginSuccessCallback);
                        }
                    }
                    // Po nieudanym logowaniu: wywołanie callbacku
                    case UNAUTHORIZED -> {
                        if(loginFailureCallback != null) {
                            SwingUtilities.invokeLater(loginFailureCallback);
                        }
                    }
                    // Aktualizacja listy aukcji i portfela po przebiciu oferty
                    case BID -> {
                        System.out.println(message);
                        Message<BidResponse> items = Message.deserialize(message, new TypeReference<Message<BidResponse>>() {});
                        BidResponse msg = items.getMessage();

                        SwingUtilities.invokeLater(() -> tableModel.setItems(msg.getAuctions()));
                        SwingUtilities.invokeLater(() -> amountLabel.setText(String.valueOf(msg.getAmount())));
                    }
                    // Aktualizacja portfela po zakończeniu aukcji
                    case AUCTION_END -> {
                        Message<EndAuctionResponse> msg = Message.deserialize(message, new TypeReference<Message<EndAuctionResponse>>() {});
                        EndAuctionResponse response = msg.getMessage();

                        SwingUtilities.invokeLater(() -> amountLabel.setText(String.valueOf(response.getAmount())));
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
