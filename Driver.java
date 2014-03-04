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

		// build grammar
		Grammar grammar = buildTigerGrammar();

		Parser parser = new Parser(scanner, grammar);

		parser.parseText();
	}

	public static Grammar buildTigerGrammar(){
		Grammar grm = new Grammar();
		// TIGER_PROGRAM
		grm.addRule(new ProductionRule(NonTerminals.TIGER_PROGRAM, new ParserSymbol[] {
			new TerminalParserSymbol(Terminals.LET), new NonTerminalParserSymbol(NonTerminals.DECLARATION_SEGMENT),
			new TerminalParserSymbol(Terminals.IN), new NonTerminalParserSymbol(NonTerminals.STAT_SEQ),
			new TerminalParserSymbol(Terminals.END)
		}));
		
		// DECLARATION_SEGMENT
		grm.addRule(new ProductionRule(NonTerminals.DECLARATION_SEGMENT, new ParserSymbol[] {
			new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION_LIST), new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION_LIST),
			new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION_LIST)
		}));
		
		// TYPE_DECLARATION_LIST
		grm.addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION_LIST, new ParserSymbol[] {
			new TerminalParserSymbol(Terminals.NULL)	
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION_LIST, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION), new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION_LIST)
		}));
		
		// VAR_DECLARATION_LIST
		grm.addRule(new ProductionRule(NonTerminals.VAR_DECLARATION_LIST, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.VAR_DECLARATION_LIST, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION), new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION_LIST)
		}));
		
		// FUNCT_DECLARATION_LIST
		grm.addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION_LIST, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.NULL)	
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION_LIST, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION), new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION_LIST)	
		}));
		
		// TYPE_DECLARATION
		grm.addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.TYPE), new TerminalParserSymbol(Terminals.ID), 
			new TerminalParserSymbol(Terminals.EQ), new NonTerminalParserSymbol(NonTerminals.TYPE)
		}));
		
		// TYPE
		grm.addRule(new ProductionRule(NonTerminals.TYPE, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.ARRAY), new TerminalParserSymbol(Terminals.LBRACK), new TerminalParserSymbol(Terminals.INTLIT),
				new TerminalParserSymbol(Terminals.RBRACK), new NonTerminalParserSymbol(NonTerminals.TYPEDIM)
		}));
		
		//TYPEDIM
		grm.addRule(new ProductionRule(NonTerminals.TYPEDIM, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.RBRACK), new TerminalParserSymbol(Terminals.INTLIT), new TerminalParserSymbol(Terminals.LBRACK),
			new NonTerminalParserSymbol(NonTerminals.TYPEDIM)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.TYPEDIM, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.NULL)
		}));
		
		// TYPE_ID
		grm.addRule(new ProductionRule(NonTerminals.TYPE_ID, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.INTLIT)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.TYPE_ID, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.STRLIT)	
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.TYPE_ID, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.ID)
		}));
		
		// VAR_DECLARATION
		grm.addRule(new ProductionRule(NonTerminals.VAR_DECLARATION, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.VAR), new NonTerminalParserSymbol(NonTerminals.ID_LIST),
				new TerminalParserSymbol(Terminals.COLON), new NonTerminalParserSymbol(NonTerminals.TYPE_ID),
				new NonTerminalParserSymbol(NonTerminals.OPTIONAL_INIT), new TerminalParserSymbol(Terminals.SEMI)
		}));
		
		
		
		
		
		
		
		
		
		
	}
}
