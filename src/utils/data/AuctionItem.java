package utils.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import utils.status.AuctionStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class AuctionItem {
    private String timeToEnd;
    private Integer id;
    private String product;
    private Float actualPrice;
    private String ownerName;
    private String highestBidder;
    private AuctionStatus status = AuctionStatus.ACTIVE;

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    public String getTimeToEnd() {
        return timeToEnd;
    }

    public void setTimeToEnd(String timeToEnd) {
        this.timeToEnd = timeToEnd;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Float getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(Float actualPrice) {
        this.actualPrice = actualPrice;
    }

    @JsonIgnore
    public String getTimeLeftFormatted() {
        try {
            LocalDateTime endTime = LocalDateTime.parse(timeToEnd);
            Duration duration = Duration.between(LocalDateTime.now(), endTime);

            if (duration.isNegative()) {
                return "00:00";
            }

            long minutes = duration.toMinutes();
            long seconds = duration.minusMinutes(minutes).getSeconds();

            return String.format("%02d:%02d", minutes, seconds);
        } catch (DateTimeParseException e) {
            return "--:--";
        }
    }


    public AuctionItem(Integer id, String product, Float actualPrice, String timeToEnd, String ownerName, AuctionStatus status) {
        this.actualPrice = actualPrice;
        this.id = id;
        this.product = product;
        this.timeToEnd = timeToEnd;
        this.highestBidder = "";
        this.ownerName = ownerName;
        this.status = status;
    }

    public AuctionItem() {}
}
