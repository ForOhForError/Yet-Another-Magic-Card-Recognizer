import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class SetLoadPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private JCheckBox[] checks;
	private String[] names;
	private File[] files;
	private RecogStrategy strat;
	
	private boolean loaded = false;
	
	private int dir = 0;
	private String[] dirs;

	public SetLoadPanel(RecogStrategy st)
	{
		super();
		strat = st;
		File folder = new File(SavedConfig.PATH);
		loadListFromPath(folder);
		dirs = new String[2];
		dirs[0] = SavedConfig.PATH;
		dirs[1] = SavedConfig.getDecksPath();
	}

	public void actionPerformed(ActionEvent arg0) {
		int i = Integer.parseInt(((JCheckBox)arg0.getSource()).getName());
		if(checks[i].isSelected())
		{
			checks[i].setEnabled(false);
			synchronized(strat){
				try {
					strat.addFromFile(files[i]);
				} catch (Exception e1) {
					checks[i].setSelected(false);
				}
			}
		}
	}

	public int getBoxWidth()
	{
		int max = 0;
		for(JCheckBox c:checks)
		{
			int w=c.getText().length()*7;
			if(w>max)
			{
				max=w;
			}
		}
		return max;
	}

	public void loadListFromPath(File folder)
	{
		if(loaded)
		{
			removeAll();
			unloadAll();
		}
		File[] flist = folder.listFiles();
		ArrayList<File> good = new ArrayList<>();
		int size = 0;
		for(int i = 0; i < flist.length; i++){
			if(flist[i].isFile() && flist[i].getName().endsWith(".dat")) {
				good.add(flist[i]);
				size++;
			}
		}
		files = new File[size];
		names = new String[size];
		checks = new JCheckBox[size];
		setLayout(new GridLayout(size,1));
		int i=0;
		for(File f:good)
		{
			String name = RecogList.getNameFromFile(f);
			names[i]=name;
			checks[i]=new JCheckBox(name);
			files[i]=f;
			add(checks[i]);
			checks[i].addActionListener(this);
			checks[i].setName(i+"");
			i++;
		}
		loaded=true;
		if(this.getRootPane()!=null)
		{
			this.getRootPane().validate();
		}
	}
	
	
	public void loadAll()
	{
		Counter c = new Counter();
		for(int i=0;i<names.length;i++)
		{
			final int x = i;
			new Thread()
			{
				public void run()
				{
					if(!checks[x].isSelected())
					{
						try {
							checks[x].setEnabled(false);
							synchronized(strat)
							{
								strat.addFromFile(files[x]);
								synchronized(c)
								{
									c.count = c.count+1;
									if(c.count==names.length)
									{
										strat.finalizeLoad();
									}
								}
							}
							checks[x].setSelected(true);
						} catch (Exception e1) {
							checks[x].setSelected(false);
							checks[x].setEnabled(true);
						}
					}
				}
			}.start();

		}
	}

	public void toggle()
	{
		dir = (dir+1)%dirs.length;
		loadListFromPath(new File(dirs[dir]));
	}
	
	public void unloadAll()
	{
		strat.clear();
		for(int i=0;i<checks.length;i++)
		{
			checks[i].setSelected(false);
			checks[i].setEnabled(true);
		}
	}
}
