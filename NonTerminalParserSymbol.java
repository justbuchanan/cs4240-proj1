
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
		// return new String("NonTerminal(" + nonTerminal + ")");
		return "<" + nonTerminal.toString() + ">";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || this.getClass() != obj.getClass()) return false;

		NonTerminalParserSymbol other = (NonTerminalParserSymbol)obj;
		return other.nonTerminal.equals(nonTerminal);
	}

	@Override
	public int hashCode() {
		return nonTerminal.hashCode();
	}
}
