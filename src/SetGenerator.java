import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;
import forohfor.scryfall.api.Set;

public class SetGenerator extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private JTextArea jt;
	private JButton gen;
	
	private Thread genSets;
	
	private static volatile boolean runThread = true;
	
	public SetGenerator()
	{
		super("Set generator");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		gen = new JButton("Generate sets");
		JScrollPane scroll = new JScrollPane();
		gen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				runThread = true;
				genSets = new Thread(){
					public void run()
					{
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
					}
				};
				genSets.start();
			}
		});
		jt=new JTextArea(10,50);
		jt.setEditable(false);
		scroll.setViewportView(jt);
		setLayout(new BorderLayout());
		add(scroll,BorderLayout.CENTER);
		add(gen,BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}
	
	public void dispose()
	{
		runThread = false;
		super.dispose();
	}
	
	public void writeSet(Set set, String path, boolean ignoreBasics)
	{
		RecogList r = new RecogList(set.getName());
		File f = new File(path+set.getCode()+".dat");

		jt.append(set.getName()+"...\n");
		if(f.exists() && f.isFile())
		{
			jt.append("Set exists. Skipping.\n");
		}
		else
		{
			ArrayList<Card> cards = MTGCardQuery.getCardsFromURI(set.getSearchUri());

			for(Card card:cards)
			{
				if(!ignoreBasics || !card.getTypeLine().toLowerCase().contains("basic"))
				{
					BufferedImage i = ImageUtil.getImageFromUrl(card.getImageURI());
					if(i!=null)
					{
						String key = card.getName()+"|"+card.getSetCode()+"|"+card.getScryfallUUID();
						r.addPair(new ImageDesc(ImageUtil.getScaledImage(i)), key);
					}
					else
					{
						jt.append("Couldn't find card art for card: "+card.toString()+"\n");
					}
				}
			}

			try {
				r.writeOut(f);
			} catch (IOException e) {
				jt.append("Write failed.\n");
			}
		}
	}
}
