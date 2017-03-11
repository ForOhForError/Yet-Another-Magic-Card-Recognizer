import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ddogleg.struct.FastQueue;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import georegression.struct.point.Point2D_F64;
import boofcv.struct.feature.BrightFeature;

@SuppressWarnings({"unchecked","rawtypes"})
public class ImageDesc {
	static DetectDescribePoint<GrayF32, BrightFeature> detDesc = createFromPremade(GrayF32.class);
	static <T extends GrayF32, TD extends BrightFeature>
	DetectDescribePoint<T, TD> createFromPremade( Class<T> imageType ) {
		return (DetectDescribePoint)FactoryDetectDescribe.surfStable(
				new ConfigFastHessian(10, 2, 100, 2, 9, 3, 4), null,null, imageType);
	}
	
	
	
	private List<Point2D_F64> points = new ArrayList<>();
	public FastQueue<BrightFeature> desc = UtilFeature.createQueue(detDesc,0);
	
	public ImageDesc(BufferedImage in)
	{
		GrayF32 img = ConvertBufferedImage.convertFromSingle(in, null, GrayF32.class);
		points.clear();
		desc.reset();
		ScoreMatch.describeImage(img,points,desc);
	}
	
	public ImageDesc(FastQueue<BrightFeature> d)
	{
		desc = d;
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
	}
	
	public static ImageDesc readIn(DataInputStream in) throws IOException
	{
		FastQueue<BrightFeature> des = UtilFeature.createQueue(detDesc,0);
		int dts = in.readInt();
		for(int i=0;i<dts;i++)
		{
			int vs = in.readInt();
			BrightFeature f = new BrightFeature(vs);
			double[] vls = new double[vs];
			for(int j=0;j<vs;j++)
			{
				vls[j]=in.readDouble();
			}
			f.set(vls);
			des.add(f);
		}
		return new ImageDesc(des);
	}
}
