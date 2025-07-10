package client.models;

import utils.status.AuctionStatus;
import utils.data.AuctionItem;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class AuctionTableModel extends AbstractTableModel {
    private List<AuctionItem> items = new ArrayList<>();
    private String[] headers = {"ID", "Produkt", "Aktualna cena", "Koniec aukcji"};

    public AuctionTableModel() {}

    public AuctionTableModel(List<AuctionItem> items) {
        this.items = items;
    }

    public void setItems(List<AuctionItem> items) {
        this.items = new ArrayList<>(items);
        fireTableDataChanged();

        System.out.println("Zmieniono model");
    }

    public void addItem(AuctionItem item) {
        items.add(item);
        fireTableDataChanged();
    }

    public void removeItem(int id) {
        items.removeIf(item -> item.getId() == id);
        fireTableDataChanged();
    }

    public List<AuctionItem> getItems() {
        return items;
    }


    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Integer.class;
            case 1: return String.class;
            case 2: return Float.class;
            case 3: return String.class;
            default: return null;
        }
    }

    @Override
    public int getRowCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public int getColumnCount() {
        return headers.length;
    }

    @Override
    public String getColumnName(int column) {
        return headers[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AuctionItem item = (AuctionItem) items.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return item.getId();
            case 1:
                return item.getProduct();
            case 2:
                return item.getActualPrice();
            case 3:
                if (AuctionStatus.FINISHED.equals(item.getStatus())) return "Zakończona";
                return item.getTimeLeftFormatted();
            default: return null;
        }
    }
}
