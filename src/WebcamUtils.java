import java.awt.Dimension;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamCompositeDriver;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;

import forohfor.scryfall.api.JSONUtil;

public class WebcamUtils {
	public static class CompositeDriver extends WebcamCompositeDriver {

		public CompositeDriver() {
			add(new WebcamDefaultDriver());
			add(new IpCamDriver());
		}
	}
	
	private static boolean INIT = false;
	
	private static String prefCamName = "";
	private static Dimension prefCamRes = null;
	
	public static Webcam chooseWebcam()
	{
		LinkedList<PrettyWebcam> pcams = new LinkedList<PrettyWebcam>();
		for(Webcam cam:Webcam.getWebcams())
		{
			pcams.add(new PrettyWebcam(cam));
		}
		pcams.add(new PrettyWebcam(new DummyWebcam()));
		
		PrettyWebcam pw = (PrettyWebcam) JOptionPane.showInputDialog(null, "Choose a webcam", "Select webcam", 
				JOptionPane.PLAIN_MESSAGE, null, 
				pcams.toArray(),Webcam.getDefault());
		
		Webcam w = pw.get();
		
		if(w==null)
		{
			return null;
		}
		PrettyDimension[] dims;
		
		//kill the program if the ip cam isn't up
		Thread t = new Thread()
		{
			public void run()
			{
				try {
					Thread.sleep(1000);
					System.exit(1);
				} catch (InterruptedException e) {
					return;
				}
			}
		};
		t.start();
		dims = new PrettyDimension[w.getViewSizes().length];
		t.interrupt();
		for(int i=0;i<dims.length;i++)
		{
			dims[i] = new PrettyDimension(w.getViewSizes()[i]);
		}
		
		Dimension d = (Dimension) JOptionPane.showInputDialog(null, "Choose a resolution", "Select resolution", 
				JOptionPane.PLAIN_MESSAGE, null, 
				dims,dims[dims.length-1]);
		
		if(d == null)
		{
			return null;
		}

		w.setViewSize(d);
		
		SavedConfig.updateWebcamPrefs(w,d);
		SavedConfig.writeOut();
		
		return w;
	}
	
	public static Webcam getPreferredElseChooseWebcam()
	{
		if(!prefCamName.equals("") && prefCamRes.height > 0 && prefCamRes.width > 0)
		{
			List<Webcam> cams = new ArrayList<Webcam>(Webcam.getWebcams());
			cams.add(new DummyWebcam());
			for(Webcam cam:cams)
			{
				if(cam.getName().equals(prefCamName))
				{
					cam.setViewSize(prefCamRes);
					return cam;
				}
			}
		}
		return chooseWebcam();
	}
	
	public static void clearIPCams()
	{
		IpCamDeviceRegistry.unregisterAll();
	}

	public static void loadSettings(JSONObject obj)
	{
		if(!INIT)
		{
			Webcam.setDriver(new CompositeDriver());
			INIT = true;
		}
		JSONArray jarr = (JSONArray) obj.get("ip_cams");
		prefCamName = JSONUtil.getStringData(obj, "cam_name");
		prefCamRes = new Dimension(
				JSONUtil.getIntData(obj, "cam_resolution_w"),
				JSONUtil.getIntData(obj, "cam_resolution_h")
				);
		for(Object o:jarr.toArray())
		{
			JSONObject entry = (JSONObject)o;
			registerIPCam(
					JSONUtil.getStringData(entry, "name"),
					JSONUtil.getStringData(entry, "address"),
					JSONUtil.getStringData(entry, "mode"),
					JSONUtil.getIntData(entry,"width"),
					JSONUtil.getIntData(entry,"height")
			);
		}
		
	}
	
	public static boolean registerIPCam(String name, String address, String mode, int w, int h)
	{
		try {
			IpCamDevice dev;
			if(mode!=null && mode.equalsIgnoreCase("push"))
			{
				dev = new IpCamDevice(name,address,IpCamMode.PUSH);
			}
			else
			{
				dev = new IpCamDevice(name,address,IpCamMode.PULL);
			}
			dev.setResolution(new Dimension(w,h));
			IpCamDeviceRegistry.register(dev);
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
