import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

abstract class AreaRecognitionStrategy implements MouseInputListener
{
    public abstract ArrayList<MatchResult> recognize(BufferedImage in, RecognitionStrategy strat);

    public abstract String getStratName();

    public abstract String getStratDisplayName();

    public abstract void draw(Graphics g);

    public abstract void init(int width, int height);

    public abstract SettingsEntry getSettingsEntry();

    public String toString()
    {
        return getStratDisplayName();
    }
}