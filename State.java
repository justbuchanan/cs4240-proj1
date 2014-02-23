
//	accept states are token types
//	TODO: reorder to group accept states together
//	COMMA and below are accept states
public enum State {
	START,

	_COMMENT_END,
	STRLIT_PART,
	COMMENT_BEGIN,
	STRLIT_SLASH,
	STRLIT_SLASH_CONTROL,
	STRLIT_SLASH_DECIMAL1,
	STRLIT_SLASH_DECIMAL2,

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



	ERROR,
}



