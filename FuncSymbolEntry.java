import java.util.LinkedList;


public class FuncSymbolEntry extends SymbolTableEntry{
	
	private LinkedList<VarSymbolEntry> params;
	
	public FuncSymbolEntry(String name, Scope scope) {
		super(name, scope);
		params = new LinkedList();
	}
	
	public LinkedList<VarSymbolEntry> getParams(){
		return params;
	}

	public void addParam(VarSymbolEntry var){
		params.add(var);
	}
}
