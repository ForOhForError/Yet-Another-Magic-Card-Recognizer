import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import com.github.sarxos.webcam.Webcam;

import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.alg.filter.binary.Contour;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;

public class WebcamCanvas extends JPanel implements MouseInputListener{
	private static final long serialVersionUID = 1L;

	BufferedImage baseline;

	public WebcamCanvas(Webcam w) {
		super();
		cam = w;
		canvas = new Canvas();
		setSize(w.getViewSize());
		canvas.setSize(w.getViewSize());
		add(canvas);
		initBox();
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
	}
	private Webcam cam;
	private Canvas canvas;
	private BufferedImage lastDrawn;
	private BufferedImage buf;
	private Point2D_I32[] points = new Point2D_I32[4];
	private boolean pointsValid = false;
	private MatchResult lastResult;

	private int draggingPoint = -1;

	public void setWebcam(Webcam w)
	{
		cam.close();
		cam = w;
		setSize(w.getViewSize());
		canvas.setSize(w.getViewSize());
		add(canvas);
		initBox();
	}

	private ArrayList<CardCandidate> ccs = new ArrayList<>();

	public Webcam getWebcam() {
		return cam;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public BufferedImage lastDrawn()
	{
		return lastDrawn;
	}

	public void addCandidate(CardCandidate cc)
	{
		ccs.add(cc);
	}

	public BufferedImage getBoundedZone()
	{
		if( lastDrawn!=null && pointsValid )
		{
			try
			{
				Planar<GrayF32> input = ConvertBufferedImage.convertFromMulti(lastDrawn, null, true, GrayF32.class);

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

	public void draw()
	{
		if(!cam.isOpen())
		{
			cam.open();
		}
		lastDrawn = cam.getImage();
		if(baseline==null)
		{
			baseline=lastDrawn;
		}

		if(buf == null || buf.getHeight() != lastDrawn.getHeight() || buf.getWidth() != lastDrawn.getWidth())
		{
			buf = new BufferedImage(lastDrawn.getWidth(),lastDrawn.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
		}
		Graphics gi = canvas.getGraphics();
		Graphics g = buf.getGraphics();
		g.drawImage(lastDrawn, 0, 0, null);
		drawBounds(g,0,0);
		g.setColor(Color.RED);
		if(lastResult!=null)
		{
			g.drawString(lastResult.toString(), 0, 10);
		}

		g.setColor(Color.RED);

		/**
		for(CardCandidate cc:ccs)
		{
			cc.draw(g,recogBounds.x,recogBounds.y);
		}
		ccs.clear();
		*/

		gi.drawImage(buf, 0, 0, null);

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

	public void drawContours(Graphics g)
	{
		List<Contour> contours = FindCardCandidates.getCannyContours(lastDrawn);
		for(Contour con:contours)
		{
			int[] xpoint = new int[con.external.size()];
			int[] ypoint = new int[con.external.size()];
			int i=0;
			for(Point2D_I32 pt:con.external)
			{
				xpoint[i]=pt.x;
				ypoint[i]=pt.y;
				i++;
			}
			g.drawPolygon(xpoint,ypoint,xpoint.length);
		}
	}

	public void close()
	{
		cam.close();
	}

	public void initBox()
	{
		int height = (int)(cam.getViewSize().getHeight()*8/10);
		int width = height*63/88;
		int x = (int)(cam.getViewSize().getWidth()/2-width/2);
		int y = (int)(cam.getViewSize().getHeight()/2-height/2);

		points[0] = new Point2D_I32(x,y);
		points[1] = new Point2D_I32(x+width,y);
		points[2] = new Point2D_I32(x+width,y+height);
		points[3] = new Point2D_I32(x,y+height);
		pointsValid = true;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		Point p = arg0.getPoint();
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
	public void mouseReleased(MouseEvent arg0) {
		draggingPoint = -1;
	}

	public void setLastResult(MatchResult lastResult) {
		this.lastResult = lastResult;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(draggingPoint != -1)
		{
			Point p = e.getPoint();
			if(p.x >= 0 && p.x <= cam.getViewSize().getWidth())
			{
				points[draggingPoint].x = p.x;
			}
			if(p.y >= 0 && p.y <= cam.getViewSize().getHeight())
			{
				points[draggingPoint].y = p.y;
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}

}
