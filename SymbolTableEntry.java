public class SymbolTableEntry {
	private String name;
	private String type;
	private String scope;

	public SymbolTableEntry(String name, String type, String scope) {
		this.name = name;
		this.type = type;
		this.scope = scope;
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}

	public String getScope() {
		return scope;
	}
}
