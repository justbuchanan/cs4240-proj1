
public class TigerGrammar extends Grammar {
	TigerGrammar() {
		// TIGER_PROGRAM
		addRule(new ProductionRule(NonTerminals.TIGER_PROGRAM, new ParserSymbol[] {
			new Token(State.LET),
			new NonTerminalParserSymbol(NonTerminals.DECLARATION_SEGMENT),
			new Token(State.IN),
			new NonTerminalParserSymbol(NonTerminals.STAT_SEQ),
			new Token(State.END)
		}));
		
		// DECLARATION_SEGMENT
		addRule(new ProductionRule(NonTerminals.DECLARATION_SEGMENT, new ParserSymbol[] {
			new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION_LIST), new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION_LIST),
			new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION_LIST)
		}));
		
		// TYPE_DECLARATION_LIST
		addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION_LIST, new ParserSymbol[] {
			new Token(State.NULL)	
		}));
		
		addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION_LIST, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION),
				new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION_LIST)
		}));
		
		// VAR_DECLARATION_LIST
		addRule(new ProductionRule(NonTerminals.VAR_DECLARATION_LIST, new ParserSymbol[]{
			new Token(State.NULL)
		}));
		
		addRule(new ProductionRule(NonTerminals.VAR_DECLARATION_LIST, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION),
				new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION_LIST)
		}));
		
		// FUNCT_DECLARATION_LIST
		addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION_LIST, new ParserSymbol[]{
			new Token(State.NULL)	
		}));
		
		addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION_LIST, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION),
			new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION_LIST)	
		}));
		
		// TYPE_DECLARATION
		addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION, new ParserSymbol[]{
			new Token(State.TYPE),
			new Token(State.ID), 
			new Token(State.EQ),
			new NonTerminalParserSymbol(NonTerminals.TYPE)
		}));
		
		// TYPE
		addRule(new ProductionRule(NonTerminals.TYPE, new ParserSymbol[]{
				new Token(State.ARRAY),
				new Token(State.LBRACK),
				new Token(State.INTLIT),
				new Token(State.RBRACK),
				new NonTerminalParserSymbol(NonTerminals.TYPEDIM),
				new Token(State.OF),
				new NonTerminalParserSymbol(NonTerminals.TYPE_ID),
				new Token(State.SEMI)
		}));
		
		//TYPEDIM
		addRule(new ProductionRule(NonTerminals.TYPEDIM, new ParserSymbol[]{
			new Token(State.LBRACK),
			new Token(State.INTLIT),
			new Token(State.RBRACK),
			new NonTerminalParserSymbol(NonTerminals.TYPEDIM)
		}));
		
		addRule(new ProductionRule(NonTerminals.TYPEDIM, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		// TYPE_ID
		addRule(new ProductionRule(NonTerminals.TYPE_ID, new ParserSymbol[]{
				new Token(State.INTLIT)
		}));
		
		addRule(new ProductionRule(NonTerminals.TYPE_ID, new ParserSymbol[]{
			new Token(State.STRLIT)	
		}));
		
		addRule(new ProductionRule(NonTerminals.TYPE_ID, new ParserSymbol[]{
			new Token(State.ID)
		}));
		
		// VAR_DECLARATION
		addRule(new ProductionRule(NonTerminals.VAR_DECLARATION, new ParserSymbol[]{
				new Token(State.VAR),
				new NonTerminalParserSymbol(NonTerminals.ID_LIST),
				new Token(State.COLON),
				new NonTerminalParserSymbol(NonTerminals.TYPE_ID),
				new NonTerminalParserSymbol(NonTerminals.OPTIONAL_INIT),
				new Token(State.SEMI)
		}));
		
		// ID_LIST
		addRule(new ProductionRule(NonTerminals.ID_LIST, new ParserSymbol[]{
				new Token(State.ID), new NonTerminalParserSymbol(NonTerminals.ID_LIST_PRIME)
		}));
		
		// ID_LIST_PRIME
		addRule(new ProductionRule(NonTerminals.ID_LIST_PRIME, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		addRule(new ProductionRule(NonTerminals.ID_LIST_PRIME, new ParserSymbol[]{
				new Token(State.COMMA), new NonTerminalParserSymbol(NonTerminals.ID_LIST)
		}));
		
		// OPTIONAL_INIT
		addRule(new ProductionRule(NonTerminals.OPTIONAL_INIT, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		addRule(new ProductionRule(NonTerminals.OPTIONAL_INIT, new ParserSymbol[]{
				new Token(State.ASSIGN),
				new NonTerminalParserSymbol(NonTerminals.CONST)
		}));
		
		// FUNCT_DECLARATION
		addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION, new ParserSymbol[]{
				new Token(State.FUNC),
				new Token(State.ID),
				new Token(State.LPAREN),
				new NonTerminalParserSymbol(NonTerminals.PARAM_LIST),
				new Token(State.RPAREN), 
				new NonTerminalParserSymbol(NonTerminals.RET_TYPE),
				new Token(State.BEGIN), 
				new NonTerminalParserSymbol(NonTerminals.STAT_SEQ),
				new Token(State.END), new Token(State.SEMI)
		}));
		
		// PARAM_LIST
		addRule(new ProductionRule(NonTerminals.PARAM_LIST, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		addRule(new ProductionRule(NonTerminals.PARAM_LIST, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.PARAM), new NonTerminalParserSymbol(NonTerminals.PARAM_LIST_TAIL)
		}));
		
		// PARAM_LIST_TAIL
		addRule(new ProductionRule(NonTerminals.PARAM_LIST_TAIL, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		addRule(new ProductionRule(NonTerminals.PARAM_LIST_TAIL, new ParserSymbol[]{
				new Token(State.COMMA),
				new NonTerminalParserSymbol(NonTerminals.PARAM),
				new NonTerminalParserSymbol(NonTerminals.PARAM_LIST_TAIL)
		}));
		
		// RET_TYPE
		addRule(new ProductionRule(NonTerminals.RET_TYPE, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		addRule(new ProductionRule(NonTerminals.RET_TYPE, new ParserSymbol[]{
				new Token(State.COLON),
				new NonTerminalParserSymbol(NonTerminals.TYPE_ID)
		}));
		
		// PARAM
		addRule(new ProductionRule(NonTerminals.PARAM, new ParserSymbol[]{
				new Token(State.ID), new Token(State.COLON),
				new NonTerminalParserSymbol(NonTerminals.TYPE_ID)
		}));
		
		// STAT_SEQ
		addRule(new ProductionRule(NonTerminals.STAT_SEQ, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.STAT),
				new NonTerminalParserSymbol(NonTerminals.STAT_SEQ_PRIME)
		}));
		
		// STAT_SEQ_PRIME
		addRule(new ProductionRule(NonTerminals.STAT_SEQ_PRIME, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.STAT),
			new NonTerminalParserSymbol(NonTerminals.STAT_SEQ_PRIME)
		}));
		
		addRule(new ProductionRule(NonTerminals.STAT_SEQ_PRIME, new ParserSymbol[]{
			new Token(State.NULL)
		}));
		
		// STAT

		addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new Token(State.WHILE),
			new NonTerminalParserSymbol(NonTerminals.EXPR),
			new Token(State.DO),
			new NonTerminalParserSymbol(NonTerminals.STAT_SEQ),
			new Token(State.ENDDO),
			new Token(State.SEMI)
		}));
		
		addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new Token(State.FOR),
			new Token(State.ID),
			new Token(State.ASSIGN),
			new NonTerminalParserSymbol(NonTerminals.EXPR),
			new Token(State.TO),
			new NonTerminalParserSymbol(NonTerminals.EXPR), 
			new Token(State.DO),
			new NonTerminalParserSymbol(NonTerminals.STAT_SEQ),
			new Token(State.ENDDO), 
			new Token(State.SEMI)
		}));

		addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new Token(State.BREAK),
			new Token(State.SEMI)
		}));
		
		addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new Token(State.RETURN),
			new NonTerminalParserSymbol(NonTerminals.EXPR),
			new Token(State.SEMI)
		}));

		addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.STAT_IF)
		}));

		//	STAT ID
		addRule(new ProductionRule(NonTerminals.STAT, new ParserSymbol[]{
			new Token(State.ID),
			new NonTerminalParserSymbol(NonTerminals.STAT_AFTER_ID)
		}));

		addRule(new ProductionRule(NonTerminals.STAT_AFTER_ID, new ParserSymbol[]{
			new Token(State.LPAREN),
			new NonTerminalParserSymbol(NonTerminals.EXPR_LIST),
			new Token(State.RPAREN),
			new Token(State.SEMI)
		}));

		addRule(new ProductionRule(NonTerminals.STAT_AFTER_ID, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.LVALUE_TAIL),
			new Token(State.ASSIGN),
			new NonTerminalParserSymbol(NonTerminals.EXPR_OR_ID),
			new Token(State.SEMI)
		}));

		addRule(new ProductionRule(NonTerminals.EXPR_OR_ID, new ParserSymbol[]{
			new Token(State.ID),
			new NonTerminalParserSymbol(NonTerminals.EXPR_OR_FUNC)
		}));

		addRule(new ProductionRule(NonTerminals.EXPR_OR_ID, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.EXPR_NO_LVALUE)
		}));

		addRule(new ProductionRule(NonTerminals.EXPR_OR_FUNC, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.LVALUE_TAIL),
			new NonTerminalParserSymbol(NonTerminals.EXPR_ANY_TAIL)
		}));

		addRule(new ProductionRule(NonTerminals.EXPR_OR_FUNC, new ParserSymbol[]{
			new Token(State.LPAREN),
			new NonTerminalParserSymbol(NonTerminals.EXPR_LIST),
			new Token(State.RPAREN)
		}));

		
		// STAT_IF
		addRule(new ProductionRule(NonTerminals.STAT_IF, new ParserSymbol[]{
			new Token(State.IF),
			new NonTerminalParserSymbol(NonTerminals.EXPR),
			new Token(State.THEN),
			new NonTerminalParserSymbol(NonTerminals.STAT_SEQ),
			new NonTerminalParserSymbol(NonTerminals.STAT_IF_CLAUSE_2)
		}));
		
		// STAT_IF_CLAUSE_2
		addRule(new ProductionRule(NonTerminals.STAT_IF_CLAUSE_2, new ParserSymbol[]{
			new Token(State.ENDIF),
			new Token(State.SEMI)
		}));
		
		addRule(new ProductionRule(NonTerminals.STAT_IF_CLAUSE_2, new ParserSymbol[]{
			new Token(State.ELSE),
			new NonTerminalParserSymbol(NonTerminals.STAT_SEQ),
			new Token(State.ENDIF),
			new Token(State.SEMI)
		}));
		
		// EXPR
		addRule(new ProductionRule(NonTerminals.EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.LVALUE),
			new NonTerminalParserSymbol(NonTerminals.EXPR_ANY_TAIL)
		}));

		addRule(new ProductionRule(NonTerminals.EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.EXPR_NO_LVALUE)
		}));

		//	EXPR_NO_LVALUE
		addRule(new ProductionRule(NonTerminals.EXPR_NO_LVALUE, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.CONST),
			new NonTerminalParserSymbol(NonTerminals.EXPR_ANY_TAIL)
		}));

		addRule(new ProductionRule(NonTerminals.EXPR_NO_LVALUE, new ParserSymbol[]{
			new Token(State.MINUS),
			new NonTerminalParserSymbol(NonTerminals.ATOM_EXPR)
		}));

		addRule(new ProductionRule(NonTerminals.EXPR_NO_LVALUE, new ParserSymbol[]{
			new Token(State.LPAREN),
			new NonTerminalParserSymbol(NonTerminals.EXPR),
			new Token(State.RPAREN),
			new NonTerminalParserSymbol(NonTerminals.EXPR_ANY_TAIL)
		}));


		//	EXPR_ANY_TAIL

		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.MULT_DIV_EXPR_TAIL)
		}));
		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.ADD_SUB_EXPR_TAIL)
		}));
		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.AND_EXPR_TAIL)
		}));
		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.BOOL_EXPR_TAIL)
		}));
		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL, new ParserSymbol[]{
			new Token(State.NULL)
		}));


		//	FIXME: check OR expressions

		
		//ATOM_EXPR
		addRule(new ProductionRule(NonTerminals.ATOM_EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.CONST)
		}));
		
		addRule(new ProductionRule(NonTerminals.ATOM_EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.LVALUE)
		}));
		
		//NEGATED_EXPR
		addRule(new ProductionRule(NonTerminals.NEGATED_EXPR, new ParserSymbol[]{
			new Token(State.MINUS), new NonTerminalParserSymbol(NonTerminals.ATOM_EXPR)
		}));
		
		addRule(new ProductionRule(NonTerminals.NEGATED_EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.ATOM_EXPR)
		}));
		
		// MULT_DIV_EXPR
		addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.NEGATED_EXPR),
				new NonTerminalParserSymbol(NonTerminals.MULT_DIV_EXPR_TAIL),
		}));
		
		// MULT_DIV_EXPR_TAIL
		addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR_TAIL, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.MULT_DIV_OP),
			new NonTerminalParserSymbol(NonTerminals.NEGATED_EXPR), 
			new NonTerminalParserSymbol(NonTerminals.MULT_DIV_EXPR_TAIL)
		}));

		addRule(new ProductionRule(NonTerminals.MULT_DIV_OP, new ParserSymbol[]{
			new Token(State.MULT)
		}));

		addRule(new ProductionRule(NonTerminals.MULT_DIV_OP, new ParserSymbol[]{
			new Token(State.DIV)
		}));
		
		addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR_TAIL, new ParserSymbol[]{
				new Token(State.NULL)
		}));
		
		// ADD_SUB_EXPR
		addRule(new ProductionRule(NonTerminals.ADD_SUB_EXPR, new ParserSymbol[]{
				new NonTerminalParserSymbol(NonTerminals.MULT_DIV_EXPR),
				new NonTerminalParserSymbol(NonTerminals.ADD_SUB_EXPR_TAIL)
		}));
		
		// ADD_SUB_EXPR_TAIL
		addRule(new ProductionRule(NonTerminals.ADD_SUB_EXPR_TAIL, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.ADD_SUB_OP),
			new NonTerminalParserSymbol(NonTerminals.MULT_DIV_EXPR),
			new NonTerminalParserSymbol(NonTerminals.ADD_SUB_EXPR_TAIL)
		}));

		addRule(new ProductionRule(NonTerminals.ADD_SUB_OP, new ParserSymbol[]{
			new Token(State.PLUS)
		}));

		addRule(new ProductionRule(NonTerminals.ADD_SUB_OP, new ParserSymbol[]{
			new Token(State.MINUS)
		}));
		
		addRule(new ProductionRule(NonTerminals.ADD_SUB_EXPR_TAIL, new ParserSymbol[]{
			new Token(State.NULL)
		}));
		
		// BOOL_EXPR{
		addRule(new ProductionRule(NonTerminals.BOOL_EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.ADD_SUB_EXPR),
			new NonTerminalParserSymbol(NonTerminals.BOOL_EXPR_TAIL)
		}));
		
		// BOOL_EXPR_TAIL
		addRule(new ProductionRule(NonTerminals.BOOL_EXPR_TAIL, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.BOOL_OP),
			new NonTerminalParserSymbol(NonTerminals.ADD_SUB_EXPR), 
			new NonTerminalParserSymbol(NonTerminals.BOOL_EXPR_TAIL)
		}));
		
		addRule(new ProductionRule(NonTerminals.BOOL_EXPR_TAIL, new ParserSymbol[]{
			new Token(State.NULL)
		}));
		
		// AND_EXPR
		addRule(new ProductionRule(NonTerminals.AND_EXPR, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.BOOL_EXPR),
			new NonTerminalParserSymbol(NonTerminals.AND_EXPR_TAIL)
		}));
		
		// AND_EXPR_TAIL
		addRule(new ProductionRule(NonTerminals.AND_EXPR_TAIL, new ParserSymbol[]{
			new Token(State.AND),
			new NonTerminalParserSymbol(NonTerminals.BOOL_EXPR), 
			new NonTerminalParserSymbol(NonTerminals.AND_EXPR_TAIL)
		}));
		
		addRule(new ProductionRule(NonTerminals.AND_EXPR_TAIL, new ParserSymbol[]{
			new Token(State.NULL)
		}));
		
		// BOOL_OP
		addRule(new ProductionRule(NonTerminals.BOOL_OP, new ParserSymbol[]{
			new Token(State.EQ)
		}));
		
		addRule(new ProductionRule(NonTerminals.BOOL_OP, new ParserSymbol[]{
			new Token(State.NEQ)
		}));
		
		addRule(new ProductionRule(NonTerminals.BOOL_OP, new ParserSymbol[]{
			new Token(State.GREATER)
		}));
		
		addRule(new ProductionRule(NonTerminals.BOOL_OP, new ParserSymbol[]{
			new Token(State.LESSER)
		}));
		
		addRule(new ProductionRule(NonTerminals.BOOL_OP, new ParserSymbol[]{
			new Token(State.GREATEREQ)
		}));

		addRule(new ProductionRule(NonTerminals.BOOL_OP, new ParserSymbol[]{
			new Token(State.LESSEREQ) 
		}));
		
		// CONST
		addRule(new ProductionRule(NonTerminals.CONST, new ParserSymbol[]{
			new Token(State.INTLIT)
		}));
		
		addRule(new ProductionRule(NonTerminals.CONST, new ParserSymbol[]{
			new Token(State.STRLIT)
		}));

		addRule(new ProductionRule(NonTerminals.CONST, new ParserSymbol[]{
			new Token(State.NIL)
		}));
		
		// EXPR_LIST
		addRule(new ProductionRule(NonTerminals.EXPR_LIST, new ParserSymbol[]{
			new Token(State.NULL)
		}));
		
		addRule(new ProductionRule(NonTerminals.EXPR_LIST, new ParserSymbol[]{
			new NonTerminalParserSymbol(NonTerminals.EXPR),
			new NonTerminalParserSymbol(NonTerminals.EXPR_LIST_TAIL)
		}));
		
		// EXPR_LIST_TAIL
		addRule(new ProductionRule(NonTerminals.EXPR_LIST_TAIL, new ParserSymbol[]{
			new Token(State.COMMA),
			new NonTerminalParserSymbol(NonTerminals.EXPR), 
			new NonTerminalParserSymbol(NonTerminals.EXPR_LIST_TAIL)
		}));
		
		addRule(new ProductionRule(NonTerminals.EXPR_LIST_TAIL, new ParserSymbol[]{
			new Token(State.NULL)
		}));
		
		// LVALUE{
		addRule(new ProductionRule(NonTerminals.LVALUE, new ParserSymbol[]{
			new Token(State.ID),
			new NonTerminalParserSymbol(NonTerminals.LVALUE_TAIL)
		}));
		
		// LVALUE_TAIL
		addRule(new ProductionRule(NonTerminals.LVALUE_TAIL, new ParserSymbol[]{
			new Token(State.LBRACK),
			new NonTerminalParserSymbol(NonTerminals.EXPR), 
			new Token(State.RBRACK),
			new NonTerminalParserSymbol(NonTerminals.LVALUE_TAIL)
		}));
		
		addRule(new ProductionRule(NonTerminals.LVALUE_TAIL, new ParserSymbol[]{
			new Token(State.NULL)
		}));
	}
}
