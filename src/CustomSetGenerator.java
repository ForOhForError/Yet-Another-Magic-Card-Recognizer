import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.json.simple.JSONObject;


public class CustomSetGenerator extends JFrame{

	private static final long serialVersionUID = 1L;

	private JButton gen;
	private JTextField namebox;

	private static String[] formatNames = ImageIO.getReaderFileSuffixes();

	public CustomSetGenerator()
	{
		super("Custom Set generator");
		JPanel pan = new JPanel();
		pan.setBorder(new EmptyBorder(5, 5, 5, 5));
		gen = new JButton("Generate Set");
		namebox = new JTextField("Enter Set Name");
		setResizable(false);
		gen.addActionListener(e -> 
		{
			writeSet();
		});

		JPanel bot = new JPanel();
		BorderLayout bl = new BorderLayout();
		bl.setVgap(10);
		bot.setLayout(bl);

		bl = new BorderLayout();
		bl.setVgap(10);
		pan.setLayout(bl);
		bot.add(namebox,BorderLayout.CENTER);
		bot.add(gen,BorderLayout.SOUTH);
		pan.add(bot,BorderLayout.SOUTH);

		pan.add(new JLabel(
			"<html>"+
			"To generate a custom set, enter a set name, then press the button below<br>"+
			"and select a directory containing image files to populate the set.<br>"+
			"The names within the set will use the image file names, and ids will be<br>"+
			"assigned randomly."+
			"</html>"
		));
		add(pan);
		pack();
		setVisible(true);
	}

	@SuppressWarnings("unchecked")
	public void writeSet()
	{
		String setName = namebox.getText();
		ListRecogStrat r = new ListRecogStrat(namebox.getText());
		new File(SavedConfig.getSubPath("custom")).mkdirs();
		File f = new File(SavedConfig.getCustomSetPath("custom",namebox.getText()));

		final OperationBar bar = RecogApp.INSTANCE.getOpBar();
		if(bar.setTask("Generating Custom Set...",1))
		{
			new Thread()
			{
				public void run()
				{
					try
					{
						JFileChooser chooser = new JFileChooser();
						chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
						chooser.setDialogTitle("Select image directory");
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						chooser.setAcceptAllFileFilterUsed(false);
						if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
							String folderName = chooser.getSelectedFile().getAbsolutePath();
							if(!folderName.endsWith(File.separator))
							{
								folderName = folderName+File.separator;
							}
							File[] flist = new File(folderName).listFiles();

							ArrayList<File> good = new ArrayList<>();
							for(int i = 0; i < flist.length; i++){
								if(flist[i].isFile()) {
									String fname = flist[i].getName().toLowerCase();
									for(String format:formatNames)
									{
										if(fname.endsWith(format.toLowerCase()))
										{
											good.add(flist[i]);
											break;
										}
									}
								}
							}
							for(File f:good)
							{
								BufferedImage img = ImageIO.read(f);
								ImageDesc id = new ImageDesc(img);
								JSONObject meta = new JSONObject();
								meta.put("name", f.getName().split("\\.")[0]);
								meta.put("set", setName);
								DescContainer dc = new DescContainer(id, UUID.randomUUID().toString(), meta, img);
								r.add(dc);
							}
							r.writeOut(f);
						}
					}
					catch(IOException e)
					{
						System.err.println("Set generation failed.");
						e.printStackTrace();
					}
					bar.progressTask();
				}
			}.start();
		}
	}

}
