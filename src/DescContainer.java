import org.json.simple.JSONObject;

import forohfor.scryfall.api.JSONUtil;

public class DescContainer implements Comparable<DescContainer>{
	public DescContainer(ImageDesc descData, String id, JSONObject jo) {
		super();
		this.descData = descData;
		this.id = id;
		this.jsonData = jo;
	}
	
	public DescContainer(DescContainer d)
	{
		this.descData = d.descData;
		this.id = d.id;
		this.jsonData = d.jsonData;
	}
	
	public ImageDesc descData;
	public String id;
	public JSONObject jsonData;
	
	public double match = 0;

	@Override
	public int compareTo(DescContainer arg0) {
		return Double.compare(arg0.match,this.match);
	}
	
	public String getID()
	{
		return id;
	}

	public String getName()
	{
		return JSONUtil.getStringData(jsonData,"name");
	}
}
