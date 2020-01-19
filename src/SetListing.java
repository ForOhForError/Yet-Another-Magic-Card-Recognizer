import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;

import forohfor.scryfall.api.MTGCardQuery;
import forohfor.scryfall.api.Set;

public class SetListing {
	private static HashMap<String, Set> sets;
	private static ArrayList<Set> setList;
	private static final String CACHE_NAME = "cached_sets.json";

	static {
		sets = new HashMap<String, Set>();
		setList = new ArrayList<Set>();
	}

	public static void init() {
		for (Set set : MTGCardQuery.getSets()) {
			sets.put(set.getName(), set);
			setList.add(set);
		}
		if (sets.size() == 0) {
			try {
				readFromFile(CACHE_NAME);
			} catch (IOException | ParseException e) {
				System.err.println("Could not read cached set file; no sets loaded");
				JOptionPane.showMessageDialog(null, "Please connect to the internet and re-launch program to fetch set data.");
			}
		} else {
			try {
				writeToFile(CACHE_NAME);
			} catch (IOException e) {
				System.err.println("Could not write cached set file; continuing without casheing set data");
			}
		}
	}

	public static Set getSet(String name) {
		return sets.get(name);
	}

	public static ArrayList<Set> getSets() {
		return new ArrayList<Set>(setList);
	}

	public static void readFromFile(String name) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject sets_obj = (JSONObject)parser.parse(new FileReader(name));
		JSONArray arr = (JSONArray)((JSONObject)sets_obj).get("sets");
		for(Object o:arr)
		{
			Set set = new Set((JSONObject)o);
			sets.put(set.getName(), set);
			setList.add(set);
		}
	}

	@SuppressWarnings("unchecked")
	public static void writeToFile(String name) throws IOException
	{
		JSONObject jobj = new JSONObject();
		JSONArray sets = new JSONArray();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(Set set : setList)
		{
			JSONObject setObj = new JSONObject();
			setObj.put("id", set.getId());
			setObj.put("code", set.getCode());
			setObj.put("name",set.getName());
			setObj.put("search_uri", set.getSearchURI());
			setObj.put("block_code",set.getBlockCode());
			setObj.put("block_name",set.getBlockName());
			setObj.put("card_count",set.getCardCount());
			setObj.put("released_at",sdf.format(set.getReleasedAt()));
			setObj.put("parent_set_code",set.getParentSetCode());
			sets.add(setObj);
		}
		jobj.put("sets", sets);
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(name)));
		writer.write(jobj.toString());
		writer.flush();
		writer.close();
	}
}
