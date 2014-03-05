import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

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

		// build grammar
		Grammar grammar = new TigerGrammar();

		//	create parser
		Parser parser = new Parser(scanner, grammar);

		//	write debug files
		writeFile("parser_rules.txt", grammar.prettyPrintedRules());
		writeFile("parser_table.csv", parser.generateParseTableCSV());
		writeFile("first_sets.txt", parser.prettyPrintedFirstSets());
		writeFile("follow_sets.txt", parser.prettyPrintedFollowSets());

		//	parse!
		parser.parseText();
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
