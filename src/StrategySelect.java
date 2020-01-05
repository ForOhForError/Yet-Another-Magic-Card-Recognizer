import java.util.ArrayList;

class StrategySelect
{
    private static ArrayList<RecognitionStrategy> strats;

    static
    {
        strats = new ArrayList<RecognitionStrategy>(2);
        registerStrategy(new ListRecogStrat());
        registerStrategy(new TreeRecogStrat());
    }

    public static void registerStrategy(RecognitionStrategy strat) 
    {
        strats.add(strat);
    }

    public static RecognitionStrategy getStrat(String id) 
    {
        for(RecognitionStrategy strat : strats)
        {
            if(strat.getStratName().equals(id))
            {
                return strat;
            }
        }
        return null;
    }
}