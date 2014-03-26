
public class VarSymbolEntry extends SymbolTableEntry {

	private TypeSymbolEntry type;
	
	public VarSymbolEntry(String name, Scope scope, TypeSymbolEntry type) {
		super(name, scope);
		this.type = type;
	}

	public TypeSymbolEntry getType(){
		return this.type;
	}
	
	public String toString(){
		return "(name: " + this.getName() + " | level: " + this.getScope().getLevel() + " | func: "
				+ this.getScope().getFuncName() + " | type " + type.getName() + ")";
	}
	
	public boolean equals(SymbolTableEntry var){
		return (var.getName().equals(this.getName()) &&
				var.getClass() == this.getClass() &&
				this.getScope().equals(var.getScope()) &&
				this.getType().equals(((VarSymbolEntry)var).getType()));
	}
	
}
