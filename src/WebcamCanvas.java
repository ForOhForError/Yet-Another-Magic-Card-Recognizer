import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.github.sarxos.webcam.Webcam;

import boofcv.alg.filter.binary.Contour;
import georegression.struct.point.Point2D_I32;

public class WebcamCanvas extends JPanel implements MouseListener{
	private static final long serialVersionUID = 1L;

	public WebcamCanvas(Webcam w) {
		super();
		cam = w;
		canvas = new Canvas();
		setSize(w.getViewSize());
		canvas.setSize(w.getViewSize());
		add(canvas);
		initBox();
		canvas.addMouseListener(this);
	}
	private Webcam cam;
	private Canvas canvas;
	private BufferedImage lastDrawn;
	private BufferedImage buf;
	private Rectangle recogBounds;
	private MatchResult lastResult;
	
	
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
		if(lastDrawn!=null && recogBounds!=null)
		{
			return lastDrawn.getSubimage(recogBounds.x, recogBounds.y,
					recogBounds.width, recogBounds.height);
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
		
		if(buf == null || buf.getHeight() != lastDrawn.getHeight() || buf.getWidth() != lastDrawn.getWidth())
		{
			buf = new BufferedImage(lastDrawn.getWidth(),lastDrawn.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
		}
		Graphics gi = canvas.getGraphics();
		Graphics g = buf.getGraphics();
		g.drawImage(lastDrawn, 0, 0, null);
		g.setColor(Color.WHITE);
		g.drawRect(recogBounds.x, recogBounds.y, 
				recogBounds.width, recogBounds.height);
		g.setColor(Color.RED);
		if(lastResult!=null)
		{
			g.drawString(lastResult.toString(), 0, 10);
		}
		
		g.setColor(Color.RED);
		
		for(CardCandidate cc:ccs)
		{
			cc.draw(g,recogBounds.x,recogBounds.y);
		}
		ccs.clear();
		
		gi.drawImage(buf, 0, 0, null);
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
		recogBounds = new Rectangle(0,0,0,0);
		recogBounds.height = (int)(cam.getViewSize().getHeight()*8/10);
		recogBounds.width = recogBounds.height*63/88;
		recogBounds.x = (int)(cam.getViewSize().getWidth()/2-recogBounds.width/2);
		recogBounds.y = (int)(cam.getViewSize().getHeight()/2-recogBounds.height/2);
	}


	public void moveBox(MouseEvent a) {
		if(lastDrawn!=null)
		{
			Point b = a.getPoint();
			int w = lastDrawn.getWidth();
			int h = lastDrawn.getHeight();
			int w2 = recogBounds.width/2;
			int h2 = recogBounds.height/2;
			int x = (int)b.getX();
			int y = (int)b.getY();

			if(x>w-w2)
			{
				x=w-w2;
			}
			else if(x<w2)
			{
				x=w2;
			}

			if(y>h-h2)
			{
				y=h-h2;
			}
			else if(y<h2)
			{
				y=h2;
			}

			recogBounds.x = x-w2;
			recogBounds.y = y-h2;
		}
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
		if(!SettingsPanel.LOCK_BOUNDS)
		{
			moveBox(arg0);
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	public void setLastResult(MatchResult lastResult) {
		this.lastResult = lastResult;
	}
	
}
