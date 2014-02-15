import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Driver {
	public static void main(String[] args) {
		String fileName = args[0];


		String str = "";

		try {
			//	read the full contents of the file into @str
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			int c;
			while ( (c = br.read()) != -1) {
				str += (char)c;
			}
		}
		catch (FileNotFoundException fnf) {
			System.out.println("File not found!");
			return;
		}
		catch (IOException ioexc) {
			System.out.println("IO Error");
			return;
		}


	    //	create Scanner with string
		Scanner scanner = new Scanner(str);

		//	print out all tokens
		while (true) {
			Token token = scanner.nextToken();
			if (token == null) break;
			System.out.println(">> " + token.value);
		}
	}
}
