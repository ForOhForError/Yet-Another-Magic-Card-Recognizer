import forohfor.scryfall.api.JSONUtil;
import org.json.simple.JSONObject;

import java.awt.image.BufferedImage;

public class DescContainer implements Comparable<DescContainer>
{
    public DescContainer(ImageDesc descData, String id, JSONObject jo, BufferedImage i)
    {
        super();
        this.descData = descData;
        this.id = id;
        this.jsonData = jo;
        this.img = i;
    }

    public DescContainer(DescContainer d)
    {
        this.descData = d.descData;
        this.id = d.id;
        this.jsonData = d.jsonData;
        this.img = d.img;
    }

    private ImageDesc descData;
    private String id;
    private JSONObject jsonData;
    private BufferedImage img;

    private double match = 0;

    @Override
    public int compareTo(DescContainer arg0)
    {
        return Double.compare(arg0.match, this.match);
    }

    public String getID()
    {
        return id;
    }

    public String getScryfallId()
    {
        return JSONUtil.getStringData(jsonData, "scryfall-id");
    }

    public String getName()
    {
        return JSONUtil.getStringData(jsonData, "name");
    }

    public String getSet()
    {
        return JSONUtil.getStringData(jsonData, "set");
    }

    public ImageDesc getDescData()
    {
        return descData;
    }

    public JSONObject getJSON()
    {
        return jsonData;
    }

    public BufferedImage getImage()
    {
        return img;
    }

    public void setMatchScore(double score)
    {
        match = score;
    }
}
