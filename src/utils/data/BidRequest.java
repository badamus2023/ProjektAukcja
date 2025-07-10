package utils.data;

public class BidRequest {
    private int auctionId;
    private float amount;
    private String bidderName;

    public BidRequest() {}

    public BidRequest(int auctionId, float amount, String bidderName) {
        this.auctionId = auctionId;
        this.amount = amount;
        this.bidderName = bidderName;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public float getAmount() {
        return amount;
    }

    public String getBidderName() {
        return bidderName;
    }
}

