
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
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || this.getClass() != obj.getClass()) return false;

		Token other = (Token)obj;
		return other.type.equals(type);
	}

	@Override
	public int hashCode() {
		return type.hashCode();
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

	public String toString() {
		// return new String("Token(" + type + ": '" + value + "')");
		return type.toString();
	}
}
