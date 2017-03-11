import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

import forohfor.scryfall.api.*;

public class RecogList {
	private ArrayList<String> dataString;
	private ArrayList<ImageDesc> dataDesc;
	
	private String name;
	
	public RecogList(String name)
	{
		dataString = new ArrayList<String>();
		dataDesc = new ArrayList<ImageDesc>();
		this.name = name;
	}
	
	public RecogList(File f) throws IOException
	{
		dataString = new ArrayList<String>();
		dataDesc = new ArrayList<ImageDesc>();
		DataInputStream in = new DataInputStream(new FileInputStream(f));
		name = in.readUTF();
		
		int rec = in.readInt();
		for(int i=0;i<rec;i++)
		{
			String s = in.readUTF();
			ImageDesc id = ImageDesc.readIn(in);
			dataString.add(s);
			dataDesc.add(id);
		}
		in.close();
	}
	
	public RecogList(Scanner scan) throws IOException
	{
		dataString = new ArrayList<String>();
		dataDesc = new ArrayList<ImageDesc>();
		while(scan.hasNextLine())
		{
			String name = scan.nextLine();
			ArrayList<Card> cards = MTGCardQuery.search("++!\""+name+"\"");
			for(Card c:cards)
			{
				try{
					BufferedImage i = ImageIO.read(new URL(c.getImageURI()));
					i = ImageUtil.getScaledImage(i);
					dataDesc.add(new ImageDesc(i));
					System.out.println(c);
					dataString.add(c.getName().toString());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		scan.close();
	}
	
	public void writeOut(File f) throws IOException
	{
		DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
		out.writeUTF(name);
		out.writeInt(dataDesc.size());
		for(int i=0;i<dataDesc.size();i++)
		{
			out.writeUTF(dataString.get(i));
			dataDesc.get(i).writeOut(out);
		}
		out.close();
	}
	
	public void addPair(ImageDesc desc, String str)
	{
		dataString.add(str);
		dataDesc.add(desc);
	}
	
	public MatchResult getMatch(ImageDesc in, double threshhold)
	{
		int ix = 0;
		double max = 0;
		for(int i=0;i<dataDesc.size();i++)
		{
			double score = ScoreMatch.matchScore(in, dataDesc.get(i));
			if(score>max)
			{
				max=score;
				ix=i;
			}
		}
		if(max>threshhold)
		{
			return new MatchResult(dataString.get(ix),max);
		}
		return null;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
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
}
