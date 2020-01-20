import java.awt.image.BufferedImage;
import java.util.ArrayList;

class AutoDetectAreaStrat extends AreaRecognitionStrategy {

    private ArrayList<MatchResult> results = new ArrayList<MatchResult>();

    @Override
    public ArrayList<MatchResult> recognize(BufferedImage in, RecognitionStrategy strat) {
        results.clear();
        ArrayList<ContourBoundingBox> bounds = CardBoundingBoxFinder.process(in);
        for(ContourBoundingBox bound:bounds)
        {
            ImageDesc i = new ImageDesc(bound.getTransformedImage(in));
            MatchResult mr = strat.getMatch(i, SettingsPanel.RECOG_THRESH);
            if(mr != null)
            {
                results.add(mr);
            }
        }
        
        return results;
    }

    @Override
    public String getStratName() {
        return "auto-detect";
    }

    @Override
    public String getStratDisplayName() {
        return "Auto-Detect Card Bounds Within Area";
    }

}