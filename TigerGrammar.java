
public class TigerGrammar extends Grammar {
	TigerGrammar() {
		// TIGER_PROGRAM
		addRule(new ProductionRule(NonTerminals.TIGER_PROGRAM,
			State.LET, NonTerminals.DECLARATION_SEGMENT, State.IN, NonTerminals.STAT_SEQ, State.END));
		
		// DECLARATION_SEGMENT
		addRule(new ProductionRule(NonTerminals.DECLARATION_SEGMENT,
			NonTerminals.TYPE_DECLARATION_LIST, NonTerminals.VAR_DECLARATION_LIST, NonTerminals.FUNCT_DECLARATION_LIST));
		
		// TYPE_DECLARATION_LIST
		addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION_LIST,
			State.NULL	));
		
		addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION_LIST, 
			NonTerminals.TYPE_DECLARATION, 	NonTerminals.TYPE_DECLARATION_LIST));
		
		// VAR_DECLARATION_LIST
		addRule(new ProductionRule(NonTerminals.VAR_DECLARATION_LIST,
			State.NULL));
		
		addRule(new ProductionRule(NonTerminals.VAR_DECLARATION_LIST, 
			NonTerminals.VAR_DECLARATION, 	NonTerminals.VAR_DECLARATION_LIST));
		
		// FUNCT_DECLARATION_LIST
		addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION_LIST,
			State.NULL	));
		
		addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION_LIST,
			NonTerminals.FUNCT_DECLARATION, NonTerminals.FUNCT_DECLARATION_LIST	));
		
		// TYPE_DECLARATION
		addRule(new ProductionRule(NonTerminals.TYPE_DECLARATION,
			State.TYPE, State.ID, 
			State.EQ, NonTerminals.TYPE));
		
		// TYPE
		addRule(new ProductionRule(NonTerminals.TYPE, 
			State.ARRAY, 	State.LBRACK, 	State.INTLIT, 	State.RBRACK, 	NonTerminals.TYPEDIM, 	State.OF, 	NonTerminals.TYPE_ID, 	State.SEMI));
		
		//TYPEDIM
		addRule(new ProductionRule(NonTerminals.TYPEDIM,
			State.LBRACK, State.INTLIT, State.RBRACK, NonTerminals.TYPEDIM));
		
		addRule(new ProductionRule(NonTerminals.TYPEDIM, 
			State.NULL));
		
		// TYPE_ID
		addRule(new ProductionRule(NonTerminals.TYPE_ID, 
			State.INTLIT));
		
		addRule(new ProductionRule(NonTerminals.TYPE_ID,
			State.STRLIT	));
		
		addRule(new ProductionRule(NonTerminals.TYPE_ID,
			State.ID));
		
		// VAR_DECLARATION
		addRule(new ProductionRule(NonTerminals.VAR_DECLARATION, 
			State.VAR, 	NonTerminals.ID_LIST, 	State.COLON, 	NonTerminals.TYPE_ID, 	NonTerminals.OPTIONAL_INIT, 	State.SEMI));
		
		// ID_LIST
		addRule(new ProductionRule(NonTerminals.ID_LIST, 
			State.ID, NonTerminals.ID_LIST_PRIME));
		
		// ID_LIST_PRIME
		addRule(new ProductionRule(NonTerminals.ID_LIST_PRIME, 
			State.NULL));
		
		addRule(new ProductionRule(NonTerminals.ID_LIST_PRIME, 
			State.COMMA, NonTerminals.ID_LIST));
		
		// OPTIONAL_INIT
		addRule(new ProductionRule(NonTerminals.OPTIONAL_INIT, 
			State.NULL));
		
		addRule(new ProductionRule(NonTerminals.OPTIONAL_INIT, 
			State.ASSIGN, 	NonTerminals.CONST));
		
		// FUNCT_DECLARATION
		addRule(new ProductionRule(NonTerminals.FUNCT_DECLARATION, 
			State.FUNC, 	State.ID, 	State.LPAREN, 	NonTerminals.PARAM_LIST, 	State.RPAREN, 
				NonTerminals.RET_TYPE, 	State.BEGIN, 
				NonTerminals.STAT_SEQ, 	State.END, State.SEMI));
		
		// PARAM_LIST
		addRule(new ProductionRule(NonTerminals.PARAM_LIST, 
			State.NULL));
		
		addRule(new ProductionRule(NonTerminals.PARAM_LIST, 
			NonTerminals.PARAM, NonTerminals.PARAM_LIST_TAIL));
		
		// PARAM_LIST_TAIL
		addRule(new ProductionRule(NonTerminals.PARAM_LIST_TAIL, 
			State.NULL));
		
		addRule(new ProductionRule(NonTerminals.PARAM_LIST_TAIL, 
			State.COMMA, 	NonTerminals.PARAM, 	NonTerminals.PARAM_LIST_TAIL));
		
		// RET_TYPE
		addRule(new ProductionRule(NonTerminals.RET_TYPE, 
			State.NULL));
		
		addRule(new ProductionRule(NonTerminals.RET_TYPE, 
			State.COLON, 	NonTerminals.TYPE_ID));
		
		// PARAM
		addRule(new ProductionRule(NonTerminals.PARAM, 
			State.ID, State.COLON, 	NonTerminals.TYPE_ID));
		
		// STAT_SEQ
		addRule(new ProductionRule(NonTerminals.STAT_SEQ, 
			NonTerminals.STAT, 	NonTerminals.STAT_SEQ_PRIME));
		
		// STAT_SEQ_PRIME
		addRule(new ProductionRule(NonTerminals.STAT_SEQ_PRIME,
			NonTerminals.STAT, NonTerminals.STAT_SEQ_PRIME));
		
		addRule(new ProductionRule(NonTerminals.STAT_SEQ_PRIME,
			State.NULL));
		
		// STAT

		addRule(new ProductionRule(NonTerminals.STAT,
			State.WHILE, NonTerminals.EXPR, State.DO, NonTerminals.STAT_SEQ, State.ENDDO, State.SEMI));
		
		addRule(new ProductionRule(NonTerminals.STAT,
			State.FOR, State.ID, State.ASSIGN, NonTerminals.EXPR, State.TO, NonTerminals.EXPR, 
			State.DO, NonTerminals.STAT_SEQ, State.ENDDO, 
			State.SEMI));

		addRule(new ProductionRule(NonTerminals.STAT,
			State.BREAK, State.SEMI));
		
		addRule(new ProductionRule(NonTerminals.STAT,
			State.RETURN, NonTerminals.EXPR, State.SEMI));


		//	STAT ID
		addRule(new ProductionRule(NonTerminals.STAT,
			State.ID, NonTerminals.STAT_AFTER_ID));

		addRule(new ProductionRule(NonTerminals.STAT_AFTER_ID,
			State.LPAREN, NonTerminals.EXPR_LIST, State.RPAREN, State.SEMI));

		addRule(new ProductionRule(NonTerminals.STAT_AFTER_ID,
			NonTerminals.LVALUE_TAIL, State.ASSIGN, NonTerminals.EXPR_OR_ID, State.SEMI));

		addRule(new ProductionRule(NonTerminals.EXPR_OR_ID,
			State.ID, NonTerminals.EXPR_OR_FUNC));

		addRule(new ProductionRule(NonTerminals.EXPR_OR_ID,
			NonTerminals.EXPR_NO_LVALUE));

		addRule(new ProductionRule(NonTerminals.EXPR_OR_FUNC,
			NonTerminals.LVALUE_TAIL, NonTerminals.EXPR_ANY_TAIL));

		addRule(new ProductionRule(NonTerminals.EXPR_OR_FUNC,
			State.LPAREN, NonTerminals.EXPR_LIST, State.RPAREN));

		
		// STAT_IF
		addRule(new ProductionRule(NonTerminals.STAT,
			State.IF, NonTerminals.EXPR, State.THEN, NonTerminals.STAT_SEQ, NonTerminals.STAT_IF_TAIL));
		
		// STAT_IF_TAIL
		addRule(new ProductionRule(NonTerminals.STAT_IF_TAIL,
			State.ENDIF, State.SEMI));
		
		addRule(new ProductionRule(NonTerminals.STAT_IF_TAIL,
			State.ELSE, NonTerminals.STAT_SEQ, State.ENDIF, State.SEMI));
		
		// EXPR
		addRule(new ProductionRule(NonTerminals.EXPR,
			NonTerminals.LVALUE, NonTerminals.EXPR_ANY_TAIL));

		addRule(new ProductionRule(NonTerminals.EXPR,
			NonTerminals.EXPR_NO_LVALUE));

		//	EXPR_NO_LVALUE
		addRule(new ProductionRule(NonTerminals.EXPR_NO_LVALUE,
			NonTerminals.CONST, NonTerminals.EXPR_ANY_TAIL));

		addRule(new ProductionRule(NonTerminals.EXPR_NO_LVALUE,
			State.MINUS, NonTerminals.ATOM_EXPR));

		addRule(new ProductionRule(NonTerminals.EXPR_NO_LVALUE,
			State.LPAREN, NonTerminals.EXPR, State.RPAREN, NonTerminals.EXPR_ANY_TAIL));


		//	EXPR_ANY_TAIL

		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL,
			NonTerminals.MULT_DIV_EXPR_TAIL));
		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL,
			NonTerminals.ADD_SUB_EXPR_TAIL));
		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL,
			NonTerminals.AND_EXPR_TAIL));
		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL,
			NonTerminals.OR_EXPR_TAIL));
		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL,
			NonTerminals.BOOL_EXPR_TAIL));
		addRule(new ProductionRule(NonTerminals.EXPR_ANY_TAIL,
			State.NULL));


		//	FIXME: check OR expressions

		
		//ATOM_EXPR
		addRule(new ProductionRule(NonTerminals.ATOM_EXPR,
			NonTerminals.CONST));
		
		addRule(new ProductionRule(NonTerminals.ATOM_EXPR,
			NonTerminals.LVALUE));
		
		//NEGATED_EXPR
		addRule(new ProductionRule(NonTerminals.NEGATED_EXPR,
			State.MINUS, NonTerminals.ATOM_EXPR));
		
		addRule(new ProductionRule(NonTerminals.NEGATED_EXPR,
			NonTerminals.ATOM_EXPR));
		
		// MULT_DIV_EXPR
		addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR, 
			NonTerminals.NEGATED_EXPR, 	NonTerminals.MULT_DIV_EXPR_TAIL));
		
		// MULT_DIV_EXPR_TAIL
		addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR_TAIL,
			NonTerminals.MULT_DIV_OP, NonTerminals.NEGATED_EXPR, 
			NonTerminals.MULT_DIV_EXPR_TAIL));

		addRule(new ProductionRule(NonTerminals.MULT_DIV_OP,
			State.MULT));

		addRule(new ProductionRule(NonTerminals.MULT_DIV_OP,
			State.DIV));
		
		addRule(new ProductionRule(NonTerminals.MULT_DIV_EXPR_TAIL, 
			State.NULL));
		
		// ADD_SUB_EXPR
		addRule(new ProductionRule(NonTerminals.ADD_SUB_EXPR, 
			NonTerminals.MULT_DIV_EXPR, 	NonTerminals.ADD_SUB_EXPR_TAIL));
		
		// ADD_SUB_EXPR_TAIL
		addRule(new ProductionRule(NonTerminals.ADD_SUB_EXPR_TAIL,
			NonTerminals.ADD_SUB_OP, NonTerminals.MULT_DIV_EXPR, NonTerminals.ADD_SUB_EXPR_TAIL));

		addRule(new ProductionRule(NonTerminals.ADD_SUB_OP,
			State.PLUS));

		addRule(new ProductionRule(NonTerminals.ADD_SUB_OP,
			State.MINUS));
		
		addRule(new ProductionRule(NonTerminals.ADD_SUB_EXPR_TAIL,
			State.NULL));
		
		// BOOL_EXPR{
		addRule(new ProductionRule(NonTerminals.BOOL_EXPR,
			NonTerminals.ADD_SUB_EXPR, NonTerminals.BOOL_EXPR_TAIL));
		
		// BOOL_EXPR_TAIL
		addRule(new ProductionRule(NonTerminals.BOOL_EXPR_TAIL,
			NonTerminals.BOOL_OP, NonTerminals.ADD_SUB_EXPR, 
			NonTerminals.BOOL_EXPR_TAIL));
		
		addRule(new ProductionRule(NonTerminals.BOOL_EXPR_TAIL,
			State.NULL));
		
		// AND_EXPR
		addRule(new ProductionRule(NonTerminals.AND_EXPR,
			NonTerminals.BOOL_EXPR, NonTerminals.AND_EXPR_TAIL));
		
		// AND_EXPR_TAIL
		addRule(new ProductionRule(NonTerminals.AND_EXPR_TAIL,
			State.AND, NonTerminals.BOOL_EXPR, 
			NonTerminals.AND_EXPR_TAIL));
		
		addRule(new ProductionRule(NonTerminals.AND_EXPR_TAIL,
			State.NULL));

		//	OR_EXPR
		addRule(new ProductionRule(NonTerminals.OR_EXPR,
			NonTerminals.BOOL_EXPR, NonTerminals.OR_EXPR_TAIL));

		// OR_EXPR_TAIL
		addRule(new ProductionRule(NonTerminals.OR_EXPR_TAIL,
			State.OR, NonTerminals.BOOL_EXPR, 
			NonTerminals.OR_EXPR_TAIL));
		
		addRule(new ProductionRule(NonTerminals.OR_EXPR_TAIL,
			State.NULL));

		
		// BOOL_OP
		addRule(new ProductionRule(NonTerminals.BOOL_OP,
			State.EQ));
		
		addRule(new ProductionRule(NonTerminals.BOOL_OP,
			State.NEQ));
		
		addRule(new ProductionRule(NonTerminals.BOOL_OP,
			State.GREATER));
		
		addRule(new ProductionRule(NonTerminals.BOOL_OP,
			State.LESSER));
		
		addRule(new ProductionRule(NonTerminals.BOOL_OP,
			State.GREATEREQ));

		addRule(new ProductionRule(NonTerminals.BOOL_OP,
			State.LESSEREQ ));
		
		// CONST
		addRule(new ProductionRule(NonTerminals.CONST,
			State.INTLIT));
		
		addRule(new ProductionRule(NonTerminals.CONST,
			State.STRLIT));

		addRule(new ProductionRule(NonTerminals.CONST,
			State.NIL));
		
		// EXPR_LIST
		addRule(new ProductionRule(NonTerminals.EXPR_LIST,
			State.NULL));
		
		addRule(new ProductionRule(NonTerminals.EXPR_LIST,
			NonTerminals.EXPR, NonTerminals.EXPR_LIST_TAIL));
		
		// EXPR_LIST_TAIL
		addRule(new ProductionRule(NonTerminals.EXPR_LIST_TAIL,
			State.COMMA, NonTerminals.EXPR, 
			NonTerminals.EXPR_LIST_TAIL));
		
		addRule(new ProductionRule(NonTerminals.EXPR_LIST_TAIL,
			State.NULL));
		
		// LVALUE{
		addRule(new ProductionRule(NonTerminals.LVALUE,
			State.ID, NonTerminals.LVALUE_TAIL));
		
		// LVALUE_TAIL
		addRule(new ProductionRule(NonTerminals.LVALUE_TAIL,
			State.LBRACK, NonTerminals.EXPR, 
			State.RBRACK, NonTerminals.LVALUE_TAIL));
		
		addRule(new ProductionRule(NonTerminals.LVALUE_TAIL,
			State.NULL));
	}
}
