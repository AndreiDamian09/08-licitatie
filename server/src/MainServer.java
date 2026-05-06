public class MainServer {
    public static void main(String[] args) {
        int port = getEnvInt("SERVER_PORT", 5000);
        int auctionDurationSeconds = getEnvInt("AUCTION_DURATION_SECONDS", 30);

        AuctionServer server = new AuctionServer(port, auctionDurationSeconds);
        try {
            server.start();
        } catch (Exception e) {
            System.err.println("Server failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getEnvInt(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
