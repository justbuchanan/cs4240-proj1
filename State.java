
//	accept states are token types
//	Terminals come first so the indices of the columns for the parser table can start at zero
public enum State {
	COMMA,
	COLON,
	SEMI,
	LPAREN,
	RPAREN,
	LBRACK,
	RBRACK,
	MINUS,
	PLUS,
	MULT,
	DIV,
	EQ,

	LESSER,
	NEQ,
	GREATER,
	LESSEREQ,
	GREATEREQ,
	AND,
	OR,

	ASSIGN,

	COMMENT,

	ID,
	INTLIT,
	STRLIT,

	//	used only by the parser, not the scanner
	$,
	NULL,


	//	keywords - they're not really states, but they are token types
	ARRAY,
	BREAK,
	DO,
	ELSE,
	END,
	FOR,
	FUNC,
	IF,
	IN,
	LET,
	NIL,
	OF,
	THEN,
	TO,
	TYPE,
	VAR,
	WHILE,
	ENDIF,
	BEGIN,
	ENDDO,
	RETURN,


	//	the following are NOT accept states
	START,
	_COMMENT_END,
	STRLIT_PART,
	COMMENT_BEGIN,
	STRLIT_SLASH,
	STRLIT_SLASH_CONTROL,
	STRLIT_SLASH_DECIMAL1,
	STRLIT_SLASH_DECIMAL2,


	ERROR,
}



