import java.util.ArrayList;
import java.util.HashMap;

import forohfor.scryfall.api.MTGCardQuery;
import forohfor.scryfall.api.Set;

public class SetListing {
	private static HashMap<String,Set> sets;
	private static ArrayList<Set> setList;
	
	static
	{
		sets = new HashMap<String,Set>();
		setList = new ArrayList<Set>();
	}
	
	public static void init()
	{
		for(Set set: MTGCardQuery.getSets())
		{
			sets.put(set.getName(), set);
			setList.add(set);
		}
	}
	
	public static Set getSet(String name)
	{
		return sets.get(name);
	}
	
	public static ArrayList<Set> getSets()
	{
		return new ArrayList<Set>(setList);
	}
}
