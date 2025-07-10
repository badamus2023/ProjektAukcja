package utils.data;

public class EndAuctionResponse {
    private Float amount;

    public EndAuctionResponse(Float amount) {
        this.amount = amount;
    }

    public EndAuctionResponse() {}

    public Float getAmount() {
        return amount;
    }

    public Float setAmount(Float amount) {
        return this.amount = amount;
    }
}
