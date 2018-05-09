import java.awt.image.BufferedImage;
import java.io.IOException;

import org.ddogleg.struct.FastQueue;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.image.GrayF32;
import forohfor.scryfall.api.MTGCardQuery;

public class ThresholdTester {
	
	static DetectDescribePoint<GrayF32, BrightFeature> dd = 
			FactoryDetectDescribe.surfStable(StaticConfigs.getHessianConf(), null,null, GrayF32.class);
	static ScoreAssociation<BrightFeature> scorer = FactoryAssociation.defaultScore(dd.getDescriptionType());
	static AssociateDescription<BrightFeature> associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);
	
	public static void main(String[] args) throws IOException
	{
		float thresh = 200;
		
		BufferedImage img = ImageUtil.getScaledImage(
				MTGCardQuery.getCardByScryfallId("51f724db-0427-44f9-963d-ae2356c928f4").getCannonicalImage());
		BufferedImage img2 = ImageUtil.getScaledImage(
				MTGCardQuery.getCardByScryfallId("82caa4d5-ef9f-4903-98a7-134c984da663").getCannonicalImage());
		
		
		doTest(thresh,img);
		
		doSpeedTest(thresh,img,img2);
	}

	public static void doSpeedTest(float thresh,BufferedImage in1,BufferedImage in2)
	{
		FastQueue<BrightFeature> desc1 = detdesc(in1,thresh);
		FastQueue<BrightFeature> desc2 = detdesc(in2,thresh);
		long time = System.currentTimeMillis();
		for(int i=0;i<20000;i++)
		{
			compareSURF(desc1,desc2);
		}
		time = System.currentTimeMillis()-time;
		System.out.println(time+" ms\n--");
	}
	
	public static FastQueue<BrightFeature> detdesc(BufferedImage in, float thresh)
	{
		ConfigFastHessian cfh = StaticConfigs.getHessianConf(thresh);
		DetectDescribePoint<GrayF32, BrightFeature> detDesc = 
				FactoryDetectDescribe.surfStable(cfh, null,null, GrayF32.class);
		GrayF32 img = ConvertBufferedImage.convertFromSingle(in, null, GrayF32.class);
		FastQueue<BrightFeature> desc = UtilFeature.createQueue(detDesc,0);
		detDesc.detect(img);
		for (int i = 0; i < detDesc.getNumberOfFeatures(); i++) {
			desc.grow().setTo(detDesc.getDescription(i));
		}
		return desc;
	}
	
	public static void doTest(float thresh,BufferedImage in)
	{
		FastQueue<BrightFeature> desc = detdesc(in,thresh);
		System.out.println(desc.getSize());
		System.out.println("---");
	}
	
	public static double compareSURF(FastQueue<BrightFeature> d1,FastQueue<BrightFeature> d2) {
		associate.setSource(d1);
		associate.setDestination(d2);
		associate.associate();
		
		double max = Math.max(d1.size(), d2.size());

		double score = 0;
		for (int i=0;i<associate.getMatches().size();i++)
		{
			score += 1 - associate.getMatches().data[i].fitScore;
		}
		
		return score/max;
	}
}
