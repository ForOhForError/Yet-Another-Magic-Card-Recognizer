import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import forohfor.scryfall.api.MTGCardQuery;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.BoxLayout;
import java.awt.Component;

class CollectionManagerWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private JTable leftTable;
    private CollectionData leftData = new CollectionData(false);
    private JTable rightTable;
    private CollectionData rightData = new CollectionData(true);
    private JFileChooser fileChooser;

    public CollectionManagerWindow() {
        super("Collection Manager");

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new CSVFilter());

        JPanel right = new JPanel();
        JPanel left = new JPanel();
        JPanel center = new JPanel();
        JPanel rightButtons = new JPanel();

        leftTable = new JTable(leftData);
        rightTable = new JTable(rightData);

        setLayout(new BorderLayout(10, 10));

        right.setLayout(new BorderLayout());
        rightButtons.setLayout(new FlowLayout());
        left.setLayout(new BorderLayout());
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));

        JButton arrow = new JButton("=>");
        arrow.setAlignmentY(Component.CENTER_ALIGNMENT);
        arrow.addActionListener(e -> this.leftToRight());

        JButton m4 = new JButton("-4");
        m4.addActionListener(e -> this.offsetRightCounts(-4));

        JButton m1 = new JButton("-1");
        m1.addActionListener(e -> this.offsetRightCounts(-1));

        JButton p1 = new JButton("+1");
        p1.addActionListener(e -> this.offsetRightCounts(1));

        JButton p4 = new JButton("+4");
        p4.addActionListener(e -> this.offsetRightCounts(4));

        JButton foil = new JButton("Toggle Foil");
        foil.addActionListener(e -> this.toggleFoils());

        JButton save = new JButton("Save CSV");
        save.addActionListener(e -> this.save());

        JButton load = new JButton("Load CSV");
        load.addActionListener(e -> this.load());

        rightButtons.add(m4);
        rightButtons.add(m1);
        rightButtons.add(foil);
        rightButtons.add(p1);
        rightButtons.add(p4);
        rightButtons.add(save);
        rightButtons.add(load);

        right.add(new JScrollPane(rightTable), BorderLayout.CENTER);
        right.add(rightButtons, BorderLayout.SOUTH);
        left.add(new JScrollPane(leftTable), BorderLayout.CENTER);
        center.add(arrow);

        add(center, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        add(left, BorderLayout.WEST);

        leftData.addCards(MTGCardQuery.search("t:noble"));

        pack();
        setVisible(true);
        setResizable(false);
    }

    private class CSVFilter extends FileFilter {
        public String getDescription() {
            return "CSV Data Files (*.csv)";
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            } else {
                String filename = f.getName().toLowerCase();
                return filename.endsWith(".csv");
            }
        }
    }

    private void save()
    {
        if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) 
        {
            rightData.saveToFile(fileChooser.getSelectedFile());
        }
    }

    private void load()
    {
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
        {
            rightData.loadFromFile(fileChooser.getSelectedFile());
        }
    }

    private void toggleFoils()
    {
        int[] sels = rightTable.getSelectedRows();
        for(int sel:sels)
        {
            rightData.toggleFoil(sel);
        }
    }

    private void offsetRightCounts(int offset)
    {
        int[] sels = rightTable.getSelectedRows();
        for(int sel:sels)
        {
            rightData.offsetCount(sel, offset);
        }
        if(offset < 0)
        {
            rightData.removeEmptyRows();
        }
    }

    private void leftToRight()
    {
        int[] sels = leftTable.getSelectedRows();
        for(int sel:sels)
        {
            rightData.addEntry(new CollectionEntry(leftData.get(sel)));
        }
    }

    public static void main(String[] args)
    {
        new CollectionManagerWindow();
    }
}