import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Tray {

    static TrayIcon trayIcon;
    static Image redFox = Toolkit.getDefaultToolkit().getImage(FoxShare.class.getResource("icons/redfox.png"));
    static Image greenFox = Toolkit.getDefaultToolkit().getImage(FoxShare.class.getResource("icons/greenfox.png"));

    public static void start() throws AWTException {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();

            trayIcon = new TrayIcon(redFox, "FoxShare", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                        if (e.getButton() == 1) new MainFrame();
                    } else {
                        if (e.getButton() == 3) new MainFrame();
                    }
                }
            });

            MenuItem enableReceiving = new MenuItem(FoxShare.bundle.getString("enableReceiving"));
            enableReceiving.addActionListener(e -> {
                Contact.receiving = true;
                trayIcon.setImage(greenFox);
            });
            popup.add(enableReceiving);

            MenuItem disableReceiving = new MenuItem(FoxShare.bundle.getString("disableReceiving"));
            disableReceiving.addActionListener(e -> {
                Contact.receiving = false;
                trayIcon.setImage(redFox);
            });
            popup.add(disableReceiving);

            MenuItem sendFiles = new MenuItem(FoxShare.bundle.getString("sendFiles"));
            sendFiles.addActionListener(e -> Sender.startSending());
            popup.add(sendFiles);

            Menu information = new Menu(FoxShare.bundle.getString("information"));

            MenuItem version = new MenuItem("Version: " + FoxShare.version);
            version.setEnabled(false);
            information.add(version);

            MenuItem copyright = new MenuItem("Copyright (c) 2021 Semih Kaiser");
            copyright.setEnabled(false);
            information.add(copyright);

            try {
                MenuItem hostname = new MenuItem("Hostname: " + InetAddress.getLocalHost().getHostName());
                hostname.setEnabled(false);
                information.add(hostname);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            try {
                MenuItem ip = new MenuItem("IP: " + InetAddress.getLocalHost().getHostAddress());
                ip.setEnabled(false);
                information.add(ip);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            popup.add(information);

            MenuItem quit = new MenuItem(FoxShare.bundle.getString("quit"));
            quit.addActionListener(e -> System.exit(0));
            popup.add(quit);

            tray.add(trayIcon);
        }
    }
}
