package client.models;

import utils.data.AuctionItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class AuctionRowRender extends DefaultTableCellRenderer {
    private final String currentUsername;
    private final AuctionTableModel model;

    public AuctionRowRender(String currentUsername, AuctionTableModel model) {
        this.currentUsername = currentUsername;
        this.model = model;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        AuctionItem item = model.getItems().get(row);

        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        } else {
            c.setForeground(Color.BLACK);

            if (item.getOwnerName().equals(currentUsername)) {
                c.setBackground(new Color(255, 235, 205));
            } else if (item.getHighestBidder().equals(currentUsername)) {
                c.setBackground(new Color(204, 255, 204));
            } else {
                c.setBackground(Color.WHITE);
            }
        }

        return c;
    }
}
