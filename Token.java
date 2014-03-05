
public class Token extends ParserSymbol {
	public State type;
	public String value;
	public int lineNumber;

	Token(State type) {
		this.type = type;
	}

	Token(int type, String value, int lineNumber) {
		this.type = State.values()[type];
		this.value = value;
		this.lineNumber = lineNumber;
	}
	
	public boolean equals(Token other) {
		return other.type.equals(type);
	}
	
	public int ordinal(){
		return type.ordinal();
	}

	public State type() {
		return type;
	}

	public boolean isTerminal() {
		return true;
	}
}
