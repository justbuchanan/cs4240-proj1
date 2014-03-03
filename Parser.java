public class Parser{
	
	private Scanner scanner;

	public Parser(Scanner scanner){
		this.scanner = scanner;
	}

	public void parseText(){
				//	print out all tokens
		while (true) {
			Token token = scanner.nextToken();
			if (token == null) break;
			System.out.println(">> " + token.type + " : '" + token.value + "' (" +  token.lineNumber + ")");
		}
	}

	

}
