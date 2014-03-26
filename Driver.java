import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Driver {
	public static void main(String[] args) {
		
		final String stdlibPath = "./stdlib.tiger";		
		String fileName = args[0];

		// grab stdlib and create scanner
		String stdlibText = getFileText(stdlibPath);
		Scanner stdlibScanner = new Scanner(stdlibText);
		
		// get source file text and create scanner
		String sourceText = getFileText(fileName);
		Scanner sourceScanner = new Scanner(sourceText);

		// build grammar
		Grammar grammar = new TigerGrammar();

		//	create parser
		Parser parser = new Parser(grammar);
		
		// parse stdlib file
		parser.parseText(stdlibScanner, false);
		
		// parse input file
		parser.parseText(sourceScanner, true);

		//	write debug files
		writeFile("parser_rules.txt", grammar.prettyPrintedRules());
		writeFile("parser_table.csv", parser.generateParseTableCSV());
		writeFile("first_sets.txt", parser.prettyPrintedFirstSets());
		writeFile("follow_sets.txt", parser.prettyPrintedFollowSets());

		parser.printTree();
	}
	
	public static String getFileText(String fileName){
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
		return str;
	}

	public static void writeFile(String fileName, String contents) {
		try {
			PrintWriter fileWriter = new PrintWriter(fileName);
			fileWriter.print(contents);
			fileWriter.close();
		}
		catch (FileNotFoundException fnf) {
			System.err.println(fnf);
		}
	}
}
