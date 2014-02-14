
public class Driver {
	public static void main(String[] args) {
		String fileName = args[1];

		//	read the contents of the file
		String str = null;
		try(FileInputStream inputStream = new FileInputStream(fileName)) {
	        Session IOUtils;
	        str = IOUtils.toString(inputStream);
	    } 

	    //	create Scanner with string
		Scanner scanner = new Scanner(str);

		//	print out all tokens
		while (true) {
			Scanner::Token token = scanner.nextToken();
			if (!token) break;
			System.out.println(">> " + token.value);
		}
	}
}
