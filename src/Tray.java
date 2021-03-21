import java.awt.*;

public class Tray {

    static TrayIcon trayIcon;
    static MenuItem toggleReceiving;

    public static void start() throws AWTException {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();

            trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(FoxShare.class.getResource("icons/fox.png")), "FoxShare", popup);
            trayIcon.setImageAutoSize(true);

            toggleReceiving = new MenuItem(FoxShare.bundle.getString("enableReceiving"));
            toggleReceiving.addActionListener(e -> {
                Contact.receiving = !Contact.receiving;
                if (Contact.receiving)
                    toggleReceiving.setLabel(FoxShare.bundle.getString("disableReceiving"));
                else
                    toggleReceiving.setLabel(FoxShare.bundle.getString("enableReceiving"));
            });
            popup.add(toggleReceiving);

            MenuItem sendFiles = new MenuItem(FoxShare.bundle.getString("sendFiles"));
            sendFiles.addActionListener(e -> Sender.startSending());
            popup.add(sendFiles);

            MenuItem showGUI = new MenuItem("GUI");
            showGUI.addActionListener(e -> new MainFrame());
            popup.add(showGUI);

            MenuItem quit = new MenuItem(FoxShare.bundle.getString("quit"));
            quit.addActionListener(e -> System.exit(0));
            popup.add(quit);

            tray.add(trayIcon);
        }
    }
}
