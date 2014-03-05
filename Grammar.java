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
		for (ArrayList<ProductionRule> subRules : rules.values()) {
			allRules.addAll(subRules);
		}
		return allRules;
	}

	public void addRule(ProductionRule rule) {
		ArrayList<ProductionRule> arr = rules.get(rule.left());
		if (arr == null) {
			arr = new ArrayList<>();
			rules.put(rule.left(), arr);
		}

		arr.add(rule);
	}

	public Set<Token> findFirstSet(ProductionRule productionRule) {
		Set<Token> set = new HashSet<Token>();

		for(ParserSymbol parserSymbol : productionRule) {
			if(parserSymbol.isTerminal()) {
				set.add((Token) parserSymbol);
				break;
			} else {
				Set<Token> first = findTotalFirstSet((NonTerminalParserSymbol)parserSymbol);

				Token nullSmbl = new Token(State.NULL);
				if (first.contains(nullSmbl)) {
					//	add @first to @set except for null symbol
					first.remove(nullSmbl);
					set.addAll(first);
				} else {
					//	@parserSymbol is not nullable, so we're done
					set.addAll(first);
					break;
				}
			}
		}

		return set;
	}

	public Set<Token> findTotalFirstSet(NonTerminalParserSymbol nonterminal) {
		//	the first set of @nonterminal for ALL rules where it is the left-hand-side
		Set<Token> first = new HashSet<>();

		//	loop through each rule where @nonterminal is the left-hand-side to build @first
		for (ProductionRule rule : rules.get(nonterminal)) {
			Set<Token> firstForRule = findFirstSet(rule);
			first.addAll(firstForRule);
		}

		return first;
	}

	public Set<Token> findFollowSet(NonTerminalParserSymbol nonterminal) {
		Set<Token> followSet = new HashSet<>();

		//	add EOF/$ to follow set for the start symbol
		if (nonterminal.equals(NonTerminals.TIGER_PROGRAM)) {
			followSet.add(new Token(State.$));
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

					if (smbl.equals(nonterminal)) {
						//	we found it!
						seenTheNonterminal = true;
					} else if (seenTheNonterminal) {
						//	get first set of @smbl - handle differently depending on whether it's a terminal or not
						Set<Token> first;
						if (smbl.isTerminal()) {
							first = new HashSet<>();
							first.add((Token)smbl);
						} else {
							first = findTotalFirstSet((NonTerminalParserSymbol)smbl);
						}

						Token nullSmbl = new Token(State.NULL);
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

		return followSet;
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
