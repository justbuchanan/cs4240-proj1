

public class Scanner {

	Scanner(String str) {
		setupTransitionTable();
		_strIndex = 0;
		_string = str;
	}


	/**
	 * Gets the integer value of the next character.
	 * If no character is available, returns -1.
	 */
	public int popChar() {
		if (_strIndex == _strIndex.length()) {
			return -1;
		}

		char c = _string[_strIndex];
		_strIndex++;
		return c;
	}

	/**
	 * Undoes popChar()
	 */
	public void pushChar(int c) {
		_strIndex--;
	}


	/**
	 * Initializes the contents of @_stateTransitionTable so it is
	 * ready to scan the 'Tiger' language
	 */
	private void setupTransitionTable() {
		//	TODO

		_stateTransitionTable = new int[NUM_STATES][256];
		for (int i = 0; i < NUM_STATES; i++) {
			for (int j = 0; j < 256; j++) {
				//	mark everything as a non-accept state
				_stateTransitionTable[i][j] = -1;
			}
		}
	}


	public Token nextToken() {
		int state = START;
		String str = "";

		int c = popChar();
		while (true) {
			int nextState = _stateTransitionTable[state][c];
			if (nextState == -1) {
				if (str.length() > 0 && isAcceptState(state)) {
					Token tok;
					tok.type = state;
					tok.string = str;

					pushChar(c);

					return tok;
				} else {
					//	error!!!
					throw new ????;	//	FIXME: throw exception or return an invalid token type?
					return null;
				}
			} else {
				str += (char)c;
				state = nextState;
			}
		}
	}


	//	stateTransitionTable[currentState][nextCharacter] = nextState
	private int[][] _stateTransitionTable;

	//	where we're currently at in the string
	private int _strIndex;

	private String _string;


	public class Token {
		public int type;
		public String value;
	}


	//	accept states are token types
	//	TODO: reorder to group accept states together
	public enum States {
		START,

		COMMA,
		COLON,
		SEMI,
		LPAREN,
		RPAREN,
		LBRACK,
		RBRACK,
		LBRACE,
		RBRACE,
		PERIOD,
		MINUS,
		MULT,
		DIV,
		EQ,

		LESSER,
		NEQ,
		GREATER,
		LESSER,
		LESSEREQ,
		GREATEREQ,
		AND,
		OR,

		_ASSIGN,
		ASSIGN,

		COMMENT_BEGIN,
		_COMMENT_END,
		COMMENT,

		ID,
		INTLIT,
		STRLIT,
		STRLIT_PART,
		STRLIT_SLASH
	}

	static int NUM_STATES = 33;
}