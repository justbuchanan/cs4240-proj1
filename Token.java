
public class Token {
		
	public State type;
	public String value;
	public int lineNumber;

	Token(int type, String value, int lineNumber) {
		this.type = State.values()[type];
		this.value = value;
		this.lineNumber = lineNumber;
	}
}
