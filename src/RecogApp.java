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
	public static RecognitionStrategy strat;
	private static RecognitionCanvas canvas;
	public static SetLoadPanel select;
	public static RecogApp INSTANCE;
	public static OperationBar task;

	public static void main(String[] args)
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (final Exception e) {
			System.err.println(e.getMessage());
		}
		SavedConfig.init();
		new RecogApp();
	}

	public RecogApp()
	{
		super("Yet Another Magic Card Recognizer");
		INSTANCE = this;
		final BorderLayout bl = new BorderLayout();
		setLayout(bl);
		task = new OperationBar();
		strat = SavedConfig.getStrat();

		SetListing.init();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final Webcam w = WebcamUtils.getPreferredElseChooseWebcam();

		if(w==null)
		{
			System.exit(1);
		}

		final JPanel right = new JPanel();
		right.setLayout(new GridLayout(2,1));

		canvas = new RecognitionCanvas(w);

		ImageIcon ico = null;
		ico = new ImageIcon("res/YamCR.png");
		setIconImage(ico.getImage());

		final JScrollPane scroll = new JScrollPane();
		select = new SetLoadPanel(strat);
		scroll.setViewportView(select);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(canvas,BorderLayout.CENTER);
		add(right,BorderLayout.EAST);
		add(task,BorderLayout.SOUTH);
		right.add(new SettingsPanel());
		right.add(scroll);
		right.setPreferredSize(new Dimension(300,canvas.getHeight()));
		pack();
		setVisible(true);
		setResizable(false);
		try{
			w.open(true);
		}catch(final WebcamLockException e)
		{
			JOptionPane.showMessageDialog(null, "Webcam already in use. Exiting.");
			System.exit(0);
		}
		canvas.getCanvas().addKeyListener(this);
		while(true)
		{
			canvas.draw();
			task.repaint();
			if(SettingsPanel.RECOG_EVERY_FRAME)
			{
				doRecog();
			}
		}
	}

	public void doSetStrat(final RecognitionStrategy strategy)
	{
		synchronized(strat)
		{
			strat.clear();
			strat = strategy;
			SavedConfig.setPreferredStrat(strat);
		}
	}

	public void doSetWebcam()
	{
		synchronized(canvas)
		{
			final Webcam w = WebcamUtils.chooseWebcam();
			if(w != null)
			{
				canvas.setWebcam(w);
				pack();
			}
		}
	}

	public void doRecog()
	{
		synchronized(canvas)
		{
			final BufferedImage img = canvas.getBoundedZone();
			doRecog(img);
		}
	}

	public static void doRecog(BufferedImage img)
	{
		if(!task.isOperating())
		{
			if(img!=null)
			{
				img = ImageUtil.getScaledImage(img);
				final ImageDesc id = new ImageDesc(img);
				synchronized(strat){
					final MatchResult res = strat.getMatch(id, SettingsPanel.RECOG_THRESH/100f);
					if(res!=null){
						canvas.setLastResult(res);
						PopoutCardWindow.setDisplay(res.scryfallId,res.name);
					}
				}
			}
		}
	}

	public OperationBar getOpBar()
	{
		return task;
	}

	@Override
	public void keyPressed(final KeyEvent arg0) {
		doRecog();
	}

	@Override
	public void keyReleased(final KeyEvent arg0) {
	}

	@Override
	public void keyTyped(final KeyEvent arg0) {
	}
}
