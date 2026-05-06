import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionServer {
    private final int port;
    private final int auctionDurationSeconds;
    private final Map<String, ClientConnection> clients;
    private final Map<String, AuctionItem> items;
    private final ScheduledExecutorService scheduler;
    private final DecimalFormat priceFormat;

    public AuctionServer(int port, int auctionDurationSeconds) {
        this.port = port;
        this.auctionDurationSeconds = auctionDurationSeconds;
        this.clients = new ConcurrentHashMap<>();
        this.items = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.priceFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.US);
        this.priceFormat.applyPattern("0.00");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Auction server started on port " + port);
            System.out.println("Auction duration: " + auctionDurationSeconds + " seconds");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }
        } finally {
            scheduler.shutdownNow();
        }
    }

    private void handleClient(Socket socket) {
        String clientName = null;
        try (Socket autoClose = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println("WELCOME Use CONNECT <name> as first command.");
            writer.println("INFO Commands: LIST, PUBLISH <product> <minPrice>, BID <product> <amount>, QUIT");

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                String[] parts = trimmed.split("\\s+");
                String cmd = parts[0].toUpperCase(Locale.ROOT);

                if (clientName == null) {
                    if (!"CONNECT".equals(cmd)) {
                        writer.println("ERR First command must be CONNECT <name>.");
                        continue;
                    }
                    clientName = handleConnect(parts, writer);
                    if (clientName != null) {
                        broadcast("EVENT CLIENT_JOINED " + clientName, clientName);
                    }
                    continue;
                }

                switch (cmd) {
                    case "LIST":
                        sendProductList(writer);
                        break;
                    case "PUBLISH":
                        handlePublish(parts, clientName, writer);
                        break;
                    case "BID":
                        handleBid(parts, clientName, writer);
                        break;
                    case "QUIT":
                        writer.println("OK Bye.");
                        return;
                    default:
                        writer.println("ERR Unknown command.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client connection error: " + e.getMessage());
        } finally {
            if (clientName != null) {
                clients.remove(clientName);
                broadcast("EVENT CLIENT_LEFT " + clientName, clientName);
            }
        }
    }

    private String handleConnect(String[] parts, PrintWriter writer) {
        if (parts.length != 2) {
            writer.println("ERR Usage: CONNECT <name>");
            return null;
        }

        String candidate = parts[1];
        if (clients.containsKey(candidate)) {
            writer.println("ERR Name already in use.");
            return null;
        }

        ClientConnection connection = new ClientConnection(candidate, writer);
        ClientConnection prev = clients.putIfAbsent(candidate, connection);
        if (prev != null) {
            writer.println("ERR Name already in use.");
            return null;
        }

        writer.println("OK Connected as " + candidate + ".");
        sendProductList(writer);
        return candidate;
    }

    private void sendProductList(PrintWriter writer) {
        List<AuctionItem> snapshot = new ArrayList<>(items.values());
        writer.println("PRODUCTS " + snapshot.size());
        for (AuctionItem item : snapshot) {
            writer.println(buildProductLine(item));
        }
        writer.println("END_PRODUCTS");
    }

    private String buildProductLine(AuctionItem item) {
        String status = item.isActive() ? "ACTIVE" : "EXPIRED";
        return "PRODUCT "
                + item.getName()
                + " owner=" + item.getOwner()
                + " min=" + formatPrice(item.getMinimumPrice())
                + " current=" + formatPrice(item.getCurrentPrice())
                + " status=" + status;
    }

    private void handlePublish(String[] parts, String owner, PrintWriter writer) {
        if (parts.length != 3) {
            writer.println("ERR Usage: PUBLISH <productName> <minimumPrice>");
            return;
        }

        String productName = parts[1];
        double minPrice;
        try {
            minPrice = Double.parseDouble(parts[2]);
            if (minPrice <= 0) {
                writer.println("ERR Minimum price must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            writer.println("ERR Invalid minimum price.");
            return;
        }

        AuctionItem item = new AuctionItem(productName, owner, minPrice);
        AuctionItem previous = items.putIfAbsent(productName, item);
        if (previous != null) {
            writer.println("ERR Product name already exists.");
            return;
        }

        writer.println("OK Product published.");
        broadcast("EVENT PRODUCT_PUBLISHED " + buildProductLine(item), null);
        scheduler.schedule(() -> expireAuction(productName), auctionDurationSeconds, TimeUnit.SECONDS);
    }

    private void handleBid(String[] parts, String bidder, PrintWriter writer) {
        if (parts.length != 3) {
            writer.println("ERR Usage: BID <productName> <amount>");
            return;
        }

        String productName = parts[1];
        double amount;
        try {
            amount = Double.parseDouble(parts[2]);
            if (amount <= 0) {
                writer.println("ERR Bid amount must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            writer.println("ERR Invalid bid amount.");
            return;
        }

        AuctionItem item = items.get(productName);
        if (item == null) {
            writer.println("ERR Product not found.");
            return;
        }

        synchronized (item) {
            if (!item.isActive()) {
                writer.println("ERR Product auction expired.");
                return;
            }
            if (amount <= item.getCurrentPrice()) {
                writer.println("ERR Bid must be greater than current price " + formatPrice(item.getCurrentPrice()) + ".");
                return;
            }

            Set<String> previousBidders = item.getBiddersSnapshot();
            boolean accepted = item.placeBid(bidder, amount);
            if (!accepted) {
                writer.println("ERR Bid rejected.");
                return;
            }

            writer.println("OK Bid accepted. New current price: " + formatPrice(item.getCurrentPrice()) + ".");
            notifyBidInterestedUsers(item, bidder, previousBidders);
            broadcast("EVENT BID_UPDATE " + buildProductLine(item), null);
        }
    }

    private void notifyBidInterestedUsers(AuctionItem item, String newBidder, Set<String> previousBidders) {
        String message = "EVENT BID_NOTICE " + item.getName() + " newBid=" + formatPrice(item.getCurrentPrice()) + " bidder=" + newBidder;

        sendToClient(item.getOwner(), message);
        for (String bidderName : previousBidders) {
            sendToClient(bidderName, message);
        }
    }

    private void expireAuction(String productName) {
        AuctionItem item = items.get(productName);
        if (item == null) {
            return;
        }

        synchronized (item) {
            if (!item.isActive()) {
                return;
            }
            item.expire();
        }
        broadcast("EVENT AUCTION_EXPIRED " + buildProductLine(item), null);
    }

    private void sendToClient(String clientName, String message) {
        ClientConnection connection = clients.get(clientName);
        if (connection != null) {
            connection.send(message);
        }
    }

    private void broadcast(String message, String excludedClient) {
        for (ClientConnection connection : clients.values()) {
            if (excludedClient != null && excludedClient.equals(connection.getName())) {
                continue;
            }
            connection.send(message);
        }
    }

    private String formatPrice(double value) {
        return priceFormat.format(value);
    }
}
