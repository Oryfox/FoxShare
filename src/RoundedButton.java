import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

public class RoundedButton extends JPanel implements MouseListener {

    ActionListener actionListener;
    Color buttonColor;

    boolean hover;

    public RoundedButton(String text, ActionListener actionListener, Color color, int fontSize) {
        super(new BorderLayout());
        this.actionListener = actionListener;
        this.buttonColor = color;

        this.setOpaque(false);

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("fonts/AkayaTelivigala-Regular.ttf"));
            label.setFont(font.deriveFont(Font.PLAIN, fontSize));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        this.add(label, BorderLayout.CENTER);
        this.addMouseListener(this);
    }

    public RoundedButton(String text, ActionListener actionListener, Color color) {
        this(text, actionListener, color, 28);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hover) g.setColor(buttonColor.darker());
        else g.setColor(buttonColor);
        ((Graphics2D) g).fill(new RoundRectangle2D.Double(10, 10, this.getWidth() - 20, this.getHeight() - 20, 25, 25));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        actionListener.actionPerformed(null);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        hover = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hover = false;
        repaint();
    }
}
