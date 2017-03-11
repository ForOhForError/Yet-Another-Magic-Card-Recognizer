import java.awt.image.BufferedImage;
import java.util.List;

import org.ddogleg.struct.FastQueue;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.GrayF32;
import georegression.struct.point.Point2D_F64;
import boofcv.struct.feature.BrightFeature;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ScoreMatch {
	
	static DetectDescribePoint<GrayF32, BrightFeature> detDesc = createFromPremade(GrayF32.class);

	static <T extends GrayF32, TD extends TupleDesc> DetectDescribePoint<T, TD> createFromPremade(Class<T> imageType) {
		return (DetectDescribePoint) FactoryDetectDescribe.surfStable(new ConfigFastHessian(10, 2, 100, 2, 9, 3, 4),
				null, null, imageType);
	}

	static ScoreAssociation scorer = FactoryAssociation.defaultScore(detDesc.getDescriptionType());
	static AssociateDescription associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);

	public static void describeImage(GrayF32 input, List<Point2D_F64> points, FastQueue<BrightFeature> descs) {
		detDesc.detect(input);
		for (int i = 0; i < detDesc.getNumberOfFeatures(); i++) {
			points.add(detDesc.getLocation(i).copy());
			descs.grow().setTo(detDesc.getDescription(i));
		}
		detDesc = createFromPremade(GrayF32.class);
	}

	public static double matchScore(ImageDesc i1, ImageDesc i2) {
		associate.setSource(i1.desc);
		associate.setDestination(i2.desc);
		associate.associate();
		
		double min = Math.max(i1.desc.size(), i2.desc.size());

		double score = 0;
		for (int i=0;i<associate.getMatches().size();i++)
		{
			score += 1;
			score -= associate.getMatches().data[i].fitScore;
		}
		
		return score*100/min;
	}

	public static double matchScore(BufferedImage imageA, BufferedImage imageB) {
		return matchScore(new ImageDesc(imageA), new ImageDesc(imageB));
	}
}
