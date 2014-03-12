
//	accept states are token types
//	Terminals come first so the indices of the columns for the parser table can start at zero
public enum State {
	COMMA,
	LPAREN,
	RPAREN,
	NUMBER,
	ID,
	
	NULL,

	ERROR,
}
