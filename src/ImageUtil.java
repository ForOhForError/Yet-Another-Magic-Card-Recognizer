import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.CardFace;
import forohfor.scryfall.api.MTGCardQuery;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageUtil
{


    public static BufferedImage toABGR(BufferedImage in)
    {
        if (in.getType() != BufferedImage.TYPE_3BYTE_BGR)
        {
            BufferedImage i2 = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            i2.getGraphics().drawImage(in, 0, 0, null);
            in = i2;
        }
        return in;
    }

    public static BufferedImage getScaledImage(BufferedImage src, double fraction)
    {
        int finalw = (int) (src.getWidth() * fraction);
        int finalh = (int) (src.getHeight() * fraction);
        double factor = 1.0d;
        if (src.getWidth() > src.getHeight())
        {
            factor = ((double) src.getHeight() / (double) src.getWidth());
            finalh = (int) (finalw * factor);
        } else
        {
            factor = ((double) src.getWidth() / (double) src.getHeight());
            finalw = (int) (finalh * factor);
        }

        BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, finalw, finalh, null);
        g2.dispose();
        return resizedImg;
    }

    public static BufferedImage rotate(BufferedImage img, double angle)
    {
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));

        int w = img.getWidth(null), h = img.getHeight(null);

        int neww = (int) Math.floor(w * cos + h * sin),
                newh = (int) Math.floor(h * cos + w * sin);

        BufferedImage bimg = new BufferedImage(neww, newh, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = bimg.createGraphics();

        g.translate((neww - w) / 2, (newh - h) / 2);
        g.rotate(Math.toRadians(angle), w / 2, h / 2);
        g.drawRenderedImage(img, null);
        g.dispose();

        return bimg;
    }

    public static final int SQUARE_SIZE = 300;

    public static BufferedImage getScaledImage(BufferedImage src)
    {
        int finalw = SQUARE_SIZE;
        int finalh = SQUARE_SIZE;
        double factor = 1.0d;
        if (src.getWidth() > src.getHeight())
        {
            factor = ((double) src.getHeight() / (double) src.getWidth());
            finalh = (int) (finalw * factor);
        } else
        {
            factor = ((double) src.getWidth() / (double) src.getHeight());
            finalw = (int) (finalh * factor);
        }

        BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, finalw, finalh, null);
        g2.dispose();
        return resizedImg;
    }

    public static BufferedImage getDescContainerImage(DescContainer dc)
    {
        BufferedImage i = dc.getImage();
        if (i == null)
        {
            try
            {
                Card card = MTGCardQuery.getCardByScryfallId(dc.getScryfallId());
                if (card.isMultifaced())
                {
                    for (CardFace face : card.getCardFaces())
                    {
                        if (face.getName().equals(dc.getName()))
                        {
                            return face.getImage();
                        }
                    }
                }
                return card.getImage();
            } catch (IOException e)
            {
            }
        }
        return i;
    }

    public static BufferedImage getImageFromUrl(String u)
    {
        try
        {
            URL url = new URL(u);
            return ImageIO.read(url);
        } catch (Exception e)
        {
            return null;
        }
    }

    public static BufferedImage getCardImage(String multiid)
    {
        try
        {
            URL url = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + multiid
                    + "&type=card");
            return ImageIO.read(url);
        } catch (Exception e)
        {
            return null;
        }
    }
}
