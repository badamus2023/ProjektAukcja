package server.database;

import utils.data.AuctionItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuctionRepository {
    private final Path file = Paths.get("src/server/database/data/auctions.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public void editAuctionAndWrite(AuctionItem updatedItem) throws IOException {
        List<AuctionItem> items = getAuctionsFromFile();

        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(items.get(i).getId(), updatedItem.getId())) {
                items.set(i, updatedItem);
                break;
            }
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), items);
    }

    public List<AuctionItem> getAuctionsFromFile() throws IOException {
        if(!file.toFile().exists()) {
            return new ArrayList<>();
        }

        return mapper.readValue(file.toFile(), new TypeReference<List<AuctionItem>>() {});
    }

    public void writeAuctionsToFile(List<AuctionItem> items) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), items);
    }

    public void writeAuctionToFile(AuctionItem item) throws IOException {
        List<AuctionItem> items = this.getAuctionsFromFile();
        items.add(item);


        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), items);
    }
}
