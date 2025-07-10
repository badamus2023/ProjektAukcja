package client.views;

import client.models.AuctionRowRender;
import utils.status.AuctionStatus;
import utils.data.AuctionItem;
import client.network.threads.ClientThread;
import client.models.AuctionTableModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import utils.message.Message;
import utils.message.MessageType;
import utils.data.BidRequest;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {
    private AuctionTableModel model;
    private JTable auctionTable;
    private ClientThread client;
    private JLabel amountLabel;

    public MainView(ClientThread client) {
    super("Projekt Aukcja Swing");
    this.client = client;
    this.model = client.getTableModel();
    this.amountLabel = client.getAmount();
    this.auctionTable = new JTable(model);
    setSize(600,400);
    init();
    }

    public void init() {
        // Timer odświeżający GUI co 1 sekundę, nie blokuje select'a (np. do aktualizacji statusów aukcji)
        Timer timer = new Timer(1000, e -> {
            int selectedRow = auctionTable.getSelectedRow();
            model.fireTableRowsUpdated(0, model.getRowCount() - 1);
            if (selectedRow >= 0 && selectedRow < model.getRowCount()) {
                auctionTable.setRowSelectionInterval(selectedRow, selectedRow);
            }
        });
        timer.start();

        JPanel panel = new JPanel(new BorderLayout());

        JPanel bidPanel = new JPanel();
        JTextField bidTextField = new JTextField(10);
        JButton bidButton = new JButton("Licytuj");
        JLabel bidLabel = new JLabel("Kwota: ");
        JLabel valetLabel = new JLabel("Portfel: ");
        amountLabel.setText(String.valueOf(client.getUser().getValet()));

        bidPanel.add(bidLabel);
        bidPanel.add(bidTextField);
        bidPanel.add(bidButton);
        bidPanel.add(valetLabel);
        bidPanel.add(amountLabel);

        auctionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AuctionRowRender renderer = new AuctionRowRender(client.getUser().getName(), model);

        for (int i = 0; i < auctionTable.getColumnCount(); i++) {
            auctionTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Obsługa kliknięcia przycisku "Licytuj"
        bidButton.addActionListener(e -> {
            int row = auctionTable.getSelectedRow();
            if(row == -1) {
                JOptionPane.showMessageDialog(this, "Nie wybrano aukcji.");
                return;
            }

            String bidText = bidTextField.getText();
            if(bidText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nie wpisano kwoty.");
                return;
            }

            try {
                float bidAmount = Float.parseFloat(bidText);
                AuctionItem item = model.getItems().get(row);
                Integer itemId = item.getId();

                BidRequest bidRequest = new BidRequest(itemId, bidAmount, client.getUser().getName());

                if (item.getStatus() == AuctionStatus.FINISHED) {
                    JOptionPane.showMessageDialog(this, "Ta aukcja została zakończona.");
                    return;
                }

                if(item.getOwnerName().equals(client.getUser().getName())) {
                    JOptionPane.showMessageDialog(this,"Nie możesz licytować swoich aukcji");
                    return;
                }

                if(bidAmount <= item.getActualPrice()) {
                    System.out.println(item.getActualPrice());
                    System.out.println(bidAmount);

                    JOptionPane.showMessageDialog(this, "Za mała kwota");
                    return;
                }

                if(bidRequest.getAmount() > client.getUser().getValet()) {
                    JOptionPane.showMessageDialog(this, "Nie masz na tyle pięniędzy w portfelu");
                    return;
                }

                try {
                    client.sendMessage(new Message<BidRequest>(MessageType.BID, bidRequest));
                } catch(JsonProcessingException j) {
                    j.printStackTrace();
                }

            } catch(NumberFormatException ex) {
                System.out.println("Nie prawidłowa kwota.");
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel topLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Dodaj aukcje");
        topLeftPanel.add(addButton);

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel userLabel = new JLabel("Zalogowany jako: " + client.getUser().getName());
        topRightPanel.add(userLabel);

        addButton.addActionListener(e -> openAuctionDialog());

        topPanel.add(topRightPanel, BorderLayout.EAST);
        topPanel.add(topLeftPanel, BorderLayout.WEST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(auctionTable), BorderLayout.CENTER);
        panel.add(bidPanel, BorderLayout.SOUTH);
        setContentPane(panel);
    }

    private void openAuctionDialog() {
        JDialog dialog = new JDialog(this, "Dodaj aukcję", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JLabel nameLabel = new JLabel("Nazwa produktu: ");
        JTextField nameTextField = new JTextField(10);

        JLabel priceLabel = new JLabel("Cena: ");
        JTextField priceTextField = new JTextField(10);

        JLabel durationLabel = new JLabel("Czas trwania: ");
        String[] durationOptions = {"1 minuta", "5 minut", "10 minut"};
        JComboBox<String> durationComboBox = new JComboBox<>(durationOptions);

        JButton addButton = new JButton("Dodaj");

        // Górny pasek GUI – po lewej przycisk "Dodaj aukcję", po prawej użytkownik
        addButton.addActionListener(e -> {
            try {
                String name = nameTextField.getText();
                String priceText = priceTextField.getText();
                String selectedDuration = (String) durationComboBox.getSelectedItem();

                double price = Double.parseDouble(priceText);

                int durationSeconds = switch (selectedDuration) {
                    case "1 minuta" -> 60;
                    case "5 minut" -> 300;
                    case "10 minut" -> 600;
                    default -> 60;
                };

                String formattedEndTime = java.time.LocalDateTime
                        .now()
                        .plusSeconds(durationSeconds)
                        .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                AuctionItem item = new AuctionItem(
                        model.getRowCount() + 1,
                        name,
                        (float) price,
                        formattedEndTime,
                        client.getUser().getName(),
                        AuctionStatus.ACTIVE
                );

                try {
                    client.sendMessage(new Message<>(MessageType.NEW_AUCTION, item));
                } catch (JsonProcessingException j) {
                    System.out.println("<Client:NewMessage> Błąd podczas formatowania json");
                }

                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Nieprawidłowa cena.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(nameLabel);
        dialog.add(nameTextField);
        dialog.add(priceLabel);
        dialog.add(priceTextField);
        dialog.add(durationLabel);
        dialog.add(durationComboBox);
        dialog.add(new JLabel());
        dialog.add(addButton);

        dialog.setVisible(true);
    }

}
