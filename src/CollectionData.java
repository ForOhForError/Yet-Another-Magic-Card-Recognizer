import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import forohfor.scryfall.api.Card;

class CollectionData extends DefaultTableModel {
    private static final long serialVersionUID = 1L;
    private ArrayList<CollectionEntry> data;
    private static final String[] NAMES = {"Name", "Set Code", "Foil", "Count"};

    private boolean showCount = false;

    public CollectionData(boolean showCount)
    {
        data = new ArrayList<CollectionEntry>();
        this.showCount = showCount;
    }

    public CollectionEntry get(int ix)
    {
        return data.get(ix);
    }

    public void addEntry(CollectionEntry ent)
    {
        for(CollectionEntry e : data)
        {
            if(e.getId().equals(ent.getId()) && e.isFoil() == ent.isFoil())
            {
                e.setCount(e.getCount()+ent.getCount());
                fireTableDataChanged();
                return;
            }
        }
        data.add(ent);
        fireTableDataChanged();
    }

    public void addCard(Card c)
    {
        addEntry(new CollectionEntry(c));
    }

    public void addCards(Collection<Card> cards)
    {
        for(Card c: cards)
        {
            addCard(c);
        }
    }

    public void offsetCount(int row, int offset)
    {
        CollectionEntry e = data.get(row);
        e.setCount(e.getCount()+offset);
        this.fireTableRowsUpdated(row, row);
    }

    public void toggleFoil(int row)
    {
        CollectionEntry e = data.get(row);
        e.setFoil(!e.isFoil());
        this.fireTableRowsUpdated(row, row);
    }

    public void removeEmptyRows()
    {
        int i=0;
        while(i < data.size())
        {
            if(data.get(i).getCount() <= 0)
            {
                data.remove(i);
                fireTableRowsDeleted(i, i);
            }
            else
            {
                i++;
            }
        }
    }

    @Override
    public int getRowCount() {
        if(data != null)
        {
            return data.size();
        }
        return 0;
    }

    @Override
    public int getColumnCount() {
        return (showCount ? NAMES.length:NAMES.length-1);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return NAMES[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CollectionEntry coll = data.get(rowIndex);
        switch(columnIndex)
        {
            case 0:
                return coll.getName();
            case 1:
                return coll.getSetCode();
            case 2:
                return coll.isFoil() ? "✓":"";
            case 3:
                return ""+coll.getCount();
        }
        return "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        super.addTableModelListener(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        super.removeTableModelListener(l);
    }
}