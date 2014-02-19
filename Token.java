
public class Token {
		
	public State type;
	public String value;

	Token(int type, String value) {
		this.type = State.values()[type];
		this.value = value;
	}
}
