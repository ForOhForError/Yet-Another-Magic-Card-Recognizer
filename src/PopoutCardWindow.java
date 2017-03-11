import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.*;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;

public class PopoutCardWindow extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	private static JLabel display = new JLabel();

	private static BufferedImage img;
	private static Card card;

	private static boolean init = false;
	Timer timer;

	public PopoutCardWindow()
	{
		super("Card Popout");
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
	}
	
	public void dispose()
	{
		timer.stop();
		super.dispose();
	}

	public static void clear()
	{
		ImageIcon icon = new ImageIcon(new BufferedImage(336, 469,BufferedImage.TYPE_INT_ARGB));
		display.setIcon(icon);
	}

	public static boolean isDisplayingCardId(String id)
	{
		return (card!=null && card.getScryfallUUID().equals(id));
	}

	public static void setDisplay(String id)
	{
		if(!isDisplayingCardId(id))
		{
			try {
				System.out.println(id);
				card = MTGCardQuery.getCardByScryfallId(id);
				img = ImageUtil.getImageFromUrl(card.getImageURI());
				ImageIcon icon = new ImageIcon(img);
				display.setIcon(icon);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		repaint();
	}
}
