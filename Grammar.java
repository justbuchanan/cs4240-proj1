import java.util.ArrayList;
import java.util.HashMap;


public class Grammar {
	private HashMap<NonTerminalParserSymbol, ArrayList<ProductionRule> > rules;


	Grammar() {
		rules = new HashMap<>();
	}

	public void addRule(ProductionRule rule) {
		ArrayList<ProductionRule> arr = rules.get(rule.left());
		if (arr == null) {
			arr = new ArrayList<>();
		}

		arr.add(rule);
	}

	public ArrayList<TerminalParserSymbol> findFirstSet(NonTerminalParserSymbol symbol) {

	}

	public ArrayList<TerminalParserSymbol> findFollowSet(NonTerminalParserSymbol symbol) {

	}
}
