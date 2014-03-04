
public class TerminalParserSymbol extends ParserSymbol{
	private Terminals terminal;
	
	public TerminalParserSymbol(Terminals terminal){
		this.terminal = terminal;
	}
	
	public Terminals getTerminal(){
		return this.terminal;
	}

	public boolean isTerminal(){
		return true;
	}
}
