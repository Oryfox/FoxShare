import java.awt.*;
import java.io.File;

public class Sender {

    static File[] files;

    public static void startSending() {
        FileDialog fileDialog = new FileDialog(MainFrame.frame);
        fileDialog.setDirectory(System.getProperty("user.home"));
        fileDialog.setMultipleMode(true);
        fileDialog.setTitle("Select Files to send");
        fileDialog.setVisible(true);
        files = fileDialog.getFiles();

        Contact.availableHosts.clear();
        new MainFrame().sender();
        Contact.startPing();
    }
}
