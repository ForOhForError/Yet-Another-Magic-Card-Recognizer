import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.CardFace;

public abstract class RecognitionStrategy {

	private static JSONParser PARSER = new JSONParser();

	protected String name;

	public void addFromFile(File handle)
	{
		try
		{
			ByteBuffer buf;
			try {
				buf = BufferUtils.getBuffer(handle);
			} catch (IOException e) {
				return;
			}
			name = BufferUtils.readUTF8(buf);
			int rec = buf.getInt();
			for(int i=0;i<rec;i++)
			{
				String s = BufferUtils.readUTF8(buf);
				JSONObject jobj = (JSONObject)PARSER.parse(BufferUtils.readUTF8(buf));
				ImageDesc id = ImageDesc.readIn(buf);
				DescContainer dc = new DescContainer(id,s,jobj);
				if( SavedConfig.LOAD_BASICS || (!CardUtils.isEssentialBasic(dc.getName())) )
				{
					add(dc);
				}
			}
		}
		catch(Exception e)
		{
			return;
		}
	}

	public synchronized void writeOut(File f) throws IOException
	{
		DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
		out.writeUTF(name);
		out.writeInt(size());
		ArrayList<DescContainer> desc = getContainers();
		for(int i=0;i<size();i++)
		{
			out.writeUTF(desc.get(i).id);
			out.writeUTF(desc.get(i).jsonData.toJSONString());
			desc.get(i).descData.writeOut(out);
		}
		out.close();
	}
	
	public void addFromCard(Card card)
	{
		BufferedImage top_img = card.getImage(StaticConfigs.DEFAULT_ART_FORMAT);
		if(top_img==null && card.isMultifaced())
		{
			for(CardFace face:card.getCardFaces())
			{
				BufferedImage i = face.getImage(StaticConfigs.DEFAULT_ART_FORMAT);
				if(i!=null)
				{
					try
					{
						add(new DescContainer(
							new ImageDesc(ImageUtil.getScaledImage(i)), 
							card.getScryfallUUID().toString(), 
							card.getJSONData()
							));
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
			if(top_img!=null)
			{
				try
				{
					add(new DescContainer(
							new ImageDesc(ImageUtil.getScaledImage(top_img)), 
							card.getScryfallUUID().toString(), 
							card.getJSONData()
							));
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

	public abstract ArrayList<DescContainer> getContainers();

	public abstract void finalizeLoad();

	public abstract void clear();

	public abstract void add(DescContainer dc);

	public abstract MatchResult getMatch(ImageDesc id, double thresh);

	public abstract int size();

	public abstract String getStratName();

	public abstract String getStratDisplayName();

	public String toString()
	{
		return getStratDisplayName();
	}

	public static String getNameFromFile(File f)
	{
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			String name = in.readUTF();
			in.close();
			return name;
		}
		catch(IOException e)
		{
			return null;
		}
	}

	public static int getSizeFromFile(File f)
	{
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			in.readUTF();
			int size = in.readInt();
			in.close();
			return size;
		}
		catch(IOException e)
		{
			return -1;
		}
	}
}
