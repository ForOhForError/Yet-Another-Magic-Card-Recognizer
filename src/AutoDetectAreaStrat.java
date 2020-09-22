import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

class AutoDetectAreaStrat extends AreaRecognitionStrategy
{

    private ArrayList<MatchResult> results = new ArrayList<MatchResult>();
    private ArrayList<ContourBoundingBox> bounds = new ArrayList<ContourBoundingBox>();
    private AutoDetectSettings settings = new AutoDetectSettings();

    @Override
    public ArrayList<MatchResult> recognize(BufferedImage in, RecognitionStrategy strat)
    {
        results.clear();
        bounds = CardBoundingBoxFinder.process(in, settings.getRemoveBackground());
        for (ContourBoundingBox bound : bounds)
        {
            BufferedImage norm = ImageUtil.getScaledImage(bound.getTransformedImage(in, false));
            BufferedImage flip = ImageUtil.getScaledImage(bound.getTransformedImage(in, true));
            ImageDesc i = new ImageDesc(norm, flip);
            MatchResult mr = strat.getMatch(i, SettingsPanel.RECOG_THRESH / 100.0);
            if (mr != null)
            {
                results.add(mr);
            }
        }
        return results;
    }

    @Override
    public String getStratName()
    {
        return "auto-detect";
    }

    @Override
    public String getStratDisplayName()
    {
        return "Auto-Detect Card Bounds (Recommended)";
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
    }

    @Override
    public void draw(Graphics g)
    {
        for (ContourBoundingBox bb : bounds)
        {
            bb.draw(g);
        }
    }

    @Override
    public void init(int width, int height)
    {
        settings.init();
    }

    @Override
    public SettingsEntry getSettingsEntry()
    {
        return settings;
    }

}