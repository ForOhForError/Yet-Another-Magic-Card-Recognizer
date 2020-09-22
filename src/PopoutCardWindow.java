import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.CardFace;
import forohfor.scryfall.api.MTGCardQuery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PopoutCardWindow extends JFrame implements ActionListener
{
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

        if (isOut)
        {
            return;
        }

        this.setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        if (!init)
        {
            clear();
            init = true;
        }
        JButton button = new JButton(new AbstractAction("Clear")
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                clear();
            }
        });
        this.add(display, BorderLayout.CENTER);
        this.add(button, BorderLayout.SOUTH);
        pack();
        getContentPane().setBackground(Color.WHITE);
        setResizable(false);
        setVisible(true);
        img = null;
        card = null;
        timer = new Timer(40, this);
        timer.start();
        isOut = true;
    }

    public void dispose()
    {
        timer.stop();
        super.dispose();
        isOut = false;
    }

    public static void clear()
    {
        ImageIcon icon = new ImageIcon(new BufferedImage(336, 469, BufferedImage.TYPE_INT_ARGB));
        display.setIcon(icon);
    }

    public static boolean isDisplayingCardId(DescContainer dc)
    {
        return (card != null && cardId.equals(dc.getID()) && cardName.equals(dc.getName()));
    }

    public static void setDisplay(DescContainer dc)
    {
        if (isOut)
        {
            if (!isDisplayingCardId(dc))
            {
                BufferedImage i = dc.getImage();
                if (i == null)
                {
                    try
                    {
                        card = MTGCardQuery.getCardByScryfallId(dc.getScryfallId());
                        cardId = card.getScryfallUUID().toString();
                        boolean found = false;
                        if (card.isMultifaced())
                        {
                            for (CardFace face : card.getCardFaces())
                            {
                                if (face.getName().equals(dc.getName()))
                                {
                                    img = face.getImage();
                                    cardName = face.getName();
                                    found = true;
                                }
                            }
                        }
                        if ((!card.isMultifaced()) || found == false)
                        {
                            cardName = card.getName();
                            img = card.getImage();
                        }
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                } else
                {
                    img = i;
                }
                if (img != null)
                {
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(336, 469, BufferedImage.SCALE_SMOOTH));
                    display.setIcon(icon);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        repaint();
    }
}
