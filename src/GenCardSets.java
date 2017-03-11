import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;
import forohfor.scryfall.api.Set;

public class GenCardSets {

	public static void writeSet(Set set, String path, boolean ignoreBasics)
	{
		RecogList r = new RecogList(set.getName());
		File f = new File(path+set.getCode()+".dat");

		System.out.println(set.getName()+"...");

		if(f.exists() && f.isFile())
		{
			System.out.println("Set exists. Skipping.");
		}
		else
		{
			ArrayList<Card> cards = MTGCardQuery.getCardsFromURI(set.getSearchUri());

			for(Card card:cards)
			{
				if(!ignoreBasics || !card.getTypeLine().toLowerCase().contains("basic"))
				{
					BufferedImage i = ImageUtil.getImageFromUrl(card.getImageURI());
					if(i!=null)
					{
						String key = card.getName()+"|"+card.getSetCode()+"|"+card.getScryfallUUID();
						r.addPair(new ImageDesc(ImageUtil.getScaledImage(i)), key);
					}
					else
					{
						System.out.println("Couldn't find card art for card: "+card.toString());
					}
				}
			}

			try {
				r.writeOut(f);
			} catch (IOException e) {
				System.err.println("Write failed.");
			}
		}
	}

	public static void main(String[] args) throws IOException
	{
		ArrayList<Set> sets = MTGCardQuery.getSets();
		for(Set s:sets){
			writeSet(s,"D:/MTG/",true);
		}
	}
}
