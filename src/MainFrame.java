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

        basePanel = new JPanel(new GridLayout(0,1));
        basePanel.setBackground(Color.white);

        basePanel.add(new RoundedButton(FoxShare.bundle.getString("toggleReceiving"), e -> {
            Contact.receiving = !Contact.receiving;
            if (Contact.receiving)
                Tray.toggleReceiving.setLabel(FoxShare.bundle.getString("disableReceiving"));
            else
                Tray.toggleReceiving.setLabel(FoxShare.bundle.getString("enableReceiving"));
        }, OryColors.BLUE));

        basePanel.add(new RoundedButton(FoxShare.bundle.getString("sendFiles"), e -> Sender.startSending(), OryColors.BLUE));

        basePanel.add(new RoundedButton(FoxShare.bundle.getString("quit"), e -> System.exit(0), OryColors.RED));

        this.add(basePanel);
        this.setSize(500,300);
        this.setVisible(true);
        frame = this;
    }

    public void sender() {
        hostNames = new JPanel(new GridLayout(0,1));
        hostNames.setBackground(Color.white);
        this.remove(basePanel);
        this.add(hostNames);
        SwingUtilities.updateComponentTreeUI(this);
    }
}
