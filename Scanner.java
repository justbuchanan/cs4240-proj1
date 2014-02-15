

public class Scanner {

	//	stateTransitionTable[currentState][nextCharacter] = nextState
	private int[][] _stateTransitionTable;

	//	where we're currently at in the string
	private int _strIndex;

	private String _string;


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
		if (_strIndex == _string.length()) {
			return -1;
		}

		char c = _string.charAt(_strIndex);
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
		int state = State.START.ordinal();
		String str = "";

		int c = popChar();
		while (true) {
			int nextState = _stateTransitionTable[state][c];
			if (nextState == -1) {
				if (str.length() > 0 && isAcceptState(state)) {
					pushChar(c);

					return new Token(state, str);
				} else {
					return new Token(State.ERROR.ordinal(), null);
				}
			} else {
				str += (char)c;
				state = nextState;
			}
		}
	}


	//	accept states are token types
	//	TODO: reorder to group accept states together
	//	COMMA and below are accept states
	public enum State {
		START,

		_ASSIGN,
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
		LESSEREQ,
		GREATEREQ,
		AND,
		OR,

		ASSIGN,

		COMMENT,

		ID,
		INTLIT,
		STRLIT,


		ERROR,
	}

	static int NUM_STATES = 35;

	public boolean isAcceptState(int state) {
		return state >= State.COMMA.ordinal();
	}

}
