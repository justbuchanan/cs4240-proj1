import java.util.LinkedList;

public class Parser{
	
	private Scanner scanner;
	private LinkedList<ParserSymbol> symbolStack;
	private ProductionRule[][] parserTable;

	public Parser(Scanner scanner){
		this.scanner = scanner;
		symbolStack = new LinkedList();
	}

	public void parseText(){
		buildParserTable();
				//	print out all tokens
		
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

	// TODO: SHOULD parserTable[n][0] be COMMA (our first accept state) FROM STATES TABLE
	private void buildParserTable(){
		for(int nonTerminalIndex = 0; nonTerminalIndex < parserTable.length; nonTerminalIndex++){
			for(int terminalIndex = 0; terminalIndex < parserTable[0].length; terminalIndex++){
				parserTable[nonTerminalIndex][terminalIndex] = ProductionRuleFactory.getProdRule(NonTerminals.values()[nonTerminalIndex],
						Terminals.values()[terminalIndex]);
			}
		}
	}


	

}
