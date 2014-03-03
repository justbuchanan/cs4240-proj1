import java.util.LinkedList;

public class Parser{
	
	private Scanner scanner;
	private LinkedList<Integer> symbolStack;
	private ProductionRule[][] parserTable;

	public Parser(Scanner scanner){
		this.scanner = scanner;
		symbolStack = new LinkedList();
	}

	public void parseText(){
		buildParserTable();
				//	print out all tokens
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
