import java.util.LinkedList;


public class FuncSymbolEntry extends SymbolTableEntry{
	
	private LinkedList<VarSymbolEntry> params;
	
	public FuncSymbolEntry(String name, Scope scope, LinkedList<VarSymbolEntry> params) {
		super(name, scope);
		this.params = params;
	}
	
	public LinkedList<VarSymbolEntry> getParams(){
		return params;
	}

}
