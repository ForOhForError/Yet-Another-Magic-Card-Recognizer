import java.util.ArrayList;

class StrategySelect
{
    private static ArrayList<RecognitionStrategy> strats;
    private static ArrayList<AreaRecognitionStrategy> areaStrats;

    static
    {
        strats = new ArrayList<RecognitionStrategy>(2);
        registerStrategy(new ListRecogStrat());
        registerStrategy(new HashNarrowedRecogStrat());

        areaStrats = new ArrayList<AreaRecognitionStrategy>(2);
        registerAreaStrategy(new ManualAreaStrat());
        registerAreaStrategy(new AutoDetectAreaStrat());
        registerAreaStrategy(new RadiusAreaStrat());
    }

    public static void registerStrategy(RecognitionStrategy strat)
    {
        strats.add(strat);
    }

    public static void registerAreaStrategy(AreaRecognitionStrategy strat)
    {
        areaStrats.add(strat);
    }

    public static RecognitionStrategy getStrat(String id)
    {
        for (RecognitionStrategy strat : strats)
        {
            if (strat.getStratName().equals(id))
            {
                return strat;
            }
        }
        return null;
    }

    public static AreaRecognitionStrategy getAreaStrat(String id)
    {
        for (AreaRecognitionStrategy strat : areaStrats)
        {
            if (strat.getStratName().equals(id))
            {
                return strat;
            }
        }
        return null;
    }

    public static RecognitionStrategy[] getStrats()
    {
        return strats.toArray(new RecognitionStrategy[2]);
    }

    public static AreaRecognitionStrategy[] getAreaStrats()
    {
        return areaStrats.toArray(new AreaRecognitionStrategy[2]);
    }
}