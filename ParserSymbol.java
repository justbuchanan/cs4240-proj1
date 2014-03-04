
/**
 * Abstract superclass for NonTerminalParserSymbol and TerminalParserSymbol
 */
public abstract class ParserSymbol {
	public boolean isTerminal() {
		throw new RuntimeException("Subclasses must implement isTerminal()");
	}
}
