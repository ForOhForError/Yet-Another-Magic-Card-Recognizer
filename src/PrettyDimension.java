import java.awt.Dimension;

public class PrettyDimension extends Dimension{
	private static final long serialVersionUID = 1L;

	public PrettyDimension(Dimension d)
	{
		width = d.width;
		height = d.height;
	}
	
	public String toString()
	{
		return width+"x"+height;
	}
}
