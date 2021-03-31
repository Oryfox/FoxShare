import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    JPanel hostNames;
    JPanel basePanel;
    static MainFrame frame;

    public MainFrame() {
        super("FoxShare");
        if (frame != null) frame.setVisible(false);
        if (!SystemTray.isSupported()) this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setIconImage(Tray.redFox);

        basePanel = new JPanel(new GridLayout(0,1));
        basePanel.setBackground(Color.white);

        JPanel receiverPanel = new JPanel(new GridLayout(1,0));
        receiverPanel.setOpaque(false);
        receiverPanel.add(new RoundedButton(FoxShare.bundle.getString("enableReceiving"), e -> {
            Contact.receiving = true;
            Tray.trayIcon.setImage(Tray.greenFox);
        },OryColors.GREEN,22));
        receiverPanel.add(new RoundedButton(FoxShare.bundle.getString("disableReceiving"), e -> {
            Contact.receiving = true;
            Tray.trayIcon.setImage(Tray.redFox);
        },OryColors.RED,22));
        basePanel.add(receiverPanel);

        basePanel.add(new RoundedButton(FoxShare.bundle.getString("sendFiles"), e -> Sender.startSending(), OryColors.BLUE));

        basePanel.add(new RoundedButton(FoxShare.bundle.getString("quit"), e -> System.exit(0), OryColors.RED));

        this.add(basePanel);
        this.setSize(500,300);
        this.setVisible(true);
        this.toFront();
        frame = this;
    }

    public void sender() {
        hostNames = new JPanel(new GridLayout(0,1));
        hostNames.setBackground(Color.white);
        this.remove(basePanel);
        this.add(hostNames);
        this.setTitle(FoxShare.bundle.getString("selectDest"));
        SwingUtilities.updateComponentTreeUI(this);
    }
}
