import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionEventPublisher {
    private final Map<String, AuctionObserver> observers = new ConcurrentHashMap<>();

    public boolean register(AuctionObserver observer) {
        return observers.putIfAbsent(observer.getName(), observer) == null;
    }

    public void unregister(String observerName) {
        observers.remove(observerName);
    }

    public boolean contains(String observerName) {
        return observers.containsKey(observerName);
    }

    public void notifyObservers(AuctionEvent event) {
        for (AuctionObserver observer : observers.values()) {
            if (event.shouldNotify(observer.getName())) {
                observer.update(event);
            }
        }
    }
}
