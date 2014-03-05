import java.util.LinkedList;

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

	public void parseText(){
		Stack<ParserSymbol> symbolStack = new Stack<>();
		symbolStack.push(new Token(State.$));
		symbolStack.push(new NonTerminalParserSymbol(NonTerminals.TIGER_PROGRAM));

		while(!symbolStack.isEmpty()){
			Token token = scanner.peek();
			ParserSymbol parserSymbol = symbolStack.pop();

			if(parserSymbol instanceof Token) {
				if(token == parserSymbol) {
					scanner.nextToken();
					continue;
				} else {
					//ERROR STATE!
					continue;
				}
			} else {
				ProductionRule productionRule = parserTable[parserSymbol.getNonTerminal().ordinal()][token.ordinal()];
				for(int i = parserSymbol.length - 1; i >= 0; i--) {
					symbolStack.push(parserSymbol[i]);
				}
			}
		}
		
		//If next token is not NULL, then ERROR (if there are any leftover tokens)
		while (true) {
			Token token = scanner.nextToken();
			if (token == null) {
				break;
			}
			System.out.println(">> " + token.type + " : '" + token.value + "' (" +  token.lineNumber + ")");
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
				Set<Token> followSet = grammar.findFollowSet(rule);

				for (Token terminal : followSet) {
					parserTable[nonterminal.ordinal()][terminal.ordinal()] = nullSymbol;
				}
			}
		}
	}

}
