
public class TigerGrammar extends Grammar {
	TigerGrammar() {
		addRule(new ProductionRule(NonTerminals.LEXP, new ParserSymbol[] {
			new NonTerminalParserSymbol(NonTerminals.ATOM)
		}));

		addRule(new ProductionRule(NonTerminals.LEXP, new ParserSymbol[] {
			new NonTerminalParserSymbol(NonTerminals.LIST)
		}));

		addRule(new ProductionRule(NonTerminals.ATOM, new ParserSymbol[] {
			new Token(State.NUMBER)
		}));

		addRule(new ProductionRule(NonTerminals.ATOM, new ParserSymbol[] {
			new Token(State.ID)
		}));

		addRule(new ProductionRule(NonTerminals.LIST, new ParserSymbol[] {
			new Token(State.LPAREN),
			new NonTerminalParserSymbol(NonTerminals.LEXP_SEQ),
			new Token(State.RPAREN)
		}));

		addRule(new ProductionRule(NonTerminals.LEXP_SEQ, new ParserSymbol[] {
			new NonTerminalParserSymbol(NonTerminals.LEXP),
			new NonTerminalParserSymbol(NonTerminals.LEXP_SEQ_TAIL)
		}));

		addRule(new ProductionRule(NonTerminals.LEXP_SEQ_TAIL, new ParserSymbol[] {
			new NonTerminalParserSymbol(NonTerminals.LEXP),
			new Token(State.COMMA),
			new NonTerminalParserSymbol(NonTerminals.LEXP_SEQ_TAIL)
		}));

		addRule(new ProductionRule(NonTerminals.LEXP_SEQ_TAIL, new ParserSymbol[] {
			new Token(State.NULL)
		}));

	}
}
