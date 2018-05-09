import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;
import forohfor.scryfall.api.Set;

public class SetGenerator extends JFrame{
	private static final long serialVersionUID = 1L;

	private static final String[] setTypes = 
		{ 
				"all",
				"expansion+core",
				"core",
				"expansion",
				"masters",
				"masterpiece",
				"from_the_vault",
				"premium_deck",
				"duel_deck",
				"commander",
				"planechase",
				"conspiracy",
				"archenemy",
				"vanguard",
				"funny",
				"starter",
				"box",
				"promo",
				"token",
				""
		};

	private JTextArea jt;
	private JButton gen;
	private JComboBox<String> typeBox;

	private Thread genSets;

	private static volatile boolean runThread = true;
	private static boolean isOut = false;

	public SetGenerator()
	{
		super("Set generator");
		if(isOut)
		{
			return;
		}
		isOut=true;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		gen = new JButton("Generate sets");
		typeBox = new JComboBox<>(setTypes);
		JScrollPane scroll = new JScrollPane();
		gen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				runThread = true;
				genSets = new Thread(){
					public void run()
					{
						typeBox.setEnabled(false);
						gen.setEnabled(false);
						ArrayList<Set> sets = MTGCardQuery.getSets();
						for(Set s:sets){
							writeSet(s,SavedConfig.PATH,true);
							if(!runThread){
								System.out.println("stopped");
								return;
							}
						}
						gen.setEnabled(true);
						typeBox.setEnabled(true);
					}
				};
				genSets.start();
			}
		});
		
		JPanel bot = new JPanel();
		bot.setLayout(new BorderLayout());
		
		jt=new JTextArea(10,50);
		jt.setEditable(false);
		scroll.setViewportView(jt);
		setLayout(new BorderLayout());
		add(scroll,BorderLayout.CENTER);
		bot.add(typeBox,BorderLayout.CENTER);
		bot.add(gen,BorderLayout.SOUTH);
		add(bot,BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	public void dispose()
	{
		runThread = false;
		super.dispose();
		isOut=false;
	}

	public void writeSet(Set set, String path, boolean ignoreBasics)
	{
		RecogList r = new RecogList(set.getName());
		r.setSizeOfSet(set.getCardCount());
		File f = new File(path+set.getCode()+".dat");

		String setType = set.getSetType();
		String selectedType = (String)typeBox.getSelectedItem();
		
		if("expansion+core".equals(selectedType))
		{
			if(!(setType.equals("core")||setType.equals("expansion")))
			{
				return;
			}
		}
		else if(!selectedType.equals("all"))
		{
			if(!setType.equals(selectedType))
			{
				return;
			}
		}
		
		jt.append(set.getName()+"..."+"\n");
		
		if((f.exists() && f.isFile()))
		{
			int size=RecogList.getSizeFromFile(f);
			if(size==set.getCardCount())
			{
				jt.append("Set exists. Skipping.\n");
				return;
			}
			else
			{
				jt.append("Outdated set file. Updating.\n");
			}
		}
		ArrayList<Card> cards = MTGCardQuery.getCardsFromURI(set.getSearchUri());

		for(Card card:cards)
		{
			r.addFromCard(card);
		}

		try {
			r.writeOut(f);
		} catch (IOException e) {
			jt.append("Write failed.\n");
		}
	}
	
	
}
