import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.feature.BrightFeature;


public class ImageDesc {
	static DetectDescribePoint<GrayF32, BrightFeature> detDesc = 
			FactoryDetectDescribe.surfStable(StaticConfigs.getHessianConf(), null,null, GrayF32.class);
	
	static ScoreAssociation<BrightFeature> scorer = FactoryAssociation.defaultScore(detDesc.getDescriptionType());
	static AssociateDescription<BrightFeature> associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);
	
	private AverageHash hash;
	private AverageHash flipped;
	private FastQueue<BrightFeature> desc = UtilFeature.createQueue(detDesc,0);
	

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
		describeImage(normf,desc);
	}

	public ImageDesc(BufferedImage in)
	{
		this(in,null);
	}
	
	public ImageDesc(FastQueue<BrightFeature> d, AverageHash h)
	{
		desc = d;
		hash = h;
	}
	
	public void writeOut(DataOutputStream out) throws IOException
	{
		writeDescOut(out,desc);
		hash.writeOut(out);
	}
	
	public void writeDescOut(DataOutputStream out, FastQueue<BrightFeature> d) throws IOException
	{
		out.writeInt(d.data.length);
		for(BrightFeature ft:d.data)
		{
			out.writeInt(ft.value.length);
			for(double val:ft.value)
			{
				out.writeDouble(val);
			}
		}
	}
	
	public static ImageDesc readIn(ByteBuffer buf)
	{
		FastQueue<BrightFeature> d = readDescIn(buf,detDesc);
		AverageHash h = AverageHash.readIn(buf);
		return new ImageDesc(d,h);
	}
	
	public static FastQueue<BrightFeature> readDescIn(ByteBuffer buf,DetectDescribePoint<GrayF32,BrightFeature> ddp)
	{
		FastQueue<BrightFeature> d = UtilFeature.createQueue(ddp,0);
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
			d.add(f);
		}
		return d;
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

	public double compareHashWithFlip(ImageDesc i2)
	{
		return Math.max(hash.match(i2.hash), flipped.match(i2.hash));
	}
}
