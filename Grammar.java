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

	public ArrayList<TerminalParserSymbol> findFollowSet(NonTerminalParserSymbol nonterminal) {
		Set<TerminalParserSymbol> followSet = new HashSet<>();

		//	add EOF/$ to follow set for the start symbol
		if (nonterminal.isEqual(NonTerminals.TIGER_PROGRAM)) {
			followSet.add(new NonTerminalParserSymbol(NonTerminals.$));
		}

		//	loop over EVERY rule looking for places where @nonterminal appears on the right side
		for (ArrayList<ProductionRule> ruleArray : rules.values()) {
			for (ProductionRule rule : ruleArray) {
				ParserSymbol[] right = rule.right();

				//	we iterate over rule.right and set this if/when we see the nonterminal we're looking for
				//	that way, subsequent iterations can behave appropriately
				boolean seenTheNonterminal = false;

				for (int i = 0; i < right.length) {
					ParserSymbol smbl = right[i];

					if (smbl.isEqual(nonterminal)) {

						//	see if there's another symbol after the current one
						//	if so, add it's FIRST set except for NULL, to @followSet
						if (i + 1 < right.length) {
							Set<TerminalParserSymbol> first = findFirstSet(right[i+1]);
							first.remove(new TerminalParserSymbol(Terminals.NULL));
							followSet.add(first);
						} else {
							//	@nonterminal is the last symbol in this rule
							//	add FOLLOW(@rule.left) to @followSet
							followSet.add( findFollowSet(rule.left) );
						}

						seenTheNonterminal = true;
					} else if (seenTheNonterminal) {
						Set<TerminalParserSymbol> first = findFirstSet(right[i]);
						TerminalParserSymbol nullSmbl = new TerminalParserSymbol(Terminals.NULL);
						if (first.contains(nullSmbl) {
							//	if it contains a null symbol, remove the null, and add it to the follow set
							first.remove(nullSmbl);
							followSet.add(first);

							//	if this is the last symbol and it's nullable, add FOLLOW(rule.left) to @followSet
							if (i + 1 == right.length) {
								followSet.add( findFollowSet(rule.left) );
							}
						} else {
							followSet.add(first);
							break;
						}

					}
				}

			}
		}
	}

}
