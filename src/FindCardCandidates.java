import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_I32;

public class FindCardCandidates {

	private static CannyEdge<GrayU8, GrayS16> canny = 
			FactoryEdgeDetectors.canny(2, true, true, GrayU8.class, GrayS16.class);
	
	public static List<Contour> getCannyContours(BufferedImage image) {
		GrayU8 gray = ConvertBufferedImage.convertFrom(image, (GrayU8) null);
		GrayU8 edgeImage = gray.createSameShape();
		canny.process(gray, 0.1f, 0.3f, edgeImage);
		List<Contour> contours = BinaryImageOps.contour(edgeImage, ConnectRule.EIGHT, null);

		return contours;
	}

	public static ArrayList<CardCandidate> validCandidates(BufferedImage img)
	{
		List<Contour> contours = getCannyContours(img);
		ArrayList<CardCandidate> ccs = new ArrayList<>();
		for(Contour con:contours)
		{
			CardCandidate cc = contourToCandidate(con,img);
			if(cc.isValid()){
				ccs.add(cc);
			}
		}
		return ccs;
	}

	public static CardCandidate contourToCandidate(Contour con, BufferedImage src)
	{
		Point2D_I32 tl = new Point2D_I32(0,0);
		Point2D_I32 tr = new Point2D_I32(src.getWidth(),0);
		Point2D_I32 br = new Point2D_I32(src.getWidth(),src.getHeight());
		Point2D_I32 bl = new Point2D_I32(0,src.getHeight());

		int[][] mins = {{9999,0},{9999,0},{9999,0},{9999,0}};
		Point2D_I32[] pts = {tl,tr,br,bl};
		int ix = 0;
		for(Point2D_I32 pt:con.external)
		{
			for(int i=0;i<4;i++)
			{
				int d = (int)pt.distance(pts[i]);
				if(d<mins[i][0])
				{
					mins[i][0]=d;
					mins[i][1]=ix;
				}
			}
			ix++;
		}

		Point2D_I32[] corners = {
				con.external.get(mins[0][1]),
				con.external.get(mins[1][1]),
				con.external.get(mins[2][1]),
				con.external.get(mins[3][1]),
		};


		CardCandidate cc = new CardCandidate(corners);
		return cc;
	}
}
