import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Scanner;

import javax.swing.JOptionPane;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamCompositeDriver;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;

public class WebcamUtils {
	public static class CompositeDriver extends WebcamCompositeDriver {

		public CompositeDriver() {
			add(new WebcamDefaultDriver());
			add(new IpCamDriver());
		}
	}
	
	public static Webcam chooseWebcam()
	{
		registerAllIPCams("ipcams.txt");
		Webcam.setDriver(new CompositeDriver());
		Webcam w = (Webcam) JOptionPane.showInputDialog(null, "Choose a webcam", "Select webcam", 
				JOptionPane.PLAIN_MESSAGE, null, 
				Webcam.getWebcams().toArray(),Webcam.getDefault());
		if(w==null)
		{
			System.exit(1);
		}

		PrettyDimension[] dims = new PrettyDimension[w.getViewSizes().length];
		for(int i=0;i<dims.length;i++)
		{
			dims[i] = new PrettyDimension(w.getViewSizes()[i]);
		}
		
		Dimension d = (Dimension) JOptionPane.showInputDialog(null, "Choose a resolution", "Select resolution", 
				JOptionPane.PLAIN_MESSAGE, null, 
				dims,dims[dims.length-1]);
		
		if(d == null)
		{
			System.exit(1);
		}

		w.setViewSize(d);
		
		return w;
	}
	
	public static void clearIPCams()
	{
		IpCamDeviceRegistry.unregisterAll();
	}
	
	public static void registerAllIPCams(String fname)
	{
		File f = new File(fname);
		if(f.exists()&&f.isFile())
		{
			try {
				Scanner scan = new Scanner(f);
				while(scan.hasNextLine())
				{
					registerIPCam(scan.nextLine());
				}
				scan.close();
			} catch (FileNotFoundException e) {
			}
		}
	}
	
	public static boolean registerIPCam(String line)
	{
		if(line.startsWith("#"))
		{
			return false;
		}
		
		String[] split = line.trim().split("\\|");
		if(split.length==2)
		{
			return registerIPCam(split[0],split[1],"pull");
		}
		else if(split.length>=3)
		{
			return registerIPCam(split[0],split[1],split[2]);
		}
		else
		{
			return false;
		}
	}
	
	public static boolean registerIPCam(String name, String address, String mode)
	{
		try {
			if(mode!=null && mode.equalsIgnoreCase("push"))
			{
				IpCamDeviceRegistry.register(new IpCamDevice(name,address,IpCamMode.PUSH));
			}
			else
			{
				IpCamDeviceRegistry.register(new IpCamDevice(name,address,IpCamMode.PULL));
			}
			return true;
		} catch (MalformedURLException e) {
			System.out.println(address);
			return false;
		}
	}
}
