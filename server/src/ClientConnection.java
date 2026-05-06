import java.io.PrintWriter;

public class ClientConnection {
    private final String name;
    private final PrintWriter writer;

    public ClientConnection(String name, PrintWriter writer) {
        this.name = name;
        this.writer = writer;
    }

    public String getName() {
        return name;
    }

    public synchronized void send(String message) {
        writer.println(message);
    }
}
