public abstract class SymbolTableEntry {
	private String name;
	private Scope scope;

	public SymbolTableEntry(String name, Scope scope) {
		this.name = name;
		this.scope = scope;
	}

	public String getName() {
		return name;
	}
	
	public Scope getScope() {
		return scope;
	}
}
