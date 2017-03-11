import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;

public class GenSearchDb {
	public static void main(String[] args)
	{
		RecogList r = new RecogList("Modern");
		System.out.println("getting card data");
		ArrayList<Card> cards = MTGCardQuery.search("++f:modern");
		int ix =0;
		for(Card card:cards)
		{
			ix++;
			System.out.println(ix+"/"+cards.size());
			BufferedImage i = ImageUtil.getImageFromUrl(card.getImageURI());
			if(i!=null)
			{
				r.addPair(new ImageDesc(ImageUtil.getScaledImage(i)), card.getName());
			}
			else
			{
				System.out.println("Couldn't find card art for card: "+card.toString());
			}
		}
		
		try {
			r.writeOut(new File("modern.dat"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
