import java.awt.image.BufferedImage;

public class ExtractResult {
	public ExtractResult(BufferedImage image, boolean s, double a) {
		super();
		this.image = image;
		this.success = s;
		this.area = a;
	}
	public BufferedImage image;
	public boolean success;
	public double area;
}
