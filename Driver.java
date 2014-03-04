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
		
		// ID_LIST
		grm.addRule(new ProductionRule(NonTerminals.ID_LIST, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.ID), new NonTerminalParserSymbol(NonTerminals.ID_LIST_PRIME)
		}));
		
		// ID_LIST_PRIME
		grm.addRule(new ProductionRule(NonTerminals.ID_LIST_PRIME, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.ID_LIST_PRIME, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.COMMA), new NonTerminalParserSymbol(NonTerminals.ID_LIST)
		}));
		
		// OPTIONAL_INIT
		grm.addRule(new ProductionRule(NonTerminals.OPTIONAL_INIT, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.OPTIONAL_INIT, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.ASSIGN), new NonTerminalParserSymbol(NonTerminals.CONST)
		}));
		
		// FUNCT_DECLARATION
		grm.addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.FUNC), new TerminalParserSymbol(Terminals.ID), new TerminalParserSymbol(Terminals.LPAREN),
				new NonTerminalParserSymbol(NonTerminals.PARAM_LIST), new TerminalParserSymbol(Terminals.RPAREN), 
				new NonTerminalParserSymbol(NonTerminals.RET_TYPE), new TerminalParserSymbol(Terminals.BEGIN), 
				new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new TerminalParserSymbol(Terminals.END), new TerminalParserSymbol(Terminals.SEMI)
		}));
		
		// PARAM_LIST
		grm.addRule(new ProductionRule(NonTerminals.PARAM_LIST, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.PARAM_LIST, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.PARAM), new NonTerminalParserSymbol(NonTerminals.PARAM_LIST_TAIL)
		}));
		
		// PARAM_LIST_TAIL
		grm.addRule(new ProductionRule(NonTerminals.PARAM_LIST_TAIL, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.PARAM_LIST_TAIL, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.COMMA), new NonTerminalParserSymbol(NonTerminals.PARAM),
				new NonTerminalParserSymbol(NonTerminals.PARAM_LIST_TAIL)
		}));
		
		// RET_TYPE
		grm.addRule(new ProductionRule(NonTerminals.RET_TYPE, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.RET_TYPE, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.COLON), new NonTerminalParserSymbol(NonTerminals.TYPE_ID)
		}));
		
		// PARAM
		grm.addRule(new ProductionRule(NonTerminals.PARAM, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.ID), new TerminalParserSymbol(Terminals.COLON),
				new NonTerminalParserSymbol(NonTerminals.TYPE_ID)
		}));
		
		// STAT_SEQ
		grm.addRule(new ProductionRule(NonTerminals.STAT_SEQ, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.STAT), new NonTerminalParserSymbol(NonTerminals.STAT_SEQ_PRIME)
		}));
		
		// STAT_SEQ_PRIME
		grm.addRule(new ProductionRule(NonTerminals.STAT_SEQ_PRIME, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.STAT), new NonTerminalParserSymbol(NonTerminals.STAT),
				new NonTerminalParserSymbol(NonTerminals.STAT_SEQ_PRIME)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT_SEQ_PRIME, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.NULL)
		}));
		
		// STAT
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.LVALUE), new TerminalParserSymbol(Terminals.ASSIGN), new NonTerminalParserSymbol(NonTerminals.EXPR),
				new TerminalParserSymbol(Terminals.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.WHILE), new NonTerminalParserSymbol(NonTerminals.EXPR), new TerminalParserSymbol(Terminals.DO),
				new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new TerminalParserSymbol(Terminals.ENDDO), new TerminalParserSymbol(Terminals.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
				new TerminalParserSymbol(Terminals.FOR), new TerminalParserSymbol(Terminals.ID), new TerminalParserSymbol(Terminals.ASSIGN),
				new NonTerminalParserSymbol(NonTerminals.EXPR), new TerminalParserSymbol(Terminals.TO), new NonTerminalParserSymbol(NonTerminals.EXPR), 
				new TerminalParserSymbol(Terminals.DO), new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new TerminalParserSymbol(Terminals.ENDDO), 
				new TerminalParserSymbol(Terminals.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.OPT_PREFIX), new TerminalParserSymbol(Terminals.ID), new TerminalParserSymbol(Terminals.LPAREN),
			new NonTerminalParserSymbol(NonTerminals.EXPR_LIST), new TerminalParserSymbol(Terminals.RPAREN), new TerminalParserSymbol(Terminals.SEMI)
		}));

		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.BREAK), new TerminalParserSymbol(Terminals.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.RETURN), new NonTerminalParserSymbol(NonTerminals.EXPR), new TerminalParserSymbol(Terminals.SEMI)
		}));
		
		// STAT_IF
		grm.addRule(new ProductionRule(NonTerminals.STAT_IF, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.IF), new NonTerminalParserSymbol(NonTerminals.EXPR), new TerminalParserSymbol(Terminals.THEN),
			new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new NonTerminalParserSymbol(NonTerminals.STAT_IF_CLAUSE_2)
		}));
		
		// STAT_IF_CLAUSE_2
		grm.addRule(new ProductionRule(NonTerminals.STAT_IF_CLAUSE_2, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.ENDIF), new TerminalParserSymbol(Terminals.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT_IF_CLAUSE_2, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.ELSE), new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new TerminalParserSymbol(Terminals.ENDIF),
			new TerminalParserSymbol(Terminals.SEMI)
		}));
		
		//OPT_PREFIX
		grm.addRule(new ProductionRule(NonTerminals.OPT_PREFIX, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.LVALUE), new TerminalParserSymbol(Terminals.ASSIGN)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.OPT_PREFIX, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.NULL)
		}));
		
		// EXPR
		grm.addRule(new ProductionRule(NonTerminals.EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.ATOM_EXPR)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.NEGATED_EXPR)
		}));
		
		//ATOM_EXPR
		grm.addRule(new ProductionRule(NonTerminals.ATOM_EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.CONST)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.ATOM_EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.LVALUE)
		}));
		
		//NEGATED_EXPR
		grm.addRule(new ProductionRule(NonTerminals.NEGATED_EXPR, new ParserSymbol[]{
			new TerminalParserSymbol(Terminals.MINUS), new NonTerminalParserSymbol(NonTerminals.ATOM_EXPR)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.NEGATED_EXPR, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.ATOM_EXPR)
		}));
		
		// MULT_DIV_EXPR
		grm.addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.MULT_DIV_EXPR), new TerminalParserSymbol(Terminals.MULT), 
				new NonTerminalParserSymbol(NonTerminals.NEGATED_EXPR)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.NEGATED_EXPR)
		}));
		
		
		return grm;
	}
}
