import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;

public class CardCandidate {
	private Point2D_I32[] points;

	public static final double SIDE_VAR = 0.09;
	public static final double RATIO_VAR = 0.09;
	private static final double targetRatio = 63.0/88.0;

	boolean cardShaped;

	private double avgMajor;
	private double avgMinor;

	private double centerX;
	private double centerY;

	private double area;

	public CardCandidate(Point2D_I32[] pts)
	{
		points = pts;
		init();
	}

	private void init()
	{
		centerX = points[0].x+points[1].x+points[2].x+points[3].x;
		centerX /= 4.0;
		centerY = points[0].y+points[1].y+points[2].y+points[3].y;
		centerY /= 4.0;

		int[] dists = new int[4];
		dists[0] = (int)points[0].distance(points[1]);
		dists[1] = (int)points[1].distance(points[2]);
		dists[2] = (int)points[2].distance(points[3]);
		dists[3] = (int)points[3].distance(points[0]);

		Arrays.sort(dists);

		avgMajor = (((double)dists[2]+(double)dists[3])/2.0);
		avgMinor = (((double)dists[1]+(double)dists[0])/2.0);

		double actRatio = avgMinor/avgMajor;

		area = avgMinor*avgMajor;

		if(Math.abs(actRatio-targetRatio)<RATIO_VAR)
		{
			if(Math.abs(((double)dists[0]-(double)dists[1])/avgMinor)<SIDE_VAR)
			{
				if(Math.abs(((double)dists[2]-(double)dists[3])/avgMajor)<SIDE_VAR)
				{
					if(area>200)
					{
						cardShaped = true;
						return;
					}
				}
			}
		}
		cardShaped = false;
	}

	public boolean isValid()
	{
		return cardShaped;
	}

	public Polygon getPolygon()
	{
		int[] xpts = {points[0].x,points[1].x,points[2].x,points[3].x};
		int[] ypts = {points[0].y,points[1].y,points[2].y,points[3].y};
		return new Polygon(xpts,ypts,4);
	}

	public Point getCenter()
	{
		return new Point((int)centerX,(int)centerY);
	}

	public void draw(Graphics g)
	{
		draw(g,0,0);
	}
	
	public void draw(Graphics g,int offx, int offy)
	{
		g.drawLine(points[0].x+offx,points[0].y+offy,points[1].x+offx,points[1].y+offy);
		g.drawLine(points[1].x+offx,points[1].y+offy,points[2].x+offx,points[2].y+offy);
		g.drawLine(points[2].x+offx,points[2].y+offy,points[3].x+offx,points[3].y+offy);
		g.drawLine(points[3].x+offx,points[3].y+offy,points[0].x+offx,points[0].y+offy);
	}
	
	public BufferedImage getResolvedImage(BufferedImage src)
	{
		Planar<GrayF32> input = ConvertBufferedImage.convertFromMulti(src, null, true, GrayF32.class);

		RemovePerspectiveDistortion<Planar<GrayF32>> removePerspective =
				new RemovePerspectiveDistortion<>(300, 300, ImageType.pl(3, GrayF32.class));

		if( !removePerspective.apply(input,
				new Point2D_F64(points[0].x,points[0].y),
				new Point2D_F64(points[1].x,points[1].y),
				new Point2D_F64(points[2].x,points[2].y),
				new Point2D_F64(points[3].x,points[3].y)
								) ){
			return null;
		}

		Planar<GrayF32> output = removePerspective.getOutput();
		return ConvertBufferedImage.convertTo_F32(output,null,true);
	}
}
