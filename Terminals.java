
public enum Terminals {
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
	NULL
}



