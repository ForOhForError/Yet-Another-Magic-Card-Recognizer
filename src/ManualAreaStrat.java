import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.Color;

import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;

class ManualAreaStrat extends AreaRecognitionStrategy {

    private Point2D_I32[] points = new Point2D_I32[4];
    private boolean pointsValid = false;
    private int draggingPoint = -1;
    private int width;
    private int height;

    @Override
    public ArrayList<MatchResult> recognize(BufferedImage in, RecognitionStrategy strat) {
        ArrayList<MatchResult> res = new ArrayList<MatchResult>();
        BufferedImage img = ImageUtil.getScaledImage(getBoundedZone(in));
		ImageDesc id = new ImageDesc(img);
        MatchResult m = strat.getMatch(id, SettingsPanel.RECOG_THRESH/100f);
        if(m != null)
        {
            res.add(m);
        }
        return res;
    }

    public BufferedImage getBoundedZone(BufferedImage i)
	{
		if( i !=null && pointsValid )
		{
			try
			{
				Planar<GrayF32> input = ConvertBufferedImage.convertFromPlanar(i, null, true, GrayF32.class);

				RemovePerspectiveDistortion<Planar<GrayF32>> removePerspective =
						new RemovePerspectiveDistortion<>(672, 936, ImageType.pl(3, GrayF32.class));

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
			catch(Exception e)
			{
				return null;
			}
		}
		return null;
	}

    @Override
    public String getStratName() {
        return "manual";
    }

    @Override
    public String getStratDisplayName() {
        return "Manually Set Bounds";
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
		for(int i=0;i<4;i++){
			Point2D_I32 pt = points[i];
			if(Math.abs(p.x-pt.x)<=3 && Math.abs(p.y-pt.y)<=3)
			{
				draggingPoint = i;
				return;
			}
		}
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggingPoint = -1;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(draggingPoint != -1)
		{
			Point p = e.getPoint();
			if(p.x >= 0 && p.x <= this.width)
			{
				points[draggingPoint].x = p.x;
			}
			if(p.y >= 0 && p.y <= this.height)
			{
				points[draggingPoint].y = p.y;
			}
		}
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void draw(Graphics g) {
		drawBounds(g,0,0);
    }

    @Override
	public void init(int width, int height)
	{
        this.width = width;
        this.height = height;
		int h = (int)(height*8/10);
		int w = height*50/88;
		int x = (int)(width/2-w/2);
		int y = (int)(height/2-h/2);

		points[0] = new Point2D_I32(x,y);
		points[1] = new Point2D_I32(x+w,y);
		points[2] = new Point2D_I32(x+w,y+h);
		points[3] = new Point2D_I32(x,y+h);
		pointsValid = true;
	}

    public void drawBounds(Graphics g,int offx, int offy)
	{
		g.setColor(Color.GREEN);
		g.drawLine(points[0].x+offx,points[0].y+offy,points[1].x+offx,points[1].y+offy);
		g.setColor(Color.WHITE);
		g.drawLine(points[1].x+offx,points[1].y+offy,points[2].x+offx,points[2].y+offy);
		g.drawLine(points[2].x+offx,points[2].y+offy,points[3].x+offx,points[3].y+offy);
		g.drawLine(points[3].x+offx,points[3].y+offy,points[0].x+offx,points[0].y+offy);
		for(int i=0;i<4;i++){
			Point2D_I32 p = points[i];
			if(draggingPoint == i)
			{
				g.setColor(Color.RED);
			}
			else
			{
				g.setColor(Color.WHITE);
			}
			if(i==0)
			{
				g.fillOval(p.x-4, p.y-4, 9, 9);
			}
			else
			{
				g.fillOval(p.x-2, p.y-2, 5, 5);
			}
		}
	}

}