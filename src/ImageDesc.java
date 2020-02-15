import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F64;

import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.struct.FastQueue;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.enhance.EnhanceImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.ConvertImage;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.geo.ConfigRansac;
import boofcv.factory.geo.FactoryMultiViewRobust;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.geo.AssociatedPair;


public class ImageDesc {
	private static DetectDescribePoint<GrayF32, BrightFeature> detDesc = 
			FactoryDetectDescribe.surfStable(
				StaticConfigs.getHessianConf(), 
				null,
				null, 
				GrayF32.class
			);
	
	private static ScoreAssociation<BrightFeature> scorer = 
		FactoryAssociation.defaultScore(detDesc.getDescriptionType());
	private static AssociateDescription<BrightFeature> associate = 
		FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);
	private static ModelMatcher<Homography2D_F64,AssociatedPair> modelMatcher = 
		FactoryMultiViewRobust.homographyRansac(null,new ConfigRansac(60,30));
	
	private static Point2D_F64[] referencePoints = 
	{
		new Point2D_F64(0,0),
		new Point2D_F64(ImageUtil.SQUARE_SIZE, 0),
		new Point2D_F64(ImageUtil.SQUARE_SIZE, ImageUtil.SQUARE_SIZE),
		new Point2D_F64(0, ImageUtil.SQUARE_SIZE)
	};

	private AverageHash hash;
	private AverageHash flipped;
	private FastQueue<BrightFeature> desc = UtilFeature.createQueue(detDesc,0);
	private ArrayList<Point2D_F64> points = new ArrayList<Point2D_F64>(0);
	private int size;

	public ImageDesc(BufferedImage in, BufferedImage flip_in)
	{
		if(!AverageHash.isInitiated())
		{
			AverageHash.init(2, 2);
		}
		hash = AverageHash.avgHash(in,2,2);
		if(flip_in != null)
		{
			flipped = AverageHash.avgHash(flip_in,2,2);
		}
		int histogram[] = new int[256];
		int transform[] = new int[256];
		GrayU8 img = ConvertBufferedImage.convertFromSingle(in, null, GrayU8.class);
		GrayU8 norm = img.createSameShape();
		ImageStatistics.histogram(img,0,histogram);
		EnhanceImageOps.equalize(histogram, transform);
		EnhanceImageOps.applyTransform(img, transform, norm);
		GrayF32 normf = new GrayF32(img.width,img.height);
		ConvertImage.convert(norm, normf);
		desc.reset();
		size = describeImage(normf, desc, points);
	}

	public ImageDesc(BufferedImage in)
	{
		this(in,null);
	}
	
	public ImageDesc(FastQueue<BrightFeature> d, ArrayList<Point2D_F64> p, AverageHash h)
	{
		desc = d;
		hash = h;
		points = p;
		size = p.size();
	}
	
	public void writeOut(DataOutputStream out) throws IOException
	{
		out.writeInt(size);
		for(int i=0; i<size; i++)
		{
			BrightFeature f = desc.get(i);
			for(double val:f.value)
			{
				out.writeDouble(val);
			}
			Point2D_F64 pt = points.get(i);
			out.writeDouble(pt.x);
			out.writeDouble(pt.y);
		}
		hash.writeOut(out);
	}
	
	public static ImageDesc readIn(ByteBuffer buf)
	{
		int size = buf.getInt();
		ArrayList<Point2D_F64> points = new ArrayList<Point2D_F64>(size);
		FastQueue<BrightFeature> descs = UtilFeature.createQueue(detDesc,size);
		for(int i=0;i<size;i++)
		{
			BrightFeature f = detDesc.createDescription();
			for(int j=0;j<f.size();j++)
			{
				f.value[j]=buf.getDouble();
			}
			descs.add(f);
			points.add(new Point2D_F64(
				buf.getDouble(),buf.getDouble()
			));
		}
		AverageHash hash = AverageHash.readIn(buf);
		return new ImageDesc(descs, points, hash);
	}
	
	public static int describeImage(GrayF32 input, FastQueue<BrightFeature> descs, List<Point2D_F64> points) {
		detDesc.detect(input);
		int size = detDesc.getNumberOfFeatures();
		for (int i = 0; i < size; i++) {
			descs.grow().setTo(detDesc.getDescription(i));
			points.add(detDesc.getLocation(i));
		}
		return size;
	}

	public double compareSURF(ImageDesc i2) {
		associate.setSource(desc);
		associate.setDestination(i2.desc);
		associate.associate();

		List<AssociatedPair> pairs = new ArrayList<>();
		FastQueue<AssociatedIndex> matches = associate.getMatches();
		double score = 0;
		for (int i=0;i<matches.size();i++)
		{
			AssociatedIndex match = matches.get(i);
			score += 1 - match.fitScore;
			pairs.add(new AssociatedPair(
				points.get(match.src), 
				i2.points.get(match.dst),false
			));
		}

		if( !modelMatcher.process(pairs) )
		{
			return 0;
		}
		Homography2D_F64 homography = modelMatcher.getModelParameters();
		
		return score*scoreHomography(homography)*1000;
	}

	private double scoreHomography(Homography2D_F64 homography)
	{
		return HomographyCoverage.calculateCoverage(referencePoints, homography);
	}
	
	public double compareHash(ImageDesc i2)
	{
		return hash.match(i2.hash);
	}

	public double compareHashWithFlip(ImageDesc i2)
	{
		return Math.max(hash.match(i2.hash), flipped.match(i2.hash));
	}
}
