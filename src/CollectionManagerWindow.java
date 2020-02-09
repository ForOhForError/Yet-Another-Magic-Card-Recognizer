import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import forohfor.scryfall.api.MTGCardQuery;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import java.awt.Component;

class CollectionManagerWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private JTable leftTable;
    private CollectionData leftData = new CollectionData(false);
    private JTable rightTable;
    private CollectionData rightData = new CollectionData(true);

    public CollectionManagerWindow() {
        super("Collection Manager");
        setLayout(new BorderLayout(10,100));

        leftTable = new JTable(leftData);
        rightTable = new JTable(rightData);

        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        right.add(new JScrollPane(rightTable), BorderLayout.CENTER);
        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        left.add(new JScrollPane(leftTable), BorderLayout.CENTER);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center,BoxLayout.X_AXIS));
        JButton arrow = new JButton("=>");
        arrow.setAlignmentY(Component.CENTER_ALIGNMENT);
        arrow.addActionListener(e -> this.leftToRight());
        center.add(arrow);
        add(center,BorderLayout.CENTER);

        add(right, BorderLayout.EAST);
        add(left, BorderLayout.WEST);

        leftData.addCards(MTGCardQuery.search("t:noble"));

        pack();

        setVisible(true);
        setResizable(false);
    }

    private void leftToRight()
    {
        int sel = leftTable.getSelectedRow();
        if(sel != -1)
        {
            System.out.println(sel);
            rightData.addEntry(leftData.get(sel));
            rightData.fireTableChanged(null);
        }
    }

    public static void main(String[] args)
    {
        new CollectionManagerWindow();
    }
}