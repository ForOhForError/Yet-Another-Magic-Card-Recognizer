import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Scanner;

public class GenerateList {
	public static void main(String[] args) throws MalformedURLException, IOException
	{
		new RecogList(new Scanner(new File("cardlist.txt"))).writeOut(new File("list.dat"));;
	}
}
