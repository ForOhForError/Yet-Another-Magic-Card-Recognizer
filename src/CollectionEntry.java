import java.util.UUID;

import forohfor.scryfall.api.Card;

class CollectionEntry
{
    private Card backingCard;
    private UUID scry_id;
    private String name;
    private String setCode;
    private int count;
    private boolean isFoil;

    public CollectionEntry(Card c)
    {
        backingCard = c;
        scry_id = c.getScryfallUUID();
        name = c.getName();
        setCode = c.getSetCode();
        isFoil = false;
        count = 1;
    }

    public CollectionEntry(String scryID, String name, String setCode, boolean isFoil, int count)
    {
        backingCard = null;
        this.scry_id = UUID.fromString(scryID);
        this.name = name;
        this.setCode = setCode;
        this.isFoil = isFoil;
        this.count = count;
    }

    public UUID getId()
    {
        return scry_id;
    }

    public String getName()
    {
        return name;
    }

    public String getSetCode()
    {
        return setCode;
    }

    public int getCount()
    {
        return count;
    }

    public boolean isFoil()
    {
        return isFoil;
    }

    public String toTSV()
    {
        return String.format(
            "%s\t%s\t%s\t%s\t%s", 
            scry_id.toString(), 
            name,
            setCode,
            ""+isFoil,
            ""+count
        );
    }

    public static CollectionEntry fromTSV(String s)
    {
        String[] cells = s.trim().split("\t");
        return new CollectionEntry(
            cells[0],
            cells[1],
            cells[2],
            Boolean.parseBoolean(cells[3]),
            Integer.parseInt(cells[4])
        );
    }
}