import java.util.HashSet;
import java.util.Set;

public class AuctionItem {
    private final String name;
    private final String owner;
    private final double minimumPrice;
    private double currentPrice;
    private String highestBidder;
    private boolean active;
    private final Set<String> bidders;

    public AuctionItem(String name, String owner, double minimumPrice) {
        this.name = name;
        this.owner = owner;
        this.minimumPrice = minimumPrice;
        this.currentPrice = minimumPrice;
        this.highestBidder = "-";
        this.active = true;
        this.bidders = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public double getMinimumPrice() {
        return minimumPrice;
    }

    public synchronized double getCurrentPrice() {
        return currentPrice;
    }

    public synchronized String getHighestBidder() {
        return highestBidder;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized boolean placeBid(String bidder, double amount) {
        if (!active || amount <= currentPrice) {
            return false;
        }
        currentPrice = amount;
        highestBidder = bidder;
        bidders.add(bidder);
        return true;
    }

    public synchronized Set<String> getBiddersSnapshot() {
        return new HashSet<>(bidders);
    }

    public synchronized void expire() {
        active = false;
    }
}
