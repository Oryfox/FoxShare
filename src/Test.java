import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (Live.socket != null) Live.socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }));
        Live.startOnlinePresence();

        System.out.println(Live.ping());
        System.out.println("Registered: " + Live.register("Aurora"));
        System.out.println("!");
        System.out.println(Live.getReceivers());
        System.out.println("ENDE");
    }
}
