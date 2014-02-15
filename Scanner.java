

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

	public boolean hasChar() {
		return _strIndex < _string.length();
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
		//	initialize all entries to -1 (not a state)
		_stateTransitionTable = new int[NUM_STATES][256];
		for (int i = 0; i < NUM_STATES; i++) {
			for (int j = 0; j < 256; j++) {
				//	mark everything as a non-accept state
				_stateTransitionTable[i][j] = -1;
			}
		}

		_stateTransitionTable[State.START.ordinal()][','] = State.COMMA.ordinal();
		_stateTransitionTable[State.START.ordinal()][':'] = State.COLON.ordinal();
		_stateTransitionTable[State.START.ordinal()][';'] = State.SEMI.ordinal();
		_stateTransitionTable[State.START.ordinal()]['('] = State.LPAREN.ordinal();
		_stateTransitionTable[State.START.ordinal()][')'] = State.RPAREN.ordinal();
		_stateTransitionTable[State.START.ordinal()]['['] = State.LBRACK.ordinal();
		_stateTransitionTable[State.START.ordinal()][']'] = State.RBRACK.ordinal();
		_stateTransitionTable[State.START.ordinal()]['{'] = State.LBRACE.ordinal();
		_stateTransitionTable[State.START.ordinal()]['}'] = State.RBRACE.ordinal();
		_stateTransitionTable[State.START.ordinal()]['.'] = State.PERIOD.ordinal();
		_stateTransitionTable[State.START.ordinal()]['-'] = State.MINUS.ordinal();
		_stateTransitionTable[State.START.ordinal()]['*'] = State.MULT.ordinal();
		_stateTransitionTable[State.START.ordinal()]['/'] = State.DIV.ordinal();
		_stateTransitionTable[State.START.ordinal()]['='] = State.EQ.ordinal();
		_stateTransitionTable[State.START.ordinal()]['<'] = State.LESSER.ordinal();
		_stateTransitionTable[State.START.ordinal()]['>'] = State.GREATER.ordinal();
		_stateTransitionTable[State.START.ordinal()]['&'] = State.AND.ordinal();
		_stateTransitionTable[State.START.ordinal()]['|'] = State.OR.ordinal();
		
		_stateTransitionTable[State.COLON.ordinal()]['='] = State.ASSIGN.ordinal();
		_stateTransitionTable[State.LESSER.ordinal()]['>'] = State.NEQ.ordinal();
		_stateTransitionTable[State.LESSER.ordinal()]['='] = State.LESSEREQ.ordinal();
		_stateTransitionTable[State.GREATER.ordinal()]['='] = State.GREATEREQ.ordinal();


		//	TODO: STRLIT, COMMENT

		//	INTLIT

		for (int c = '0'; c <= '9'; c++) {
			_stateTransitionTable[State.START.ordinal()][c] = State.INTLIT.ordinal();
			_stateTransitionTable[State.INTLIT.ordinal()][c] = State.INTLIT.ordinal();
		}


		//	ID
		for (int c = 'a'; c <= 'z'; c++) {
			_stateTransitionTable[State.START.ordinal()][c] = State.ID.ordinal();
			_stateTransitionTable[State.ID.ordinal()][c] = State.ID.ordinal();
		}
		for (int c = 'Z'; c <= 'Z'; c++) {
			_stateTransitionTable[State.START.ordinal()][c] = State.ID.ordinal();
			_stateTransitionTable[State.ID.ordinal()][c] = State.ID.ordinal();
		}
		for (int c = '0'; c <= '9'; c++) {
			_stateTransitionTable[State.ID.ordinal()][c] = State.ID.ordinal();
		}
		_stateTransitionTable[State.ID.ordinal()]['_'] = State.ID.ordinal();


		//	STRLIT
		_stateTransitionTable[State.START.ordinal()]['"'] = State.STRLIT_PART.ordinal();

		for (int c = 0; c < 256; c++) {
			_stateTransitionTable[State.STRLIT_PART.ordinal()][c] = State.STRLIT_PART.ordinal();
		}
		_stateTransitionTable[State.STRLIT_PART.ordinal()]['\\'] = State.STRLIT_SLASH.ordinal();
		_stateTransitionTable[State.STRLIT_PART.ordinal()]['"'] = State.STRLIT.ordinal();

		_stateTransitionTable[State.STRLIT_SLASH.ordinal()]['^'] = State.STRLIT_SLASH_CONTROL.ordinal();

		_stateTransitionTable[State.STRLIT_SLASH.ordinal()]['\\'] = State.STRLIT_PART.ordinal();
		_stateTransitionTable[State.STRLIT_SLASH.ordinal()]['n'] = State.STRLIT_PART.ordinal();
		_stateTransitionTable[State.STRLIT_SLASH.ordinal()]['t'] = State.STRLIT_PART.ordinal();

		for (int c = '0'; c <= '9'; c++) {
			_stateTransitionTable[State.STRLIT_SLASH.ordinal()][c] = State.STRLIT_SLASH_DECIMAL1.ordinal();
		}
		for (int c = '0'; c <= '9'; c++) {
			_stateTransitionTable[State.STRLIT_SLASH_DECIMAL1.ordinal()][c] = State.STRLIT_SLASH_DECIMAL2.ordinal();
		}
		for (int c = '0'; c <= '9'; c++) {
			_stateTransitionTable[State.STRLIT_SLASH_DECIMAL2.ordinal()][c] = State.STRLIT_PART.ordinal();
		}
		
		
		//	COMMENT
		_stateTransitionTable[State.DIV.ordinal()]['*'] = State.COMMENT_BEGIN.ordinal();
		for (int i = 0; i <= 255; i++) {
			_stateTransitionTable[State.COMMENT_BEGIN.ordinal()][i] = State.COMMENT_BEGIN.ordinal();
		}
		_stateTransitionTable[State.COMMENT_BEGIN.ordinal()]['*'] = State._COMMENT_END.ordinal();
		for (int i = 0; i <= 255; i++) {
			_stateTransitionTable[State._COMMENT_END.ordinal()][i] = State._COMMENT_END.ordinal();
		}
		_stateTransitionTable[State._COMMENT_END.ordinal()]['/'] = State.COMMENT.ordinal();
	}


	public Token nextToken() {
		if (!hasChar()) return null;

		int state = State.START.ordinal();
		String str = "";

		int c;
		while (true) {
			c = popChar();
			if(!((c == '\n' || c == ' ' || c == '\t') && state == State.START.ordinal())) {
				int nextState = (c == -1) ? -1 : _stateTransitionTable[state][c];
				if (nextState == -1) {
					if (str.length() > 0 && isAcceptState(state)) {
						if (c != -1) pushChar(c);

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
	}


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
