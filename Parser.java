import java.util.LinkedList;
import java.util.Stack;
import java.util.Set;
import java.util.ArrayList;

public class Parser{
	
	private Scanner scanner;
	private ProductionRule[][] parserTable;
	private Grammar grammar;
	private int NUM_NONTERMINALS = 47;
	private int NUM_TERMINALS = 49;


	public Parser(Scanner scanner, Grammar grammar){
		this.scanner = scanner;
		this.grammar = grammar;
		buildParserTable();
	}

	public boolean parseText() {
		boolean debug = true;
		if (debug) 		System.out.println("-------------------------------STARTING PARSE-------------------------------");

		Stack<ParserSymbol> symbolStack = new Stack<>();
		symbolStack.push(new Token(State.$));
		symbolStack.push(new NonTerminalParserSymbol(NonTerminals.TIGER_PROGRAM));

		while(!symbolStack.isEmpty()){
			Token token = scanner.peekToken();
			
			//	when the scanner returns null, it means we're at the end of the file
			if (token == null) {
				token = new Token(State.$);
			}
			// invalid 
			if(token.ordinal() == State.ERROR.ordinal()){
				System.out.println("Invalid token on : line " + token.lineNumber);
				System.out.println("unsuccessful parse");
				return false;
			}

			//	eat comments
			if (token.ordinal() == State.COMMENT.ordinal()) {
				if (debug) System.out.println("Ate a comment\n");
				scanner.nextToken();
				continue;
			}

			if (debug) System.out.println("Peeked token: " + token + ":" + token.value());

			ParserSymbol parserSymbol = symbolStack.pop();

			if (debug) System.out.println("< Popped parser symbol: " + parserSymbol);

			if(parserSymbol instanceof Token) {
				if(token.equals(parserSymbol)) {
					scanner.nextToken();	//	eat the token we just peeked
					if (debug) System.out.println("Matched the token!\n");
					continue;
				} else {
					System.out.println("ERROR: Found " + token + ", expecting " + parserSymbol);
					return false;
				}
			} else {
				ProductionRule productionRule = parserTable[((NonTerminalParserSymbol)parserSymbol).getNonTerminal().ordinal()][token.ordinal()];
				
				if (productionRule == null) {
					System.out.println("ERROR: Trying to match '" + parserSymbol + "', but found: '" + token + "'");
					return false;
				}


				ParserSymbol rightSymbol = productionRule.right()[0];
				if (productionRule.right().length == 1 &&
					rightSymbol.isTerminal() &&
					((Token)rightSymbol).ordinal() == State.NULL.ordinal()
					) {

				} else {
					for(int i = productionRule.right().length - 1; i >= 0; i--) {
						System.out.println(">> Parser Push: " + productionRule.right()[i]);
						symbolStack.push(productionRule.right()[i]);
					}
				}
				
			}
		}
		

		if (scanner.nextToken() != null) {
			System.out.println("\nERROR: extra tokens left after parser finished");
			return false;
		}

		System.out.println("\nSuccessful parse!!!");

		return true;
	}

	private void buildParserTable(){
		parserTable = new ProductionRule[NUM_NONTERMINALS][NUM_TERMINALS];

		Token nullSymbol = new Token(State.NULL);

		//	add entries based on first && follow sets
		for (ProductionRule rule : grammar.allRules()) {
			Set<Token> firstSet = grammar.findFirstSet(rule);
			for (Token terminal : firstSet) {
				if (terminal.ordinal() != State.NULL.ordinal()) {
					if (parserTable[rule.left().ordinal()][terminal.ordinal()] != null) {
						throw new RuntimeException("ERROR: Rule collision at column " + terminal + ", grammar is not LL(1): " + parserTable[rule.left().ordinal()][terminal.ordinal()] + " ******* " + rule);
					}

					parserTable[rule.left().ordinal()][terminal.ordinal()] = rule;
				}
			}

			if (firstSet.contains(nullSymbol)) {
				NonTerminalParserSymbol nonterminal = rule.left();
				Set<Token> followSet = grammar.findFollowSet(nonterminal);

				for (Token terminal : followSet) {
					ProductionRule newNullRule = new ProductionRule(nonterminal.getNonTerminal(), new ParserSymbol[] {nullSymbol});
					if (parserTable[nonterminal.ordinal()][terminal.ordinal()] != null) {
						throw new RuntimeException("ERROR: Rule collision at column " + terminal + ", grammar is not LL(1): " + parserTable[nonterminal.ordinal()][terminal.ordinal()] + " ******* " + newNullRule);
					}
					parserTable[nonterminal.ordinal()][terminal.ordinal()] = newNullRule;
				}
			}
		}
	}

	public String generateParseTableCSV() {
		String csv = new String();

		csv += ",";	//	leave upper-left corner blank
		for (int termIdx = 0; termIdx < NUM_TERMINALS; termIdx++) {
			csv += State.values()[termIdx] + ",";
		}

		for (int nontermIdx = 0; nontermIdx < NUM_NONTERMINALS; nontermIdx++) {
			csv += NonTerminals.values()[nontermIdx] + ",";

			for (int termIdx = 0; termIdx < NUM_TERMINALS; termIdx++) {
				ProductionRule rule = parserTable[nontermIdx][termIdx];

				if (rule != null) {
					csv += rule.left().getNonTerminal() + " --> ";
					for (ParserSymbol rightSmbl : rule.right()) {
						if (rightSmbl.isTerminal()) {
							csv += ((Token)rightSmbl).type() + " ";
						} else {
							csv += ((NonTerminalParserSymbol)rightSmbl).getNonTerminal() + " ";
						}
					}
				}			

				csv += ",";
			}

			csv += "\n";
		}
		
		return csv;
	}

	public String prettyPrintedFirstSets() {
		String output = "";

		//	add entries based on first && follow sets
		for (ArrayList<ProductionRule> rules : grammar.rulesByNonTerminal()) {
			for (ProductionRule rule : rules) {
				output += "FIRST(" + rule + ") = " + grammar.findFirstSet(rule) + "\n";
			}
			output += "\n";	//	newline between sets of rules
		}

		return output;
	}

	public String prettyPrintedFollowSets() {
		String output = "";

		Token nullSymbol = new Token(State.NULL);

		for (NonTerminalParserSymbol nonterminal : grammar.allNonTerminalSymbols()) {
			Set<Token> followSet = grammar.findFollowSet(nonterminal);

			output += "FOLLOW(" + nonterminal + ") = " + followSet + "\n";
		}

		return output;
	}
}
