import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;

public class DeckGenerator extends JFrame{

	private static final long serialVersionUID = 1L;
	private static ArrayList<String> ignore = new ArrayList<>();

	private static boolean isOut = false;
	
	static
	{
		ignore.add("plains");
		ignore.add("island");
		ignore.add("swamp");
		ignore.add("mountain");
		ignore.add("forest");
	}

	private JTextArea jt;
	private JButton gen;
	private JTextField namebox;

	public ArrayList<String> getCardNames(){
		String decklist = jt.getText();

		ArrayList<String> added = new ArrayList<>();

		for (String cardname : decklist.split("\n")) {
			cardname = cardname.trim();
			if (cardname.startsWith("SB:")) {
				cardname = cardname.replace("SB:", "");
				cardname = cardname.trim();
			}

			cardname = removeLeadingNumber(cardname);
			if (cardname.contains("\t")) {
				cardname = cardname.split("\t")[1];
			}

			if (!added.contains(cardname)) {
				added.add(cardname);
			}
		}
		return added;
	}

	public static String removeLeadingNumber(String line) {
		int lastNum = 0;
		while (lastNum < line.length() && Character.isDigit(line.charAt(lastNum))) {
			lastNum++;
		}
		return line.substring(lastNum).trim();
	}

	public DeckGenerator()
	{
		super("Deck generator");
		
		if(isOut)
		{
			return;
		}
		isOut=true;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		gen = new JButton("Generate Deck");
		namebox = new JTextField("Enter Deck Name");
		JScrollPane scroll = new JScrollPane();
		gen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				gen.setEnabled(false);
				writeDeck(SavedConfig.PATH);
				gen.setEnabled(true);
			}
		});

		JPanel bot = new JPanel();
		bot.setLayout(new BorderLayout());

		jt=new JTextArea(10,50);
		jt.setText("Paste Decklist Here");
		scroll.setViewportView(jt);
		setLayout(new BorderLayout());
		add(scroll,BorderLayout.CENTER);
		bot.add(namebox,BorderLayout.CENTER);
		bot.add(gen,BorderLayout.SOUTH);
		add(bot,BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	public void writeDeck(String path)
	{
		ListRecogStrat r = new ListRecogStrat(namebox.getText());
		
		String deckspath = SavedConfig.getDecksPath();
		File dir = new File(deckspath);
		dir.mkdirs();
		File f = new File(deckspath+"decks/"+namebox.getText().replace(" ", "_")+".dat");

		ArrayList<String> names = getCardNames();
		ArrayList<Card> cards = MTGCardQuery.toCardList(names, true);
		
		for(Card card:cards)
		{
			r.addFromCard(card);
		}

		try {
			r.writeOut(f);
			JOptionPane.showMessageDialog(null, 
					"Deck saved with "+r.size()+" unique cards from "+names.size()+" card names.", 
					"Deck Saved", JOptionPane.INFORMATION_MESSAGE, null);
		} catch (IOException e) {
			System.err.println("Write failed.\n");
		}
	}
	
	public void dispose()
	{
		super.dispose();
		isOut=false;
	}

}
