import java.awt.*;
import java.util.List;

public class Palette {
    public void setIsDark(boolean dark) {
        isDark = dark;
    }

    private boolean isDark = true;
    public boolean getIsDark() {
        return isDark;
    }

    private Color backgroundDark = new Color(18,32,35,255);
    public Color getBackgroundDark() {
        return backgroundDark;
    }

    private Color backgroundLight = new Color(255,255,255,255);
    public Color getBackground() {
        if (isDark) {
            return backgroundDark;
        }
        return backgroundLight;
    }

    private Color green = new Color(46,204,64,255);
    public Color getGreen() {
        return green;
    }

    private Color textColorDark = Color.WHITE;
    private Color textColorLight = backgroundDark;

    public Color getTextColor() {
        if (isDark) {
            return textColorDark;
        } else{
            return textColorLight;
        }
    }

    private  Color red = new Color(250, 160, 160);
    private  Color amber = new Color(255, 250, 160);
    //private  Color green = new Color(193, 225, 193);


    public Palette() {
    }
}
