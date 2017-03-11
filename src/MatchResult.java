import java.io.IOException;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;

public class MatchResult {
	public String result;
	public String name;
	public String setCode;
	public String scryfallId;
	public double score;
	
	public MatchResult(String result, double score) {
		super();
		this.result = result;
		this.score = score;
		String[] split = result.split("\\|");
		name = split[0];
		setCode = split[1];
		scryfallId = split[2];
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
			return MTGCardQuery.getCardByScryfallId(scryfallId);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
		return "MatchResult [name=" + name + ", setCode=" + setCode + ", score=" + score + "]";
	}
}
