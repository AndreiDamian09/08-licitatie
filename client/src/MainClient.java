import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) {
        String host = getEnvOrDefault("SERVER_HOST", "localhost");
        int port = getEnvInt("SERVER_PORT", 5000);

        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        System.out.println("Connecting to " + host + ":" + port + "...");
        try (Socket socket = new Socket(host, port);
             BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            Thread listener = new Thread(() -> listenToServer(serverReader));
            listener.setDaemon(true);
            listener.start();

            System.out.println("Type commands:");
            System.out.println("CONNECT <name>");
            System.out.println("LIST");
            System.out.println("WINS");
            System.out.println("PUBLISH <productName> <minimumPrice>");
            System.out.println("BID <productName> <amount>");
            System.out.println("QUIT");

            while (true) {
                String input = scanner.nextLine();
                serverWriter.println(input);
                if ("QUIT".equalsIgnoreCase(input.trim())) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private static void listenToServer(BufferedReader serverReader) {
        try {
            String line;
            while ((line = serverReader.readLine()) != null) {
                System.out.println("[SERVER] " + line);
            }
        } catch (IOException e) {
            System.err.println("Disconnected from server: " + e.getMessage());
        }
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
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
