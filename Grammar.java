
public class Grammar {
	private Map<NonTerminalParserSymbol, ArrayList<ParserSymbol> > rules;


	Grammar() {
		rules = new Map<>();
	}

	public void addRule(ProductionRule rule) {

	}

	public ArrayList<TerminalParserSymbol> findFirstSet(NonTerminalParserSymbol symbol) {

	}

	public ArrayList<TerminalParserSymbol> findFollowSet(NonTerminalParserSymbol symbol) {

	}
}
