package utils.data;

import java.util.List;

public class BidResponse {
    public List<AuctionItem> getAuctions() {
        return auctions;
    }

    public void setAuctions(List<AuctionItem> auctions) {
        this.auctions = auctions;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    private List<AuctionItem> auctions;
    private float amount;

    public BidResponse(List<AuctionItem> auctions, float amount) {
        this.auctions = auctions;
        this.amount = amount;
    }

    public BidResponse() {}
}
