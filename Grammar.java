import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


public class Grammar {
	private HashMap<NonTerminalParserSymbol, ArrayList<ProductionRule> > rules;


	Grammar() {
		rules = new HashMap<>();
	}

	ArrayList<ProductionRule> allRules() {
		ArrayList<ProductionRule> allRules = new ArrayList<>();
		for (ArrayList<ProductionRule> subRules : rules) {
			allRules.addAll(subRules);
		}
		return allRules;
	}

	public void addRule(ProductionRule rule) {
		ArrayList<ProductionRule> arr = rules.get(rule.left());
		if (arr == null) {
			arr = new ArrayList<>();
		}

		arr.add(rule);
	}

	public Set<TerminalParserSymbol> findFirstSet(ProductionRule productionRule) {
		Set<TerminalParserSymbol> set = new HashSet<TerminalParserSymbol>();

		if(productionRule.right()[0].isTerminal() && ((TerminalParserSymbol)productionRule.right()[0]).getTerminal() == Terminals.NULL) {
			set.add((TerminalParserSymbol) productionRule.right()[0]);
			return set;
		}

		for(ParserSymbol parserSymbol : productionRule) {
			if(parserSymbol.isTerminal() && ((TerminalParserSymbol)parserSymbol).getTerminal() != Terminals.NULL) {
				set.add((TerminalParserSymbol) parserSymbol);
				break;
			} else {
				TerminalParserSymbol terminalParserSymbol = findFirstSetHelper((NonTerminalParserSymbol) parserSymbol, set);
				if(terminalParserSymbol != null) {
					break;
				}
			}
		}

		return set;
	}

	public TerminalParserSymbol findFirstSetHelper(NonTerminalParserSymbol symbol, Set<TerminalParserSymbol> set) {
		for(ProductionRule productionRule : rules.get(symbol)) {
 			for(ParserSymbol parserSymbol : productionRule) {
 				if(parserSymbol.isTerminal() && ((TerminalParserSymbol)parserSymbol).getTerminal() != Terminals.NULL) {
 					set.add((TerminalParserSymbol) parserSymbol);
 					return (TerminalParserSymbol) parserSymbol;
 				} else if(parserSymbol.isTerminal() && ((TerminalParserSymbol)parserSymbol).getTerminal() == Terminals.NULL) {
 					continue;
 				} else {
 					TerminalParserSymbol terminalParserSymbol = findFirstSetHelper((NonTerminalParserSymbol) parserSymbol, set);
 					if(terminalParserSymbol != null) {
 						return terminalParserSymbol;
 					}
 				}
 			}
 		}

		return null;
	}

	public ArrayList<TerminalParserSymbol> findFollowSet(NonTerminalParserSymbol nonterminal) {
		Set<TerminalParserSymbol> followSet = new HashSet<>();

		//	add EOF/$ to follow set for the start symbol
		if (nonterminal.isEqual(NonTerminals.TIGER_PROGRAM)) {
			followSet.add(new NonTerminalParserSymbol(Terminals.$));
		}

		//	loop over EVERY rule looking for places where @nonterminal appears on the right side
		for (ArrayList<ProductionRule> ruleArray : rules.values()) {
			for (ProductionRule rule : ruleArray) {
				ParserSymbol[] right = rule.right();

				//	we iterate over rule.right and set this if/when we see the nonterminal we're looking for
				//	that way, subsequent iterations can behave appropriately
				boolean seenTheNonterminal = false;

				//	if we get to the end and haven't terminated yet, we have to add FOLLOW(@rule.left) to @followSet
				boolean terminated = false;

				for (int i = 0; i < right.length; i++) {
					ParserSymbol smbl = right[i];

					if (smbl.isEqual(nonterminal)) {
						//	we found it!
						seenTheNonterminal = true;
					} else if (seenTheNonterminal) {
						//	get first set of @smbl - handle differently depending on whether it's a terminal or not
						Set<TerminalParserSymbol> first;
						if (smbl.isTerminal()) {
							first = new HashSet<>();
							first.add(smbl);
						} else {
							first = findFirstSet((NonTerminalParserSymbol)smbl);
						}

						TerminalParserSymbol nullSmbl = new TerminalParserSymbol(Terminals.NULL);
						if (first.contains(nullSmbl)) {
							//	if it contains a null symbol, remove the null, and add it to the follow set
							first.remove(nullSmbl);
							followSet.addAll(first);
						} else {
							followSet.addAll(first);
							terminated = true;
							break;
						}
					}
				}	//	end for loop

				if (seenTheNonterminal && !terminated) {
					//	@nonterminal is the last symbol in this rule OR all symbols following @nonterminal are nullable
					followSet.addAll( findFollowSet(rule.left()) );
				}
			}
		}
	}


	public String toString() {
		String desc = new String("Grammar:\n");
		for (ArrayList<ProductionRule> ruleArray : rules.values()) {
			for (ProductionRule rule : ruleArray) {
				desc += "\t" + rule + "\n";
			}
		}

		return desc;
	}

}
