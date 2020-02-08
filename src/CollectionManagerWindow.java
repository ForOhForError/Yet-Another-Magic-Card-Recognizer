import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;

class CollectionManagerWindow extends JFrame
{
    private static final long serialVersionUID = 1L;

    public CollectionManagerWindow()
    {
        super("Collection Manager");
        Dimension d;
        setLayout(new BorderLayout());

        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        JPanel left  = new JPanel();
        left.setLayout(new BorderLayout());

        add(right, BorderLayout.EAST);
        add(left, BorderLayout.WEST);

        setVisible(true);
    }

    public static void main(String[] args)
    {
        new CollectionManagerWindow();
    }
}