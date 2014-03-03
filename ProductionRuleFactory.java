public class ProductionRuleFactory{
	public static ProductionRule getProdRule(NonTerminals ruleName, Terminals next){
		
			//TIGER_PROGRAM
			if(ruleName.ordinal() == NonTerminals.TIGER_PROGRAM.ordinal()){
				if(next.ordinal() == Terminals.LET.ordinal()){ // [TIGER_PROGRAM][LET]
					return new ProductionRule(NonTerminals.TIGER_PROGRAM, 
							new ParserSymbol[]{ new TerminalParserSymbol(Terminals.LET), new NonTerminalParserSymbol(NonTerminals.DECLARATION_SEGMENT), 
								new TerminalParserSymbol(Terminals.IN),  new NonTerminalParserSymbol(NonTerminals.STAT_SEQ), 
								new TerminalParserSymbol(Terminals.END)});
				}
				else return null;
			}
			
			// DECLARATION_SEGMENT
			if(ruleName.ordinal() == NonTerminals.DECLARATION_SEGMENT.ordinal()){
				if(next.ordinal() == Terminals.TYPE.ordinal()){ //[DECLARATION_SEGMENT][TYPE]
					return new ProductionRule(NonTerminals.DECLARATION_SEGMENT, new ParserSymbol[] {new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION_LIST), 
							new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION_LIST), new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION_LIST)});
				}
				else if(next.ordinal() == Terminals.VAR.ordinal()){
					
				}
				else if(next.ordinal() == Terminals.FUNC.ordinal()){
					
				}
				else if(next.ordinal() == Terminals.NULL.ordinal()){
					
				}
				else return null;
			}
				
		return null;
	}
}