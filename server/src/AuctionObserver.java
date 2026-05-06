public interface AuctionObserver {
    String getName();

    void update(AuctionEvent event);
}
