import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Intro extends JPanel {
    private final Palette palette;
    private Image image;
    private int progress = 0;
    private final Timer loadingTimer;

    public Intro(Palette palette) {
        this.palette = palette;

        setPreferredSize(new Dimension(800, 300));

        image = new ImageIcon(
                Window.class.getResource("/assets/lancaster/logo.png")
        ).getImage();
        //System.out.println(image.getHeight(this) + " " + image.getWidth(this));

        loadingTimer = new Timer(50, e -> {
            if (progress < 100) {
                Random random = new Random();
                progress += random.nextInt(10) + 1;
                if (progress > 100) progress = 100;
                repaint();
            }
        });
        loadingTimer.setInitialDelay(1000);
        loadingTimer.start();

        setDoubleBuffered(true);
    }

    public void resetIntro() {
        progress = 0;
        loadingTimer.setInitialDelay(1000);
        loadingTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(palette.getBackgroundDark());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int imageWidth = getWidth();
        int imageHeight = (imageWidth * 15) / 20;

        if (imageHeight > getHeight()) {
            imageHeight = getHeight();
            imageWidth = (imageHeight * 20) / 15;
        }

        int x = (getWidth() - imageWidth) / 2;
        int y = (getHeight() - imageHeight) / 2 -100;

        g2d.drawImage(image, x, y, imageWidth, imageHeight, this);

        int barWidth = (int) (getWidth() * 0.75);
        int barHeight = 20;
        int barX = (getWidth() - barWidth) / 2;
        int barY = y + imageHeight - 80;

        g2d.setColor(Color.GRAY);
        g2d.fillRect(barX, barY, barWidth, barHeight);

        g2d.setColor(new Color(46,204,64,255));
        g2d.fillRect(barX, barY, (int) (barWidth * (progress / 100.0)), barHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

}
