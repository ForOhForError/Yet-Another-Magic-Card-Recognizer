import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ScreenRegionGrab extends JFrame implements ActionListener, MouseListener{
	private static final long serialVersionUID = 1L;
	
	private static final int BUBBLE_RADIUS = 2;

	private int num;
	private Timer t;

	static Point pt1;
	static Point pt2;
	static Robot rob;

	static ScreenRegionGrab win1;
	static ScreenRegionGrab win2;

	static BufferedImage cap;

	JPanel jp;
	
	public void dispose()
	{
		t.stop();
		super.dispose();
	}

	public static void init()
	{
		if(rob==null)
		{
			try {
				rob = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		win1 = new ScreenRegionGrab(1);
	}

	public ScreenRegionGrab(int n) 
	{
		super("");
		num = n;
		setUndecorated(true);
		setSize(BUBBLE_RADIUS*2+1,BUBBLE_RADIUS*2+1);
		jp = new JPanel(){
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
				g.setColor(new Color(1,1,1,1));
				g.fillOval(0,0,BUBBLE_RADIUS*2,BUBBLE_RADIUS*2);
				g.setColor(Color.RED);
				g.drawOval(0,0,BUBBLE_RADIUS*2,BUBBLE_RADIUS*2);
			}
		};
		setBackground(new Color(0, 0, 0, 0));
		setContentPane(jp);
		setLocationRelativeTo(null);
		setVisible(true);
		t = new Timer(10, this);
		t.start();
		addMouseListener(this);
		if(num==1)
		{
			win1 = this;
		}
		else
		{
			win2 = this;
		}
		this.setAlwaysOnTop(true);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		Point p = MouseInfo.getPointerInfo().getLocation();
		if(num==1)
		{
			pt1 = p;
			win2 = new ScreenRegionGrab(2);
		}
		else
		{
			pt2 = p;
			int x = Math.min(pt1.x,pt2.x);
			int y = Math.min(pt1.y, pt2.y);
			int w = Math.max(pt1.x,pt2.x)-x;
			int h = Math.max(pt1.y,pt2.y)-y;
			BufferedImage a = rob.createScreenCapture(new Rectangle(x,y,w,h));
			RecogApp.doRecog(a);
			win1.dispose();
			win1=win2=null;
			pt1=pt2=null;
			dispose();
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		setBackground(new Color(0, 0, 0, 0));
		jp.repaint();
		if( (num==1&&pt1==null) || (num==2&&pt2==null)){
			Point p = MouseInfo.getPointerInfo().getLocation();
			p.translate(-BUBBLE_RADIUS, -BUBBLE_RADIUS);
			this.setLocation(p);
		}
	}
}
