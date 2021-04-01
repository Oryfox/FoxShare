import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (Live.socket != null) Live.socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }));
        Live.startOnlinePresence();

        Live.ping();
        Live.register("Aurora");
        Thread.sleep(500);
        Live.getReceivers();
        Thread.sleep(2000);
        Live.sendFile(new File("image.png"), "Aurora", Live.availableReceivers.get("Aurora"));
    }
}
