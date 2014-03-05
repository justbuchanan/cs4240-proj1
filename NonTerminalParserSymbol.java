
public class NonTerminalParserSymbol extends ParserSymbol{
	private NonTerminals nonTerminal;
	
	public NonTerminalParserSymbol(NonTerminals nonTerminal){
		this.nonTerminal = nonTerminal;
	}
	
	public NonTerminals getNonTerminal(){
		return this.nonTerminal;
	}

	public boolean isTerminal() {
		return false;
	}

	public int ordinal() {
		return nonTerminal.ordinal();
	}

	public String toString() {
		return new String("NonTerminal(" + nonTerminal + ")");
	}
}
