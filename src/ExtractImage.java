import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.List;

import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;

import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;

public class ExtractImage {

	public static ExtractResult extract(BufferedImage image) {

		GrayU8 gray = ConvertBufferedImage.convertFrom(image, (GrayU8) null);
		GrayU8 edgeImage = gray.createSameShape();
		CannyEdge<GrayU8, GrayS16> canny = FactoryEdgeDetectors.canny(2, true, true, GrayU8.class, GrayS16.class);
		canny.process(gray, 0.1f, 0.3f, edgeImage);
		List<Contour> contours = BinaryImageOps.contour(edgeImage, ConnectRule.EIGHT, null);

		double maxar = 0;
		Polygon maxp = new Polygon();

		for (Contour c : contours) {
			Polygon pol = new Polygon();
			for (Point2D_I32 pt : c.external) {
				pol.addPoint(pt.getX(), pt.getY());
			}
			double area = area(pol);
			if (area > maxar) {
				maxar = area;
				maxp = pol;
			}
		}

		Point2D_F64[] c = corners(maxp,image);

		Planar<GrayF32> input = ConvertBufferedImage.convertFromMulti(image, null, true, GrayF32.class);

		RemovePerspectiveDistortion<Planar<GrayF32>> removePerspective = new RemovePerspectiveDistortion<>(223, 310,
			ImageType.pl(3, GrayF32.class));
		
		boolean success = removePerspective.apply(input, c[0], c[1], c[2], c[3]);

		Planar<GrayF32> output = removePerspective.getOutput();

		BufferedImage flat = ConvertBufferedImage.convertTo_F32(output, null, true);

		return new ExtractResult(flat,success, maxar);
	}

	public static Point2D_F64 centroid(Polygon p)
	{
		float x = 0;
		float y = 0;
		for (int i = 0; i < p.npoints; i++) {
			x += p.xpoints[i];
			y += p.ypoints[i];
		}
		return new Point2D_F64(x/p.npoints,y/p.npoints);
	}

	public static Point2D_F64[] corners(Polygon p, BufferedImage src) {
		if (p.npoints == 0) {
			return null;
		}

		int h = src.getHeight();
		int w = src.getWidth();


		double[][] dist = new double[p.npoints][4];

		for (int i = 0; i < p.npoints; i++) {
			Point2D_F64 pt = new Point2D_F64(p.xpoints[i], p.ypoints[i]);

			dist[i][0] = pt.distance(0, 0);
			dist[i][1] = pt.distance(w, 0);
			dist[i][2] = pt.distance(w, h);
			dist[i][3] = pt.distance(0, h);
		}
		Point2D_F64[] points = new Point2D_F64[4];

		for(int j=0;j<4;j++){
			double mindist = dist[0][j];
			int mindex = 0;
			for (int i = 0; i < p.npoints; i++) {
				if(mindist>dist[i][j]){
					mindist=dist[i][j];
					mindex=i;
				}
			}
			points[j] = new Point2D_F64(p.xpoints[mindex],p.ypoints[mindex]);
		}

		return points;
	}

	public static double angle(Point2D_F64 centerPt, Point2D_F64 targetPt)
	{
		double theta = Math.atan2(targetPt.y - centerPt.y, targetPt.x - centerPt.x);
		theta += Math.PI/2.0;
		double angle = Math.toDegrees(theta);
		if (angle < 0) {
			angle += 360;
		}

		return angle;
	}

	public static double area(Polygon p) {
		double sum = 0;
		for (int i = 0; i < p.npoints; i++) {
			if (i == 0) {
				sum += p.xpoints[i] * (p.ypoints[i + 1] - p.ypoints[p.npoints - 1]);
			} else if (i == p.npoints - 1) {
				sum += p.xpoints[i] * (p.ypoints[0] - p.ypoints[i - 1]);
			} else {
				sum += p.xpoints[i] * (p.ypoints[i + 1] - p.ypoints[i - 1]);
			}
		}

		double area = 0.5 * Math.abs(sum);
		return area;

	}
}