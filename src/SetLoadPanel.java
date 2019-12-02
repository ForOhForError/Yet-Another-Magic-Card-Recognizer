import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import forohfor.scryfall.api.Set;


public class SetLoadPanel extends JPanel implements TreeSelectionListener{
	private static final long serialVersionUID = 1L;

	private RecogStrategy strat;

	private JTree tree;
	private DefaultMutableTreeNode root;

	private ImageIcon loaded = new ImageIcon("res/Loaded.png");
	private ImageIcon unloaded = new ImageIcon("res/Unloaded.png");
	private ImageIcon notdown = new ImageIcon("res/NotDownloaded.png");

	private ArrayList<SetSelectNode> allNodes = new ArrayList<SetSelectNode>();


	public SetLoadPanel(RecogStrategy st)
	{
		super();
		setLayout(new GridLayout(0, 1));
		strat = st;

		root = new DefaultMutableTreeNode("root");
		tree = new JTree(root);
		tree.setRootVisible(false);
		tree.getSelectionModel().addTreeSelectionListener(this);
		tree.setCellRenderer(new SetSelectTreeRenderer());
		add(tree);

		buildDisplayTree();
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		Object sel = tree.getLastSelectedPathComponent();
		if(sel instanceof SetSelectNode)
		{
			SetSelectNode node = (SetSelectNode) tree.getLastSelectedPathComponent();
			if(!node.isLoaded() && node.fileExists())
			{
				synchronized(strat){
					strat.addFromFile(node.getFilePath());
					strat.finalizeLoad();
					node.setLoaded(true);
				}
			}
		}
	}

	public void actionPerformed(ActionEvent arg0) {

		System.out.println(arg0);
	}

	public int getBoxWidth()
	{
		return 50;
	}

	public void buildDisplayTree()
	{
		allNodes.clear();
		root.removeAllChildren();

		root.add(buildSetSubtree());
		root.add(buildDeckSubtree());

		((DefaultTreeModel)tree.getModel()).reload();
		if(this.getRootPane()!=null)
		{
			this.getRootPane().validate();
		}
	}

	private DefaultMutableTreeNode buildSetSubtree()
	{
		DefaultMutableTreeNode subtree = new DefaultMutableTreeNode("Sets");

		ArrayList<SetSelectNode> nodes = new ArrayList<SetSelectNode>();
		HashMap<String,SetSelectNode> bySetCode = new HashMap<String,SetSelectNode>();

		ArrayList<Set> allSets = SetListing.getSets();

		for(Set set:allSets)
		{
			SetSelectNode node = new SetSelectNode(set);
			nodes.add(node);
			allNodes.add(node);
			bySetCode.put(set.getCode(), node);
		}
		Collections.sort(nodes);

		for(Set set:allSets)
		{
			String parent = set.getParentSetCode();
			if(parent != null)
			{
				bySetCode.get(set.getCode()).setParent(bySetCode.get(parent));
			}
		}

		for(SetSelectNode node:nodes)
		{
			if(!node.hasParent())
			{
				subtree.add(node);
			}
		}

		return subtree;
	}

	public DefaultMutableTreeNode buildDeckSubtree()
	{
		DefaultMutableTreeNode subtree = new DefaultMutableTreeNode("Decks");
		try
		{
			File[] flist = new File(SavedConfig.getDecksPath()).listFiles();

			ArrayList<File> good = new ArrayList<>();
			for(int i = 0; i < flist.length; i++){
				if(flist[i].isFile() && flist[i].getName().endsWith(".dat")) {
					good.add(flist[i]);
				}
			}
			for(File f:good)
			{
				SetSelectNode node = new SetSelectNode(f);
				subtree.add(node);
				allNodes.add(node);
			}
		}
		catch(Exception e)
		{

		}

		return subtree;
	}

	public void loadSelected()
	{
		DefaultMutableTreeNode selected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		ArrayList<SetSelectNode> toLoad = new ArrayList<SetSelectNode>();

		if(selected != null)
		{
			@SuppressWarnings("unchecked")
			Enumeration<DefaultMutableTreeNode> nodes = selected.breadthFirstEnumeration();

			while(nodes.hasMoreElements())
			{
				DefaultMutableTreeNode node = nodes.nextElement();
				if(node instanceof SetSelectNode)
				{
					SetSelectNode snode = (SetSelectNode)node;
					if(snode.fileExists() && !snode.isLoaded())
					{
						toLoad.add(snode);
					}
				}
			}
		}

		Counter c = new Counter();
		for(final SetSelectNode node:toLoad)
		{
			new Thread()
			{
				public void run()
				{
					try {
						synchronized(strat)
						{
							strat.addFromFile(node.getFilePath());
							synchronized(c)
							{
								c.count += 1;
								if(c.count==toLoad.size())
								{
									strat.finalizeLoad();
								}
							}
						}
						node.setLoaded(true);
						tree.repaint();
					} catch (Exception e1) {
						e1.printStackTrace();
						node.setLoaded(false);
					}
				}
			}.start();

		}
	}

	public void downloadSelected()
	{
		DefaultMutableTreeNode selected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		ArrayList<SetSelectNode> toDownload = new ArrayList<SetSelectNode>();

		if(selected != null)
		{
			@SuppressWarnings("unchecked")
			Enumeration<DefaultMutableTreeNode> nodes = selected.breadthFirstEnumeration();

			while(nodes.hasMoreElements())
			{
				DefaultMutableTreeNode node = nodes.nextElement();
				if(node instanceof SetSelectNode)
				{
					SetSelectNode snode = (SetSelectNode)node;
					if(!snode.fileExists() && snode.isSet())
					{
						toDownload.add(snode);
					}
				}
			}
		}
		int confirm = JOptionPane.YES_OPTION;
		if(toDownload.size() > 3)
		{
			confirm = JOptionPane.showConfirmDialog(null, 
					"You are trying to generate "+toDownload.size()+" sets in the background. Generating "
							+ "multiple sets is a time-intensive operation. Continue?", "Confirm multiple set generation", 
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.PLAIN_MESSAGE, null);
		}

		if(confirm == JOptionPane.YES_OPTION)
		{
			for(final SetSelectNode node:toDownload)
			{
				new Thread()
				{
					public void run()
					{
						boolean downloaded = node.download();
						if(downloaded)
						{
							tree.repaint();
						}
					}
				}.start();
			}
		}
	}

	public void refresh()
	{
		unloadAll();
		buildDisplayTree();
	}

	public void unloadAll()
	{
		strat.clear();
		for(SetSelectNode node : allNodes)
		{
			node.setLoaded(false);
		}
		tree.repaint();
	}

	@SuppressWarnings("serial")
	class SetSelectTreeRenderer extends DefaultTreeCellRenderer
	{
		@Override
		public Component getTreeCellRendererComponent(
				JTree tree, Object value, boolean sel, 
				boolean expanded, boolean leaf, 
				int row, boolean hasFocus
				) 
		{
			JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if(value instanceof SetSelectNode)
			{
				SetSelectNode node = (SetSelectNode)value;

				if(node.fileExists())
				{
					if(node.isLoaded())
					{
						label.setIcon(loaded);
					}
					else
					{
						label.setIcon(unloaded);
					}
				}
				else
				{
					label.setIcon(notdown);
				}
			}
			return label;
		}
	}
}
