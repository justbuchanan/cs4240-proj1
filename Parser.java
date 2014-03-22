import java.util.LinkedList;
import java.util.Stack;
import java.util.Set;
import java.util.ArrayList;

public class Parser{
	
	private Scanner scanner;
	private ProductionRule[][] parserTable;
	private Grammar grammar;
	private int NUM_NONTERMINALS = 49;
	private int NUM_TERMINALS = 49;
	private ParseTree parseTree;

	public Parser(Scanner scanner, Grammar grammar){
		this.scanner = scanner;
		this.grammar = grammar;
		parseTree = new ParseTree();
		buildParserTable();
	}

	public boolean parseText() {
		boolean debug = true;
		if (debug) System.out.println("-------------------------------STARTING PARSE-------------------------------");

		Stack<ParserSymbol> symbolStack = new Stack<>();
		symbolStack.push(new Token(State.$));
		symbolStack.push(new NonTerminalParserSymbol(NonTerminals.TIGER_PROGRAM));

		while (true) {
			Token token = scanner.peekToken();
			
			//	when the scanner returns null, it means we're at the end of the file
			if (token == null) {
				token = new Token(State.$);
			}
			// invalid 
			if(token.ordinal() == State.ERROR.ordinal()){
				System.out.println("Invalid token on line: " + token.lineNumber);
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

			//	we hit a null, which is our marker in the symbol stack that we've matched all of the right-hand-side of some non-terminal
			//	this is our signal to pop the current level on the parse tree
			if (parserSymbol == null) {
				if (debug) System.out.println("Popped parse tree level");
				parseTree.popLevel();
				continue;
			}

			if(parserSymbol instanceof Token) {
				if(token.equals(parserSymbol)) {
					parseTree.add(token);	//	add it to the parse tree
					scanner.nextToken();	//	eat the token we just peeked and 
					if (debug) System.out.println("Matched the token!\n");

					if (token.type() == State.$) {
						break;	//	we completed the parse!
					} else {
						continue;
					}
				} else {
					System.out.println("ERROR: Found " + token + ", expecting " + parserSymbol);
					return false;
				}
			} else {
				ProductionRule productionRule = parserTable[((NonTerminalParserSymbol)parserSymbol).getNonTerminal().ordinal()][token.ordinal()];
				
				if (productionRule == null) {
					System.out.println("ERROR: Trying to match '" + parserSymbol + "', but found: '" + token + "' on line: " + token.lineNumber);
					return false;
				}

				ParserSymbol rightSymbol = productionRule.right()[0];
				if (productionRule.right().length == 1 &&
					rightSymbol.isTerminal() &&
					((Token)rightSymbol).ordinal() == State.NULL.ordinal()
					) {

				} else {
					parseTree.pushLevel(productionRule.left());

					symbolStack.push(null);	//	push null onto the stack so we know when to pop this level on the parse tree

					for(int i = productionRule.right().length - 1; i >= 0; i--) {
						if (debug) System.out.println(">> Parser Push: " + productionRule.right()[i]);
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
					ProductionRule newNullRule = new ProductionRule(nonterminal.getNonTerminal(), State.NULL);
					ProductionRule existingRule = parserTable[nonterminal.ordinal()][terminal.ordinal()];
					if (existingRule != null && !existingRule.equals(newNullRule)) {
						System.out.println(existingRule + " != " + newNullRule);
						throw new RuntimeException("ERROR: Rule collision at column " + terminal + ", grammar is not LL(1): " + existingRule + " ******* " + newNullRule);
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

		csv += "\n";

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

	public void printTree() {
		System.out.println("Number of nodes in parse tree: " + parseTree.getSize());
		System.out.println(parseTree.toString());
	}
}
