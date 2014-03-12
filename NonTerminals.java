
/**
 * All non-terminals from the parser grammar
 */
public enum NonTerminals {
	TIGER_PROGRAM,

	DECLARATION_SEGMENT,
	TYPE_DECLARATION_LIST,
	VAR_DECLARATION_LIST,
	FUNCT_DECLARATION_LIST,
	TYPE_DECLARATION,
	TYPE,
	TYPEDIM,
	TYPE_ID,
	VAR_DECLARATION,
	ID_LIST,
	ID_LIST_PRIME,
	OPTIONAL_INIT,
	FUNCT_DECLARATION,
	PARAM_LIST,
	PARAM_LIST_TAIL,
	RET_TYPE,
	PARAM,

	STAT_SEQ,
	STAT_SEQ_PRIME,
	STAT,
	STAT_IF_TAIL,
	
	STAT_AFTER_ID,
	EXPR_OR_ID,
	EXPR_OR_FUNC,
	EXPR_NO_LVALUE,
	EXPR_ANY_TAIL,

	EXPR,
	ATOM_EXPR,
	NEGATED_EXPR,
	MULT_DIV_EXPR,
	MULT_DIV_OP,
	MULT_DIV_EXPR_TAIL,
	ADD_SUB_EXPR,
	ADD_SUB_OP,
	ADD_SUB_EXPR_TAIL,
	BOOL_EXPR,
	BOOL_EXPR_TAIL,
	AND_EXPR,
	AND_EXPR_TAIL,
	OR_EXPR,
	OR_EXPR_TAIL,

	BOOL_OP,

	CONST,
	EXPR_LIST,
	EXPR_LIST_TAIL,
	LVALUE,
	LVALUE_TAIL,

	OPT_PREFIX,
	
}