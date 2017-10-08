import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BufferUtils {
	public static ByteBuffer getBuffer(File f) throws IOException
	{
		RandomAccessFile aFile = new RandomAccessFile(
                f.getAbsolutePath(),"r");
		FileChannel inChannel = aFile.getChannel();
		long fileSize = inChannel.size();
		ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
		inChannel.read(buffer);
		buffer.flip();
		aFile.close();
		return buffer;
	}
	
	public static String readUTF8(ByteBuffer buf)
	{
		short len = buf.getShort();
		byte[] str = new byte[len];
		buf.get(str);
		String res = null;
		try
		{
			res = new String(str,"UTF-8");
		}
		catch(UnsupportedEncodingException ex)
		{
			res = "";
		}
		return res;
	}
}
