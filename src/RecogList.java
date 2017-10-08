import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class RecogList {
	public ArrayList<DescContainer> desc;
	private String name = "";
	
	private long setTrack=0;
	private HashMap<String,Long> names;
	
	private int sizeOfSet=0;
	
	public RecogList()
	{
		desc = new ArrayList<>();
		names = new HashMap<>();
	}
	
	public RecogList(String name)
	{
		desc = new ArrayList<>();
		names = new HashMap<>();
		names.put(name, setTrack);
		setTrack++;
		this.name = name;
	}
	
	public RecogList(File f) throws IOException
	{
		desc = new ArrayList<>();
		ByteBuffer buf = BufferUtils.getBuffer(f);
		names = new HashMap<>();
		String name = BufferUtils.readUTF8(buf);
		names.put(name,setTrack);
		sizeOfSet = buf.getInt();
		int rec = buf.getInt();
		for(int i=0;i<rec;i++)
		{
			String s = BufferUtils.readUTF8(buf);
			ImageDesc id = ImageDesc.readIn(buf);
			desc.add(new DescContainer(id,s,setTrack));
		}
		setTrack++;
	}
	
	public void remove(String set)
	{
		if(names.containsKey(set))
		{
			long l = names.get(set);
			int i = 0;
			while(i<desc.size())
			{
				long l2 = desc.get(i).setNo;
				if(l2==l)
				{
					desc.remove(i);
				}
				else
				{
					i++;
				}
			}
		}
	}
	
	public boolean add(RecogList l, String n)
	{
		if(names.containsKey(n))
		{
			return false;
		}
		else
		{
			names.put(n, setTrack);
			for(int i=0;i<l.desc.size();i++)
			{
				DescContainer c = new DescContainer(l.desc.get(i));
				c.setNo = setTrack;
				desc.add(c);
			}
			setTrack++;
			return true;
		}
	}
	
	
	
	public void clear()
	{
		desc.clear();
		names.clear();
		setTrack=0;
		name = "";
	}
	
	public void writeOut(File f) throws IOException
	{
		DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
		out.writeUTF(name);
		out.writeInt(sizeOfSet);
		out.writeInt(desc.size());
		for(int i=0;i<desc.size();i++)
		{
			out.writeUTF(desc.get(i).stringData);
			desc.get(i).descData.writeOut(out);
		}
		out.close();
	}
	
	public void addPair(ImageDesc id, String str)
	{
		desc.add(new DescContainer(id,str,setTrack));
	}
	
	public void printStringData()
	{
		for(int i=0;i<desc.size();i++)
		{
			System.out.println(desc.get(i).stringData);
		}
	}
	
	public MatchResult getMatch(ImageDesc in, double threshhold)
	{
		int ix = 0;
		double max = 0;
		for(int i=0;i<desc.size();i++)
		{
			double score = in.compareSURF(desc.get(i).descData);
			if(score>max)
			{
				max=score;
				ix=i;
			}
		}
		if(max>threshhold)
		{
			return new MatchResult(desc.get(ix).stringData,max);
		}
		return null;
	}
	
	public MatchResult getMatchTopOnly(ImageDesc in, double threshhold)
	{
		sortByHash(in);
		int ix = 0;
		double max = 0;
		int size = Math.min(desc.size(),StaticConfigs.LIMIT_TO_TOP_N_HASH_MATCH);
		
		for(int i=0;i<size;i++)
		{
			double score = in.compareSURF(desc.get(i).descData);
			if(score>max)
			{
				max=score;
				ix=i;
			}
		}
		if(max>threshhold)
		{
			return new MatchResult(desc.get(ix).stringData,max);
		}
		return null;
	}
	
	public void sortByHash(ImageDesc id)
	{
		for(int i=0;i<desc.size();i++)
		{
			DescContainer d = desc.get(i);
			d.match = d.descData.compareHash(id);
		}
		Collections.sort(desc);
	}
	
	public void shuffle()
	{
		Collections.shuffle(desc);
	}
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
	
	public void setSizeOfSet(int i)
	{
		sizeOfSet = i;
	}
}
