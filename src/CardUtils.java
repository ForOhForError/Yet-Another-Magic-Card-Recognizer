import java.util.HashSet;

class CardUtils
{
    private static HashSet<String> basicNames;

    static
    {
        basicNames = new HashSet<>(5);
        basicNames.add("Plains");
        basicNames.add("Island");
        basicNames.add("Swamp");
        basicNames.add("Mountain");
        basicNames.add("Forest");
    }

    public static boolean isEssentialBasic(String name)
    {
        return basicNames.contains(name);
    }
}