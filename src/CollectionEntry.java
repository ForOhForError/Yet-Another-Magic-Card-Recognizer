import forohfor.scryfall.api.Card;

import java.util.UUID;

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

    public CollectionEntry(MatchResult res)
    {
        backingCard = null;
        String scryfallId = res.getData().getScryfallId();
        if (scryfallId != null)
        {
            this.scry_id = UUID.fromString(res.getData().getScryfallId());
        }
        this.name = res.getName();
        this.setCode = res.set();
        this.isFoil = false;
        this.count = 1;
    }

    public CollectionEntry(String scryID, String name, String setCode, boolean isFoil, int count)
    {
        backingCard = null;
        try
        {
            this.scry_id = UUID.fromString(scryID);
        } catch (IllegalArgumentException e)
        {
            this.scry_id = null;
        }
        this.name = name;
        this.setCode = setCode;
        this.isFoil = isFoil;
        this.count = count;
    }

    public CollectionEntry(CollectionEntry e)
    {
        this.backingCard = e.backingCard;
        this.scry_id = e.scry_id;
        this.name = e.name;
        this.setCode = e.setCode;
        this.isFoil = e.isFoil;
        this.count = 1;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof CollectionEntry)
        {
            CollectionEntry e = (CollectionEntry) obj;
            if (this.scry_id != null && e.getId() != null)
            {
                return this.scry_id.equals(e.getId()) && this.isFoil() == e.isFoil();
            }
            return this.name.equals(e.getName()) && this.isFoil() == e.isFoil();
        }
        return false;
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

    public void setFoil(boolean foil)
    {
        isFoil = foil;
    }

    public void setCount(int i)
    {
        count = i;
    }

    public String getIdString()
    {
        if (scry_id != null)
        {
            return scry_id.toString();
        }
        return "";
    }

    public String toTSV()
    {
        return String.format(
                "%s\t%s\t%s\t%s\t%s",
                getIdString(),
                name,
                setCode,
                "" + isFoil,
                "" + count
        );
    }

    public static CollectionEntry fromTSV(String s)
    {
        String[] cells = s.replaceAll("[\n\r]$", "").split("\t");
        return new CollectionEntry(
                cells[0],
                cells[1],
                cells[2],
                Boolean.parseBoolean(cells[3]),
                Integer.parseInt(cells[4])
        );
    }
}