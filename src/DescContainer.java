
public class DescContainer implements Comparable<DescContainer>{
	public DescContainer(ImageDesc descData, String stringData) {
		super();
		this.descData = descData;
		this.stringData = stringData;
	}
	
	public DescContainer(DescContainer d)
	{
		this.descData = d.descData;
		this.stringData = d.stringData;
	}
	
	public ImageDesc descData;
	public String stringData;
	
	public double match = 0;

	@Override
	public int compareTo(DescContainer arg0) {
		return Double.compare(arg0.match,this.match);
	}
	
}
