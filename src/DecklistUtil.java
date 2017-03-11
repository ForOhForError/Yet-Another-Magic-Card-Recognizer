import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class DecklistUtil {

	public static ArrayList<String> getAllIds(){
		try{
			ArrayList<String> ignore = new ArrayList<>();

			ignore.add("plains");
			ignore.add("island");
			ignore.add("swamp");
			ignore.add("mountain");
			ignore.add("forest");

			ArrayList<String> m_ids = new ArrayList<>();
			int page = 0;
			boolean run=true;
			while(run){
				System.out.println(page);
				URL oracle = new URL("http://api.deckbrew.com/mtg/cards?page="+page);

				BufferedReader in = new BufferedReader(
						new InputStreamReader(oracle.openStream()));

				String inputLine;
				String foundName = "";
				while ((inputLine = in.readLine()) != null){
					if(inputLine.equals("[]")){
						run=false;
						break;
					}

					String cleanline = new String(inputLine.getBytes(),"UTF-8").trim();
					if(cleanline.startsWith("\"name\"")){
						int l = cleanline.length();
						foundName = cleanline.substring(cleanline.indexOf(":")+3, l-2);
					}else if(cleanline.startsWith("\"multiverse_id\"")){
						if(!ignore.contains(foundName.trim().toLowerCase())){
							String num = cleanline.substring(cleanline.indexOf(":")+2,cleanline.indexOf(","));
							if(!num.equals("0")){
								m_ids.add(num);
							}
						}
					}
				}
				in.close();
				page++;
			}

			return m_ids;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<String> getMultiIds(String cardname){
		try{

			ArrayList<String> m_ids = new ArrayList<>();

			String escapedName = URLEncoder.encode(cardname, "UTF-8");

			URL oracle = new URL("http://api.deckbrew.com/mtg/cards?name="+escapedName);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(oracle.openStream()));

			String inputLine;
			String foundName = "";
			while ((inputLine = in.readLine()) != null){
				String cleanline = new String(inputLine.getBytes(),"UTF-8").trim();
				if(cleanline.startsWith("\"name\"")){
					int l = cleanline.length();
					foundName = cleanline.substring(cleanline.indexOf(":")+3, l-2);
				}else if(cleanline.startsWith("\"multiverse_id\"")){
					if(foundName.trim().equalsIgnoreCase(cardname.trim())){
						String num = cleanline.substring(cleanline.indexOf(":")+2,cleanline.indexOf(","));
						if(!num.equals("0")){
							m_ids.add(num);
						}
					}
				}
			}
			in.close();

			if(m_ids.isEmpty()){
				JOptionPane.showMessageDialog(null, "Card not found: "+cardname);
			}

			return m_ids;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static String removeLeadingNumber(String line){
		int lastNum = 0;
		while(lastNum < line.length() && Character.isDigit(line.charAt(lastNum))){
			lastNum++;
		}
		return line.substring(lastNum).trim();
	}

	public static ArrayList<String> decklistToIds(String decklist){
		ArrayList<String> m_ids = new ArrayList<>();
		ArrayList<String> added = new ArrayList<>();
		ArrayList<String> ignore = new ArrayList<>();

		ignore.add("plains");
		ignore.add("island");
		ignore.add("swamp");
		ignore.add("mountain");
		ignore.add("forest");

		for(String cardname:decklist.split("\n")){
			cardname = cardname.trim();

			if(cardname.startsWith("SB:	")){
				cardname = cardname.replace("SB: ", "");
			}

			try{
				Integer.parseInt(cardname);
				m_ids.add(cardname);
			}catch(Exception e){
				cardname = removeLeadingNumber(cardname);
				if(cardname.contains("\t")){
					cardname = cardname.split("\t")[1];
				}

				try{
					Integer.parseInt(cardname);
					m_ids.add(cardname);
				}catch(Exception ex){}

				if(!added.contains(cardname)){
					added.add(cardname);
					if(!ignore.contains(cardname.toLowerCase())){
						m_ids.addAll(getMultiIds(cardname));
					}
				}
			}
		}
		return m_ids;
	}

}
