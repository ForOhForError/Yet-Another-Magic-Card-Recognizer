import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.CardFace;
import forohfor.scryfall.api.JSONUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class RecognitionStrategy
{

    private static JSONParser PARSER = new JSONParser();

    protected String name;
    private int setSize;

    public void addFromFile(File handle) throws IOException
    {
        ZipFile zip = new ZipFile(handle);
        ZipEntry meta = zip.getEntry("meta.json");
        ZipEntry feature = zip.getEntry("feature.bin");
        JSONObject jo = null;
        JSONObject data = null;

        BufferedReader read = new BufferedReader(new InputStreamReader(zip.getInputStream(meta)));
        try
        {
            jo = (JSONObject) PARSER.parse(read);
            data = (JSONObject) jo.get("data");
        } catch (ParseException e)
        {
            System.err.println("meta.json is invalid");
        }
        read.close();

        DataInputStream is = new DataInputStream(zip.getInputStream(feature));
        byte[] byteArr = new byte[(int) feature.getSize()];
        is.readFully(byteArr);
        is.close();
        ByteBuffer buf = ByteBuffer.wrap(byteArr);
        int rec = JSONUtil.getIntData(jo, "size");
        for (int i = 0; i < rec; i++)
        {
            String id = BufferUtils.readUTF8(buf);
            ImageDesc desc = ImageDesc.readIn(buf);
            JSONObject jobj = (JSONObject) data.get(id);
            ZipEntry imageEntry = zip.getEntry("img/" + id + ".png");
            BufferedImage image = null;
            if (imageEntry != null)
            {
                InputStream imgs = zip.getInputStream(imageEntry);
                image = ImageIO.read(imgs);
                imgs.close();
            }
            DescContainer dc = new DescContainer(desc, id, jobj, image);
            this.add(dc);
        }
        zip.close();
    }

    @SuppressWarnings("unchecked")
    public synchronized void writeOut(File f) throws IOException
    {
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(f));
        DataOutputStream out = new DataOutputStream(zip);
        ImageOutputStream imgout = ImageIO.createImageOutputStream(zip);

        zip.putNextEntry(new ZipEntry("meta.json"));
        JSONObject top = new JSONObject();
        top.put("name", this.name);
        top.put("size", size());
        // set_size is used purely for checking if a set is outdated during
        // bulk generation -- the actual size contains multiple entries for
        // the sides of a multifaced card, for example
        top.put("set_size", this.setSize);
        JSONObject dat = new JSONObject();
        ArrayList<DescContainer> descs = getContainers();
        for (DescContainer dc : descs)
        {
            dat.put(dc.getID(), dc.getJSON());
        }
        top.put("data", dat);
        zip.write(top.toJSONString().getBytes());
        zip.closeEntry();

        zip.putNextEntry(new ZipEntry("feature.bin"));
        for (DescContainer dc : descs)
        {
            out.writeUTF(dc.getID());
            dc.getDescData().writeOut(out);
        }
        out.flush();
        zip.closeEntry();

        for (DescContainer dc : descs)
        {
            if (dc.getImage() != null)
            {
                zip.putNextEntry(new ZipEntry("img/" + dc.getID() + ".png"));
                ImageIO.write(dc.getImage(), "png", imgout);
                zip.closeEntry();
            }
        }

        zip.close();
    }

    public void addFromCard(Card card)
    {
        BufferedImage top_img = card.getImage(StaticConfigs.DEFAULT_ART_FORMAT);
        if (top_img == null && card.isMultifaced())
        {
            for (CardFace face : card.getCardFaces())
            {
                BufferedImage i = face.getImage(StaticConfigs.DEFAULT_ART_FORMAT);
                if (i != null)
                {
                    try
                    {
                        add(new DescContainer(
                                new ImageDesc(
                                        ImageUtil.getScaledImage(i)),
                                UUID.randomUUID().toString(),
                                extractFaceMetadata(card, face),
                                null
                        ));
                    } catch (Exception e)
                    {
                        System.err.println("Couldn't process card face: " + face.toString() + "; " + e.getLocalizedMessage() + "\n");
                    }
                } else
                {
                    System.err.println("Couldn't find card art for card face: " + face.getName() +
                            " from " + card.toString() + "\n");
                }
            }
        } else
        {
            if (top_img != null)
            {
                try
                {
                    add(new DescContainer(
                            new ImageDesc(ImageUtil.getScaledImage(top_img)),
                            UUID.randomUUID().toString(),
                            extractCardMetadata(card),
                            null
                    ));
                } catch (Exception e)
                {
                    System.err.println("Couldn't process card: " + card.toString() + "; " + e.getLocalizedMessage() + "\n");
                }
            } else
            {
                System.err.println("Couldn't find card art for card: " + card.toString() + "\n");
            }
        }
    }

    public BufferedImage resolveArt(String id)
    {
        for (DescContainer dc : getContainers())
        {
            if (dc.getID().equals(id))
            {
                return ImageUtil.getDescContainerImage(dc);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject extractCardMetadata(Card card)
    {
        JSONObject jo = new JSONObject();
        jo.put("name", card.getName());
        jo.put("set", card.getSetCode());
        jo.put("scryfall-id", card.getScryfallUUID().toString());
        jo.put("price", card.getPrices());
        return jo;
    }

    private static JSONObject extractFaceMetadata(Card card, CardFace face)
    {
        JSONObject jo = new JSONObject();
        jo.put("name", face.getName());
        jo.put("set", card.getSetCode());
        jo.put("scryfall-id", card.getScryfallUUID().toString());
        return jo;
    }

    public int getSetSize()
    {
        return setSize;
    }

    public void setSetSize(int size)
    {
        setSize = size;
    }

    public abstract ArrayList<DescContainer> getContainers();

    public abstract void finalizeLoad();

    public abstract void clear();

    public abstract void add(DescContainer dc);

    public abstract MatchResult getMatch(ImageDesc id, double thresh);

    public abstract int size();

    public abstract String getStratName();

    public abstract String getStratDisplayName();

    public String toString()
    {
        return getStratDisplayName();
    }

    private static JSONObject getFileMetadata(File f) throws IOException
    {
        ZipFile zip = new ZipFile(f);
        ZipEntry meta = zip.getEntry("meta.json");
        JSONObject jo = null;
        if (meta != null)
        {
            BufferedReader read = new BufferedReader(new InputStreamReader(zip.getInputStream(meta)));
            try
            {
                jo = (JSONObject) PARSER.parse(read);
            } catch (ParseException e)
            {
                System.err.println("meta.json is invalid");
            }
            read.close();
        }
        zip.close();
        return jo;
    }

    public static String getNameFromFile(File f)
    {
        try
        {
            JSONObject jo = getFileMetadata(f);
            return (String) jo.get("name");
        } catch (IOException e)
        {
            return null;
        }
    }

    public static int getSizeFromFile(File f)
    {
        try
        {
            JSONObject jo = getFileMetadata(f);
            return intValue(jo.get("set_size"));
        } catch (IOException e)
        {
            return -1;
        }
    }

    private static int intValue(Object o)
    {
        if (o instanceof Long)
        {
            return ((Long) o).intValue();
        }
        if (o instanceof Integer)
        {
            return (Integer) o;
        }
        return -1;
    }
}
