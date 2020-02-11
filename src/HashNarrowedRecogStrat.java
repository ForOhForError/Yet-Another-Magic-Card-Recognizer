import java.util.Collections;

public class HashNarrowedRecogStrat extends ListRecogStrat{

	public String getStratName()
	{
		return "Hash Narrowed";
	}

	public String getStratDisplayName()
	{
		return "Perceptual Hash Narrowing (Fast, Experimental)";
	}

	public HashNarrowedRecogStrat()
	{
		super();
	}

	@Override
	public synchronized MatchResult getMatch(ImageDesc in, double threshhold)
	{
		sortByHash(in);
		int ix = 0;
		double max = 0;
		int size = Math.min(desc.size(),StaticConfigs.LIMIT_TO_TOP_N_HASH_MATCH);

		for(int i=0;i<size;i++)
		{
			System.out.println(desc.get(i).stringData);
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
			d.match = id.compareHashWithFlip(d.descData);
		}
		Collections.sort(desc);
	}
}
