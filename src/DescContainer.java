
public class DescContainer implements Comparable<DescContainer>{
	public DescContainer(ImageDesc descData, String stringData, long setNo) {
		super();
		this.descData = descData;
		this.stringData = stringData;
		this.setNo = setNo;
	}
	
	public DescContainer(DescContainer d)
	{
		this.descData = d.descData;
		this.stringData = d.stringData;
		this.setNo = d.setNo;
	}
	
	public ImageDesc descData;
	public String stringData;
	public long setNo;
	
	public double match = 0;

	@Override
	public int compareTo(DescContainer arg0) {
		return Double.compare(arg0.match,this.match);
	}
	
}
