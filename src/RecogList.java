import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

public class RecogList extends RecogStrategy{
	public ArrayList<DescContainer> desc;
	private String name = "";

	private int sizeOfSet=0;

	public RecogList()
	{
		desc = new ArrayList<>();
	}

	public RecogList(String name)
	{
		desc = new ArrayList<>();
		this.name = name;
	}

	public RecogList(File f) throws IOException
	{
		desc = new ArrayList<>();
		ByteBuffer buf = BufferUtils.getBuffer(f);
		BufferUtils.readUTF8(buf);
		sizeOfSet = buf.getInt();
		int rec = buf.getInt();
		for(int i=0;i<rec;i++)
		{
			String s = BufferUtils.readUTF8(buf);
			ImageDesc id = ImageDesc.readIn(buf);
			desc.add(new DescContainer(id,s));
		}
	}

	public synchronized boolean add(RecogList l)
	{
		for(int i=0;i<l.desc.size();i++)
		{
			desc.add(l.desc.get(i));
		}
		return true;
	}



	public synchronized void clear()
	{
		desc.clear();
		name = "";
	}

	public synchronized void writeOut(File f) throws IOException
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

	public synchronized void add(DescContainer dc)
	{
		desc.add(dc);
	}

	public synchronized void printStringData()
	{
		for(int i=0;i<desc.size();i++)
		{
			System.out.println(desc.get(i).stringData);
		}
	}

	public synchronized MatchResult getMatch(ImageDesc in, double threshhold)
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

	public synchronized MatchResult getMatchTopOnly(ImageDesc in, double threshhold)
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

	public synchronized void sortByHash(ImageDesc id)
	{
		for(int i=0;i<desc.size();i++)
		{
			DescContainer d = desc.get(i);
			d.match = d.descData.compareHash(id);
		}
		Collections.sort(desc);
	}

	public synchronized void shuffle()
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
	
	public synchronized void finalizeLoad(){}

	public synchronized int size() {
		return desc.size();
	}
}
