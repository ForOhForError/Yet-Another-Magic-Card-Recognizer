import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;
import forohfor.scryfall.api.Set;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetGenerator
{

    private static final String[] setTypes =
            {
                    "all",
                    "expansion+core",
                    "core",
                    "expansion",
                    "masters",
                    "masterpiece",
                    "from_the_vault",
                    "premium_deck",
                    "duel_deck",
                    "commander",
                    "planechase",
                    "conspiracy",
                    "archenemy",
                    "vanguard",
                    "funny",
                    "starter",
                    "box",
                    "promo",
                    "token",
                    ""
            };

    public static void bulkGenSets()
    {
        String selectedType = (String) JOptionPane.showInputDialog(null,
                "Choose set types to pregen",
                "Bulk Generate Sets",
                JOptionPane.PLAIN_MESSAGE, null,
                setTypes, "all"
        );

        if (selectedType == null)
        {
            return;
        }

        new Thread()
        {
            public void run()
            {
                writeSets(selectedType);
            }
        }.start();
    }

    private static void writeSets(String selectedType)
    {
        ArrayList<Set> sets = MTGCardQuery.getSets();
        ArrayList<Set> toGenerate = new ArrayList<Set>();

        for (Set set : sets)
        {
            String setType = set.getSetType();
            if ("expansion+core".equals(selectedType))
            {
                if (!(setType.equals("core") || setType.equals("expansion")))
                {
                    continue;
                }
            } else if (!selectedType.equals("all"))
            {
                if (!setType.equals(selectedType))
                {
                    continue;
                }
            }

            String path = SavedConfig.getSetPath(set.getCode());
            File f = new File(path);

            if ((f.exists() && f.isFile()))
            {
                int size = ListRecogStrat.getSizeFromFile(f);
                if (size != set.getCardCount())
                {
                    toGenerate.add(set);
                }
            } else
            {
                toGenerate.add(set);
            }
        }
        generateSets(toGenerate);
    }

    public static void generateSets(List<Set> sets)
    {
        OperationBar bar = RecogApp.INSTANCE.getOpBar();
        if (bar.setTask("Generating Sets", sets.size()))
        {
            for (Set set : sets)
            {
                bar.setSubtaskName(set.getName());
                generateSet(set);
                bar.progressTask();
            }
        }
    }

    public static boolean generateSet(Set set)
    {
        String path = SavedConfig.getSetPath(set.getCode());
        ListRecogStrat r = new ListRecogStrat(set.getName());
        r.setSetSize(set.getCardCount());
        File f = new File(path);

        ArrayList<Card> cards = MTGCardQuery.getCardsFromURI(set.getSearchURI());

        for (Card card : cards)
        {
            if (CardUtils.isEssentialBasic(card.getName()))
            {
                if (!(SavedConfig.WRITE_BASICS_TO_SETS || card.isFullArt()))
                {
                    continue;
                }
            }
            r.addFromCard(card);
        }

        try
        {
            r.writeOut(f);
            return true;
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }


}
