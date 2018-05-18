import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamLockException;

public class RecogApp extends JFrame implements KeyListener{
	private static final long serialVersionUID = 1L;

	public static RecogStrategy strat;

	private static WebcamCanvas wc;

	public static SetLoadPanel select;

	public static void main(String[] args)
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		SavedConfig.init();
		new RecogApp();
	}

	public RecogApp()
	{
		super("Yet Another Magic Card Recognizer");
		BorderLayout bl = new BorderLayout();
		setLayout(bl);

		strat = new TreeRecogStrat();
		//strat = new RecogList();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Webcam w = WebcamUtils.chooseWebcam();

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2,1));

		wc = new WebcamCanvas(w);

		ImageIcon ico = null;
		ico = new ImageIcon("YamCR.png");
		setIconImage(ico.getImage());

		JScrollPane scroll = new JScrollPane();
		select = new SetLoadPanel(strat);
		scroll.setViewportView(select);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(wc,BorderLayout.CENTER);
		add(right,BorderLayout.EAST);
		right.add(new SettingsPanel());
		right.add(scroll);
		right.setPreferredSize(new Dimension(300,wc.getHeight()));
		pack();
		setVisible(true);
		setResizable(false);
		try{
			w.open();
		}catch(WebcamLockException e)
		{
			JOptionPane.showMessageDialog(null, "Webcam already in use. Exiting.");
			System.exit(0);
		}
		wc.getCanvas().addKeyListener(this);
		while(true)
		{
			wc.draw();
			if(SettingsPanel.RECOG_EVERY_FRAME)
			{
				doRecog();
			}
		}
	}

	public void doRecog()
	{
		BufferedImage img = wc.getBoundedZone();
		doRecog(img);
	}

	public static void doRecog(BufferedImage img)
	{
		if(img!=null)
		{
			img = ImageUtil.getScaledImage(img);
			ImageDesc id = new ImageDesc(img);
			synchronized(strat){
				MatchResult res = strat.getMatch(id, SettingsPanel.RECOG_THRESH/100f);
				if(res!=null){
					wc.setLastResult(res);
					PopoutCardWindow.setDisplay(res.scryfallId,res.name);
				}
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		doRecog();
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}
}
