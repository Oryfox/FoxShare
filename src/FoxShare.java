import java.awt.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class FoxShare {

    static ResourceBundle bundle;

    public static void main(String[] args) throws AWTException {
        System.setProperty("apple.awt.UIElement", "true");

        try {
            bundle = ResourceBundle.getBundle("ResourceBundles/Strings", Locale.getDefault());
        } catch (MissingResourceException e) {
            System.out.println("Language not supported - Using English");
            bundle = ResourceBundle.getBundle("ResourceBundles/Strings", Locale.ENGLISH);
        }

        Tray.start();
        Contact.startProvider();
    }
}
