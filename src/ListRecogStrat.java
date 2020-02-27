import java.util.ArrayList;
import java.util.Collections;

public class ListRecogStrat extends RecognitionStrategy{
	public ArrayList<DescContainer> desc;

	public String getStratName()
	{
		return "Full Scan";
	}

	public String getStratDisplayName()
	{
		return "Full Scan (Slow, Legacy)";
	}

	public ListRecogStrat()
	{
		desc = new ArrayList<>();
	}

	public ListRecogStrat(String name)
	{
		desc = new ArrayList<>();
		this.name = name;
	}

	public synchronized boolean add(ListRecogStrat l)
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

	public synchronized void add(DescContainer dc)
	{
		desc.add(dc);
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
			return new MatchResult(desc.get(ix),max);
		}
		return null;
	}

	public synchronized void shuffle()
	{
		Collections.shuffle(desc);
	}
	public String getName() {
		return name;
	}
	
	public synchronized void finalizeLoad(){}

	public synchronized int size() {
		return desc.size();
	}

	public ArrayList<DescContainer> getContainers() {
		return desc;
	}
}
