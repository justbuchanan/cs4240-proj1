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
			new Token(State.LET), new NonTerminalParserSymbol(NonTerminals.DECLARATION_SEGMENT),
			new Token(State.IN), new NonTerminalParserSymbol(NonTerminals.STAT_SEQ),
			new Token(State.END)
		}));
		
		// DECLARATION_SEGMENT
		grm.addRule(new ProductionRule(NonTerminals.DECLARATION_SEGMENT, new ParserSymbol[] {
			new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION_LIST), new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION_LIST),
			new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION_LIST)
		}));
		
		// TYPE_DECLARATION_LIST
		grm.addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION_LIST, new ParserSymbol[] {
			new Token(State.NULL)	
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION_LIST, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION), new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION_LIST)
		}));
		
		// VAR_DECLARATION_LIST
		grm.addRule(new ProductionRule(NonTerminals.VAR_DECLARATION_LIST, new ParserSymbol[]{
			new Token(State.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.VAR_DECLARATION_LIST, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION), new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION_LIST)
		}));
		
		// FUNCT_DECLARATION_LIST
		grm.addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION_LIST, new ParserSymbol[]{
			new Token(State.NULL)	
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION_LIST, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION), new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION_LIST)	
		}));
		
		// TYPE_DECLARATION
		grm.addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION, new ParserSymbol[]{
			new Token(State.TYPE), new Token(State.ID), 
			new Token(State.EQ), new NonTerminalParserSymbol(NonTerminals.TYPE)
		}));
		
		// TYPE
		grm.addRule(new ProductionRule(NonTerminals.TYPE, new ParserSymbol[]{
				new Token(State.ARRAY), new Token(State.LBRACK), new Token(State.INTLIT),
				new Token(State.RBRACK), new NonTerminalParserSymbol(NonTerminals.TYPEDIM)
		}));
		
		//TYPEDIM
		grm.addRule(new ProductionRule(NonTerminals.TYPEDIM, new ParserSymbol[]{
			new Token(State.RBRACK), new Token(State.INTLIT), new Token(State.LBRACK),
			new NonTerminalParserSymbol(NonTerminals.TYPEDIM)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.TYPEDIM, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		// TYPE_ID
		grm.addRule(new ProductionRule(NonTerminals.TYPE_ID, new ParserSymbol[]{
				new Token(State.INTLIT)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.TYPE_ID, new ParserSymbol[]{
			new Token(State.STRLIT)	
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.TYPE_ID, new ParserSymbol[]{
			new Token(State.ID)
		}));
		
		// VAR_DECLARATION
		grm.addRule(new ProductionRule(NonTerminals.VAR_DECLARATION, new ParserSymbol[]{
				new Token(State.VAR), new NonTerminalParserSymbol(NonTerminals.ID_LIST),
				new Token(State.COLON), new NonTerminalParserSymbol(NonTerminals.TYPE_ID),
				new NonTerminalParserSymbol(NonTerminals.OPTIONAL_INIT), new Token(State.SEMI)
		}));
		
		// ID_LIST
		grm.addRule(new ProductionRule(NonTerminals.ID_LIST, new ParserSymbol[]{
				new Token(State.ID), new NonTerminalParserSymbol(NonTerminals.ID_LIST_PRIME)
		}));
		
		// ID_LIST_PRIME
		grm.addRule(new ProductionRule(NonTerminals.ID_LIST_PRIME, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.ID_LIST_PRIME, new ParserSymbol[]{
				new Token(State.COMMA), new NonTerminalParserSymbol(NonTerminals.ID_LIST)
		}));
		
		// OPTIONAL_INIT
		grm.addRule(new ProductionRule(NonTerminals.OPTIONAL_INIT, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.OPTIONAL_INIT, new ParserSymbol[]{
				new Token(State.ASSIGN), new NonTerminalParserSymbol(NonTerminals.CONST)
		}));
		
		// FUNCT_DECLARATION
		grm.addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION, new ParserSymbol[]{
				new Token(State.FUNC), new Token(State.ID), new Token(State.LPAREN),
				new NonTerminalParserSymbol(NonTerminals.PARAM_LIST), new Token(State.RPAREN), 
				new NonTerminalParserSymbol(NonTerminals.RET_TYPE), new Token(State.BEGIN), 
				new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new Token(State.END), new Token(State.SEMI)
		}));
		
		// PARAM_LIST
		grm.addRule(new ProductionRule(NonTerminals.PARAM_LIST, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.PARAM_LIST, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.PARAM), new NonTerminalParserSymbol(NonTerminals.PARAM_LIST_TAIL)
		}));
		
		// PARAM_LIST_TAIL
		grm.addRule(new ProductionRule(NonTerminals.PARAM_LIST_TAIL, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.PARAM_LIST_TAIL, new ParserSymbol[]{
				new Token(State.COMMA), new NonTerminalParserSymbol(NonTerminals.PARAM),
				new NonTerminalParserSymbol(NonTerminals.PARAM_LIST_TAIL)
		}));
		
		// RET_TYPE
		grm.addRule(new ProductionRule(NonTerminals.RET_TYPE, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.RET_TYPE, new ParserSymbol[]{
				new Token(State.COLON), new NonTerminalParserSymbol(NonTerminals.TYPE_ID)
		}));
		
		// PARAM
		grm.addRule(new ProductionRule(NonTerminals.PARAM, new ParserSymbol[]{
				new Token(State.ID), new Token(State.COLON),
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
				new Token(State.NULL)
		}));
		
		// STAT
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.LVALUE), new Token(State.ASSIGN), new NonTerminalParserSymbol(NonTerminals.EXPR),
				new Token(State.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
				new Token(State.WHILE), new NonTerminalParserSymbol(NonTerminals.EXPR), new Token(State.DO),
				new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new Token(State.ENDDO), new Token(State.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
				new Token(State.FOR), new Token(State.ID), new Token(State.ASSIGN),
				new NonTerminalParserSymbol(NonTerminals.EXPR), new Token(State.TO), new NonTerminalParserSymbol(NonTerminals.EXPR), 
				new Token(State.DO), new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new Token(State.ENDDO), 
				new Token(State.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.OPT_PREFIX), new Token(State.ID), new Token(State.LPAREN),
			new NonTerminalParserSymbol(NonTerminals.EXPR_LIST), new Token(State.RPAREN), new Token(State.SEMI)
		}));

		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new Token(State.BREAK), new Token(State.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new Token(State.RETURN), new NonTerminalParserSymbol(NonTerminals.EXPR), new Token(State.SEMI)
		}));
		
		// STAT_IF
		grm.addRule(new ProductionRule(NonTerminals.STAT_IF, new ParserSymbol[]{
			new Token(State.IF), new NonTerminalParserSymbol(NonTerminals.EXPR), new Token(State.THEN),
			new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new NonTerminalParserSymbol(NonTerminals.STAT_IF_CLAUSE_2)
		}));
		
		// STAT_IF_CLAUSE_2
		grm.addRule(new ProductionRule(NonTerminals.STAT_IF_CLAUSE_2, new ParserSymbol[]{
			new Token(State.ENDIF), new Token(State.SEMI)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.STAT_IF_CLAUSE_2, new ParserSymbol[]{
			new Token(State.ELSE), new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), new Token(State.ENDIF),
			new Token(State.SEMI)
		}));
		
		//OPT_PREFIX
		grm.addRule(new ProductionRule(NonTerminals.OPT_PREFIX, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.LVALUE), new Token(State.ASSIGN)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.OPT_PREFIX, new ParserSymbol[]{
			new Token(State.NULL)
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
			new Token(State.MINUS), new NonTerminalParserSymbol(NonTerminals.ATOM_EXPR)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.NEGATED_EXPR, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.ATOM_EXPR)
		}));
		
		// MULT_DIV_EXPR
		grm.addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.MULT_DIV_EXPR), new Token(State.MULT), 
				new NonTerminalParserSymbol(NonTerminals.NEGATED_EXPR)
		}));
		
		grm.addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.NEGATED_EXPR)
		}));
		
		
		return grm;
	}
}
