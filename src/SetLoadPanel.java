import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

public class SetLoadPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private JCheckBox[] checks;
	private String[] names;
	private File[] files;
	private MultiList mlist;

	public SetLoadPanel(MultiList ml)
	{
		super();
		mlist = ml;
		File folder = new File(SavedConfig.PATH);
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

	}

	public void actionPerformed(ActionEvent arg0) {
		int i = Integer.parseInt(((JCheckBox)arg0.getSource()).getName());
		if(checks[i].isSelected())
		{
			synchronized(mlist){
				try {
					mlist.add(new RecogList(files[i]));
				} catch (IOException e1) {
					checks[i].setSelected(false);
				}
			}
		}
		else
		{
			synchronized(mlist){
				mlist.remove(names[i]);
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

	public void loadAll()
	{
		for(int i=0;i<names.length;i++)
		{
			if(!checks[i].isSelected())
			{
				synchronized(mlist){
					try {
						mlist.add(new RecogList(files[i]));
						checks[i].setSelected(true);
					} catch (IOException e1) {
						checks[i].setSelected(false);
					}
				}
			}
		}
	}
	
	public void unloadAll()
	{
		for(int i=0;i<names.length;i++)
		{
			mlist.remove(names[i]);
		}
	}
}
