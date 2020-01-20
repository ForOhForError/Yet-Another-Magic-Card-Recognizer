import java.util.ArrayList;

import java.awt.image.BufferedImage;

abstract class AreaRecognitionStrategy
{
    public abstract ArrayList<MatchResult> recognize(BufferedImage in, RecognitionStrategy strat);

    public abstract String getStratName();

    public abstract String getStratDisplayName();

    public String toString()
	{
		return getStratDisplayName();
	}
}