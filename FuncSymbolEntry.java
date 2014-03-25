import java.util.LinkedList;
import java.util.ArrayList;

public class FuncSymbolEntry extends SymbolTableEntry{
	
	private ArrayList<VarSymbolEntry> params;
	
	public FuncSymbolEntry(String name, Scope scope) {
		super(name, scope);
		params = new ArrayList();
	}
	
	public ArrayList<VarSymbolEntry> getParams(){
		return params;
	}

	public void addParam(VarSymbolEntry var){
		params.add(var);
	}
}
