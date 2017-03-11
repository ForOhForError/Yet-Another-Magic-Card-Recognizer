import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class BinFile {
	
	public static DataInputStream openRead(String f) throws FileNotFoundException
	{
		return new DataInputStream(new FileInputStream(new File(f)));
	}
	
	public static DataOutputStream openWrite(String f) throws FileNotFoundException
	{
		return new DataOutputStream(new FileOutputStream(new File(f)));
	}
}
