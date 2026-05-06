import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AuctionEvent {
    private final String message;
    private final String excludedObserver;
    private final Set<String> targetObservers;

    private AuctionEvent(String message, String excludedObserver, Set<String> targetObservers) {
        this.message = message;
        this.excludedObserver = excludedObserver;
        this.targetObservers = targetObservers == null ? null : Collections.unmodifiableSet(new HashSet<>(targetObservers));
    }

    public static AuctionEvent broadcast(String message) {
        return new AuctionEvent(message, null, null);
    }

    public static AuctionEvent broadcastExcept(String message, String excludedObserver) {
        return new AuctionEvent(message, excludedObserver, null);
    }

    public static AuctionEvent targeted(String message, Set<String> targetObservers) {
        return new AuctionEvent(message, null, targetObservers);
    }

    public String getMessage() {
        return message;
    }

    public boolean shouldNotify(String observerName) {
        if (excludedObserver != null && excludedObserver.equals(observerName)) {
            return false;
        }
        return targetObservers == null || targetObservers.contains(observerName);
    }
}
