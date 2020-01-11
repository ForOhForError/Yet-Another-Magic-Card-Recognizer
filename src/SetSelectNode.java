import java.io.File;
import java.util.Date;

import javax.swing.tree.DefaultMutableTreeNode;

import forohfor.scryfall.api.Set;

class SetSelectNode extends DefaultMutableTreeNode implements Comparable<SetSelectNode>
{
	private static final long serialVersionUID = 1L;
	private boolean loaded;
	private boolean exists;
	private boolean hasParent;
	private Date releasedAt;
	private File filePath;
	private Set set;

	protected SetSelectNode(Set set)
	{
		super(set.getName());
		this.set = set;
		hasParent = false;
		loaded = false;
		releasedAt = set.getReleasedAt();
		filePath = new File(SavedConfig.getSetPath(set.getCode()));
		exists = filePath.exists() && filePath.isFile();
	}
	
	protected SetSelectNode(File f)
	{
		super(ListRecogStrat.getNameFromFile(f));
		loaded = false;
		hasParent = false;
		filePath = f;
		exists = true;
	}

	public boolean isLoaded() {
		return loaded;
	}
	
	public boolean fileExists()
	{
		return exists;
	}
	
	public boolean hasParent()
	{
		return hasParent;
	}
	
	public boolean isSet()
	{
		return set != null;
	}

	public void setLoaded(boolean l) {
		loaded = l;
	}
	
	public File getFilePath()
	{
		return filePath;
	}
	
	public void setParent(DefaultMutableTreeNode node)
	{
		node.add(this);
		hasParent = true;
	}
	
	public Set getSet()
	{
		return set;
	}

	public boolean download()
	{
		if(isSet() && !exists)
		{
			boolean success = SetGenerator.generateSet(set);
			if(success)
			{
				exists = true;
			}
			return success;
		}
		return false;
	}

	@Override
	public int compareTo(SetSelectNode other) {
		if(releasedAt != null && other.releasedAt != null)
		{
			return -1*releasedAt.compareTo(other.releasedAt);
		}
		return toString().compareTo(other.toString());
	}
}