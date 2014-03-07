import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Driver {
	public static void main(String[] args) {
		
		// build grammar
		Grammar grammar = new TigerGrammar();

		//	create parser
		Parser parser = new Parser(null, grammar);

		//	write debug files
		writeFile("parser_rules.txt", grammar.prettyPrintedRules());
		writeFile("parser_table.csv", parser.generateParseTableCSV());
		writeFile("first_sets.txt", parser.prettyPrintedFirstSets());
		writeFile("follow_sets.txt", parser.prettyPrintedFollowSets());

		//	parse!
		boolean success = parser.parseText();
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
