import java.util.LinkedList;
import java.util.Stack;

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
		symbolStack.push(new TerminalParserSymbol(Terminals.$));
		symbolStack.push(new NonTerminalParserSymbol(NonTerminals.TIGER_PROGRAM));

		while(!symbolStack.isEmpty()){
			TerminalParserSymbol token = scanner.peek();
			ParserSymbol parserSymbol = symbolStack.pop();

			if(parserSymbol instanceof TerminalParserSymbol) {
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

		TerminalParserSymbol nullSymbol = new TerminalParserSymbol(Terminals.NULL);

		//	add entries based on first && follow sets
		for (ProductionRule rule : grammar.allRules()) {
			Set<TerminalParserSymbol> firstSet = grammar.findFirstSet(rule);
			for (TerminalParserSymbol terminal : firstSet) {
				parserTable[rule.left().ordinal()][terminal.ordinal()] = rule;
			}

			if (firstSet.contains(nullSymbol)) {
				NonTerminalParserSymbol nonterminal = rule.left();
				Set<TerminalParserSymbol> followSet = grammar.findFollowSet(nonterminal);

				for (TerminalParserSymbol terminal : followSet) {
					parserTable[nonterminal.ordinal()][terminal.ordinal()] = nullSymbol;
				}
			}
		}
	}

}
