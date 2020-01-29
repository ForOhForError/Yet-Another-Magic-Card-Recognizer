import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.github.sarxos.webcam.Webcam;

public class RecognitionCanvas extends JPanel{
	private static final long serialVersionUID = 1L;

	public RecognitionCanvas(Webcam w, AreaRecognitionStrategy s) {
		super();
		cam = w;
		strat = s;
		canvas = new Canvas();
		setSize(w.getViewSize());
		canvas.setSize(w.getViewSize());
		add(canvas);
		canvas.addMouseListener(strat);
		canvas.addMouseMotionListener(strat);
		strat.init(cam.getViewSize().width, cam.getViewSize().height);
	}

	private Webcam cam;
	private Canvas canvas;
	private BufferedImage lastDrawn;
	private BufferedImage buf;
	private MatchResult lastResult;
	private AreaRecognitionStrategy strat;

	public void setWebcam(Webcam w)
	{
		cam.close();
		cam = w;
		setSize(w.getViewSize());
		canvas.setSize(w.getViewSize());
		strat.init(cam.getViewSize().width, cam.getViewSize().height);
		add(canvas);
	}

	public void setAreaStrat(AreaRecognitionStrategy s)
	{
		canvas.removeMouseListener(strat);
		canvas.removeMouseMotionListener(strat);
		strat = s;
		canvas.addMouseListener(strat);
		canvas.addMouseMotionListener(strat);
		strat.init(cam.getViewSize().width, cam.getViewSize().height);
	}

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
		strat.draw(g);
		g.setColor(Color.RED);
		if(lastResult!=null)
		{
			g.drawString(lastResult.toString(), 0, 10);
		}
		gi.drawImage(buf, 0, 0, null);
	}

	public void close()
	{
		cam.close();
	}

	public void setLastResult(MatchResult lastResult) {
		this.lastResult = lastResult;
	}

}
