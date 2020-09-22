import com.github.sarxos.webcam.Webcam;

public class PrettyWebcam
{
    private Webcam cam;

    public PrettyWebcam(Webcam cam)
    {
        this.cam = cam;
    }

    public Webcam get()
    {
        return cam;
    }

    public String toString()
    {
        if (cam != null)
        {
            return cam.getName();
        }
        return "Unknown";
    }
}
