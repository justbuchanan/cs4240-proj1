import java.util.LinkedList;

public class Parser{
	
	private Scanner scanner;
	private ProductionRule[][] parserTable;
	private Grammar grammar;

	public Parser(Scanner scanner, Grammar grammar){
		this.scanner = scanner;
		symbolStack = new LinkedList();
		this.grammar = grammar;
		buildParserTable();
	}

	public void parseText(){
		Stack<ParserSymbol> symbolStack = new Stack<>();
		symbolStack.push(new TerminalParserSymbol(Terminals.$));
		symbolStack.push(new NonTerminalParserSymbol(NonTerminals.TIGER_PROGRAM));

		while(!symbolStack.isEmpty()){
			ParserSymbol next = symbolStack.pop();
			if(next instanceof TerminalParserSymbol){
				if(scanner.nextToken().equals(((TerminalParserSymbol)next).getTerminal())){
					continue;
				}
				else{
					// HANDLE ERROR HERE!
				}
			}
			else{
				NonTerminalParserSymbol symbol = (NonTerminalParserSymbol)next;
				ProductionRule nextRule = parserTable[symbol.getNonTerminal().ordinal()][scanner.nextToken().ordinal()];
				if(nextRule == null){
					// HANDLE ERROR HERE
				}
				else{
					while(nextRule.hasNext()){
						symbolStack.push(nextRule.next());
					}
				}
			}
		}
		
		
		while (true) {
			Token token = scanner.nextToken();
			if (token == null) break;
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
