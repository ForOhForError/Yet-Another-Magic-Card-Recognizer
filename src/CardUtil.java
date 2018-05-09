import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.CardFace;

public class CardUtil {
	public static BufferedImage getImage(Card c,String format)
	{
		try
		{
			return ImageIO.read(new URL(getImageURI(c,format)));
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public static BufferedImage getImage(CardFace c,String format)
	{
		try
		{
			return ImageIO.read(new URL(c.getImageURI(format)));
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public static String getImageURI(Card c,String format)
	{
		if(c.isMultifaced())
		{
			return c.getFaces().get(0).getImageURI(format);
		}
		return c.getImageURI(format);
	}
}
