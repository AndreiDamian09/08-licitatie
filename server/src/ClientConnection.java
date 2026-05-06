import java.io.PrintWriter;

public class ClientConnection implements AuctionObserver {
    private final String name;
    private final PrintWriter writer;

    public ClientConnection(String name, PrintWriter writer) {
        this.name = name;
        this.writer = writer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update(AuctionEvent event) {
        send(event.getMessage());
    }

    public synchronized void send(String message) {
        writer.println(message);
    }
}
