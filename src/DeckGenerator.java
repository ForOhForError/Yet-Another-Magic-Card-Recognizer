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

			if (!added.contains(cardname) && !ignore.contains(cardname.toLowerCase())) {
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
		new File(SavedConfig.getDecksPath()).mkdirs();
		File f = new File(SavedConfig.getDeckPath(namebox.getText()));

		ArrayList<String> names = getCardNames();
		ArrayList<Card> cards = MTGCardQuery.toCardList(names, true);
		final OperationBar bar = RecogApp.INSTANCE.getOpBar();
		if(bar.setTask("Generating Deck...",cards.size()))
		{
			new Thread()
			{
				public void run()
				{
					for(Card card:cards)
					{
						bar.setSubtaskName(String.format("%s (%s)", card.getName(), card.getSetCode()));
						r.addFromCard(card);
						bar.progressTask();
					}
					try {
						r.writeOut(f);
						JOptionPane.showMessageDialog(null, 
								"Deck saved with "+r.size()+" unique cards from "+names.size()+" card names.", 
								"Deck Saved", JOptionPane.INFORMATION_MESSAGE, null);
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, 
								"Deck couldn't be saved", 
								"Error", JOptionPane.ERROR_MESSAGE, null);
					}
				}
			}.start();
		}
	}

}
