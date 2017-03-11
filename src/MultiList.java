import java.util.LinkedHashMap;

public class MultiList {
	private LinkedHashMap<String,RecogList> map;

	public MultiList()
	{
		map = new LinkedHashMap<>();
	}

	public void add(RecogList list)
	{
		map.put(list.getName(), list);
	}

	public void remove(String name)
	{
		if(map.containsKey(name))
		{
			map.remove(name);
		}
	}

	public boolean contains(String name)
	{
		return map.containsKey(name);
	}

	public void clear()
	{
		map.clear();
	}

	public MatchResult getMatch(ImageDesc id, double threshhold)
	{
		double max = 0;
		MatchResult ret = null;
		synchronized(this) {
			for(RecogList list:map.values())
			{
				MatchResult r = list.getMatch(id, threshhold);
				if(r!=null){
					if(r.score>max)
					{
						ret = r;
						max = r.score;
					}
				}
			}
		}
		return ret;
	}
}
