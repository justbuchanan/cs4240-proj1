
public class VarSymbolEntry extends SymbolTableEntry {

	private TypeSymbolEntry type;
	
	public VarSymbolEntry(String name, Scope scope, TypeSymbolEntry type) {
		super(name, scope);
		this.type = type;
	}
	
	public String toString(){
		return "(name: " + this.getName() + " | level: " + this.getScope().getLevel() + " | func: "
				+ this.getScope().getFuncName() + " | type " + type.getName() + ")";
	}
	
}
