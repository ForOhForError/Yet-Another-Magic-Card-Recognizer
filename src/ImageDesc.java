import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.ddogleg.struct.FastQueue;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.feature.BrightFeature;


public class ImageDesc {
	static DetectDescribePoint<GrayF32, BrightFeature> detDesc = 
			FactoryDetectDescribe.surfStable(StaticConfigs.getHessianConf(), null,null, GrayF32.class);
	static ScoreAssociation<BrightFeature> scorer = FactoryAssociation.defaultScore(detDesc.getDescriptionType());
	static AssociateDescription<BrightFeature> associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);
	
	private AverageHash hash;
	private FastQueue<BrightFeature> desc = UtilFeature.createQueue(detDesc,0);
	

	public ImageDesc(BufferedImage in)
	{
		if(!AverageHash.isInitiated())
		{
			AverageHash.init(2, 2);
		}
		hash = AverageHash.avgHash(in,2,2);
		GrayF32 img = ConvertBufferedImage.convertFromSingle(in, null, GrayF32.class);
		desc.reset();
		describeImage(img,desc);
	}
	
	public ImageDesc(FastQueue<BrightFeature> d, AverageHash h)
	{
		desc = d;
		hash = h;
	}
	
	public void writeOut(DataOutputStream out) throws IOException
	{
		out.writeInt(desc.data.length);
		for(BrightFeature ft:desc.data)
		{
			out.writeInt(ft.value.length);
			for(double val:ft.value)
			{
				out.writeDouble(val);
			}
		}
		hash.writeOut(out);
	}
	
	public static ImageDesc readIn(ByteBuffer buf)
	{
		FastQueue<BrightFeature> des = UtilFeature.createQueue(detDesc,0);
		int dts = buf.getInt();
		for(int i=0;i<dts;i++)
		{
			int vs = buf.getInt();
			BrightFeature f = new BrightFeature(vs);
			double[] vls = new double[vs];
			for(int j=0;j<vs;j++)
			{
				vls[j]=buf.getDouble();
			}
			f.set(vls);
			des.add(f);
		}
		AverageHash h = AverageHash.readIn(buf);
		return new ImageDesc(des,h);
	}
	
	public static void describeImage(GrayF32 input, FastQueue<BrightFeature> descs) {
		detDesc.detect(input);
		for (int i = 0; i < detDesc.getNumberOfFeatures(); i++) {
			descs.grow().setTo(detDesc.getDescription(i));
		}
	}

	public double compareSURF(ImageDesc i2) {
		associate.setSource(desc);
		associate.setDestination(i2.desc);
		associate.associate();
		
		double max = Math.max(desc.size(), i2.desc.size());

		double score = 0;
		for (int i=0;i<associate.getMatches().size();i++)
		{
			score += 1 - associate.getMatches().data[i].fitScore;
		}
		
		return score/max;
	}
	
	public double compareHash(ImageDesc i2)
	{
		return hash.match(i2.hash);
	}
}
