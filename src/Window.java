import javax.swing.*;
import java.awt.*;

public class Window {

    static JFrame frame;
    public static JFrame getFrame() {
        return frame;
    }
    public static void setVisible(boolean b) {frame.setVisible(b);}

    static String title = "Lancasters Music Hall Booking System - Marketing";
    public static String getTitle() {
        return title;
    }
    public static void setTitle(String title) {
        Window.title = title;
    }

    static Image icon = new ImageIcon(
            Window.class.getResource("/assets/lancaster/small_logo.png")
    ).getImage();
    public static Image getIcon() {
        return icon;
    }
    public static void setIcon(Image icon) {
        Window.icon = icon;
        frame.setIconImage(getIcon());
    }

    public Window() {

        this.frame = new JFrame(getTitle());

        setIcon(getIcon());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setMinimumSize(new Dimension(1280, 720));
        frame.setSize(1280,720);
    }
}

