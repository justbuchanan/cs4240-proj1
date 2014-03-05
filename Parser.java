import java.util.LinkedList;
import java.util.Stack;
import java.util.Set;

public class Parser{
	
	private Scanner scanner;
	private ProductionRule[][] parserTable;
	private Grammar grammar;
	private int NUM_NONTERMINALS = 35;
	private int NUM_TERMINALS = 49;


	public Parser(Scanner scanner, Grammar grammar){
		this.scanner = scanner;
		this.grammar = grammar;
		buildParserTable();
	}

	public void parseText() {
		Stack<ParserSymbol> symbolStack = new Stack<>();
		symbolStack.push(new Token(State.$));
		symbolStack.push(new NonTerminalParserSymbol(NonTerminals.TIGER_PROGRAM));

		while(!symbolStack.isEmpty()){
			Token token = scanner.peekToken();

			//	when the scanner returns null, it means we're at the end of the file
			if (token == null) {
				token = new Token(State.$);
			}

			ParserSymbol parserSymbol = symbolStack.pop();

			if(parserSymbol instanceof Token) {
				if(token.equals(parserSymbol)) {
					scanner.nextToken();	//	eat the token we just peeked
					continue;
				} else {
					System.out.println("ERROR: Found " + token + ", expecting " + parserSymbol);
					return;
				}
			} else {
				ProductionRule productionRule = parserTable[((NonTerminalParserSymbol)parserSymbol).getNonTerminal().ordinal()][token.ordinal()];
				
				if (productionRule == null) {
					System.out.println("ERROR: Trying to match '" + parserSymbol + "', but found: '" + token + "'");
					return;
				}

				for(int i = productionRule.right().length - 1; i >= 0; i--) {
					symbolStack.push(productionRule.right()[i]);
				}
			}
		}
		
		//If next token is not NULL, then ERROR (if there are any leftover tokens)
		while (true) {
			Token token = scanner.nextToken();
			if (token == null) {
				break;
			}
			System.out.println(">> Extra token: " + token.type + " : '" + token.value + "' (" +  token.lineNumber + ")");
		}
	}

	private void buildParserTable(){
		parserTable = new ProductionRule[NUM_NONTERMINALS][NUM_TERMINALS];

		Token nullSymbol = new Token(State.NULL);

		//	add entries based on first && follow sets
		for (ProductionRule rule : grammar.allRules()) {
			Set<Token> firstSet = grammar.findFirstSet(rule);
			for (Token terminal : firstSet) {
				parserTable[rule.left().ordinal()][terminal.ordinal()] = rule;
			}

			if (firstSet.contains(nullSymbol)) {
				NonTerminalParserSymbol nonterminal = rule.left();
				Set<Token> followSet = grammar.findFollowSet(nonterminal);

				for (Token terminal : followSet) {
					parserTable[nonterminal.ordinal()][terminal.ordinal()] = new ProductionRule(nonterminal.getNonTerminal(), new ParserSymbol[] {nullSymbol});
				}
			}
		}
	}

}
