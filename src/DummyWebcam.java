import com.github.sarxos.webcam.Webcam;

class DummyWebcam extends Webcam {

    public DummyWebcam() {
        super(new DummyWebcamDevice());
    }

}