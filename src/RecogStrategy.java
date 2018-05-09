import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.CardFace;

public abstract class RecogStrategy {
	public abstract void finalizeLoad();
	public abstract void clear();
	public void addFromFile(File handle)
	{
		ByteBuffer buf;
		try {
			buf = BufferUtils.getBuffer(handle);
		} catch (IOException e) {
			return;
		}
		BufferUtils.readUTF8(buf);
		buf.getInt();
		int rec = buf.getInt();
		for(int i=0;i<rec;i++)
		{
			String s = BufferUtils.readUTF8(buf);
			ImageDesc id = ImageDesc.readIn(buf);
			add(new DescContainer(id,s));
		}
	}
	
	public void addFromCard(Card card)
	{
		if(card.isMultifaced())
		{
			for(CardFace face:card.getFaces())
			{
				BufferedImage i = CardUtil.getImage(face,StaticConfigs.DEFAULT_ART_FORMAT);
				if(i!=null)
				{
					String key = face.getName()+"|"+card.getSetCode()+"|"+card.getScryfallUUID();
					try
					{
						add(new DescContainer(new ImageDesc(ImageUtil.getScaledImage(i)), key));
					}
					catch(Exception e)
					{
						System.err.println("Couldn't process card face: "+face.toString()+"; "+e.getLocalizedMessage()+"\n");
					}
				}
				else
				{
					System.err.println("Couldn't find card art for card face: "+face.getName()+
							" from "+card.toString()+"\n");
				}
			}
		}
		else
		{
			BufferedImage i = CardUtil.getImage(card,StaticConfigs.DEFAULT_ART_FORMAT);
			if(i!=null)
			{
				String key = card.getName()+"|"+card.getSetCode()+"|"+card.getScryfallUUID();
				try
				{
					add(new DescContainer(new ImageDesc(ImageUtil.getScaledImage(i)), key));
				}
				catch(Exception e)
				{
					System.err.println("Couldn't process card: "+card.toString()+"; "+e.getLocalizedMessage()+"\n");
				}
			}
			else
			{
				System.err.println("Couldn't find card art for card: "+card.toString()+"\n");
			}
		}
	}
	public abstract void add(DescContainer dc);
	public abstract MatchResult getMatch(ImageDesc id, double thresh);
	
	public abstract int size();
}
