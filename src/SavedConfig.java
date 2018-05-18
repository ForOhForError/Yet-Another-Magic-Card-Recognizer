import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class SavedConfig {
	public static String PATH;
	public static boolean DEBUG;
	
	
	public static void init()
	{
		File f = new File("config.txt");
		if(f.exists())
		{
			try {
				Scanner s = new Scanner(f);
				while(s.hasNextLine())
				{
					String l = s.nextLine();
					if(l.startsWith("path:"))
					{
						PATH = l.substring(5);
					}
					if(l.startsWith("debug:"))
					{
						String st = l.substring(6).trim().toLowerCase();
						if(st.equals("true")){
							DEBUG = true;
						}
						else
						{
							DEBUG = false;
						}
					}
				}
				s.close();
			} 
			catch (FileNotFoundException e)
			{
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Config file not found; doing initial setup.");
			JOptionPane.showMessageDialog(null, "You will be prompted for a directory to save set data to.");
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			chooser.setDialogTitle("Select a directory to save set data to");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
				String s = chooser.getSelectedFile().getAbsolutePath();
				if(!s.endsWith(File.separator))
				{
					s = s+File.separator;
				}
				PATH = s;
				DEBUG=false;
				writeOut();
			}
			else {
				System.exit(0);
			}
		}
		
		if(!DEBUG)
		{
			try {
				System.setErr(new PrintStream(new FileOutputStream(new File("errorlog.txt"))));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private static void writeOut()
	{
		File f = new File("config.txt");
		FileOutputStream out;
		try {
			out = new FileOutputStream(f);
			out.write(("path:"+PATH+System.lineSeparator()).getBytes());
			out.write(("debug:"+DEBUG+System.lineSeparator()).getBytes());
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getDecksPath(){
		return PATH+"decks"+File.separator;
	}
}
