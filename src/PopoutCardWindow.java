import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.CardFace;
import forohfor.scryfall.api.MTGCardQuery;

public class PopoutCardWindow extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	private static JLabel display = new JLabel();

	private static Image img;
	private static Card card;

	private static String cardId = "";
	private static String cardName = "";

	private static boolean init = false;
	Timer timer;

	private static boolean isOut = false;

	public PopoutCardWindow()
	{
		super("Card Preview");

		if(isOut)
		{
			return;
		}

		this.setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		if(!init)
		{
			clear();
			init = true;
		}
		JButton button = new JButton(new AbstractAction("Clear") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		this.add(display, BorderLayout.CENTER);
		this.add(button, BorderLayout.SOUTH);
		pack();
		getContentPane().setBackground(Color.WHITE);
		setResizable(false);
		setVisible(true);
		img=null;
		card=null;
		timer = new Timer(40,this);
		timer.start();
		isOut = true;
	}

	public void dispose()
	{
		timer.stop();
		super.dispose();
		isOut=false;
	}

	public static void clear()
	{
		ImageIcon icon = new ImageIcon(new BufferedImage(336, 469,BufferedImage.TYPE_INT_ARGB));
		display.setIcon(icon);
	}

	public static boolean isDisplayingCardId(String id,String name)
	{
		return (card!=null && cardId.equals(id) && cardName.equals(name));
	}

	public static void setDisplay(String id,String name)
	{
		if(isOut)
		{
			if(!isDisplayingCardId(id,name))
			{
				try {
					System.out.println(id);
					card = MTGCardQuery.getCardByScryfallId(id);
					cardId = card.getScryfallUUID();
					boolean found = false;
					if(card.isMultifaced())
					{
						for(CardFace face:card.getFaces())
						{
							if(face.getName().equals(name))
							{
								img = face.getCannonicalImage().getScaledInstance(336, 469, BufferedImage.SCALE_SMOOTH);
								cardName = face.getName();
								found = true;
							}
						}
					}
					if((!card.isMultifaced())||found==false)
					{
						cardName = card.getName();
						img = card.getCannonicalImage().getScaledInstance(336, 469, BufferedImage.SCALE_SMOOTH);
					}
					ImageIcon icon = new ImageIcon(img);
					display.setIcon(icon);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		repaint();
	}
}
