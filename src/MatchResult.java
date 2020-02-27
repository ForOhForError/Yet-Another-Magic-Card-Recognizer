import java.io.IOException;

import org.json.simple.JSONObject;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.JSONUtil;
import forohfor.scryfall.api.MTGCardQuery;

public class MatchResult {
	public String result;
	public double score;
	public JSONObject meta;
	
	public MatchResult(DescContainer result, double score) {
		super();
		this.result = result.id;
		this.score = score;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		return result;
	}
	
	public Card getCard()
	{
		try {
			return MTGCardQuery.getCardByScryfallId(result);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getName()
	{
		return JSONUtil.getStringData(meta, "name");
	}

	public String set()
	{
		return JSONUtil.getStringData(meta, "set");
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchResult other = (MatchResult) obj;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return JSONUtil.getStringData(meta, "name")+" : " + score;
	}
}
