import java.util.LinkedList;
import java.util.ArrayList;

public class FuncSymbolEntry extends SymbolTableEntry{
	
	private ArrayList<VarSymbolEntry> params;
	private String returnType;
	
	public FuncSymbolEntry(String name, Scope scope, String returnType) {
		super(name, scope);
		params = new ArrayList();
		this.returnType = returnType;
	}
	
	public ArrayList<VarSymbolEntry> getParams(){
		return params;
	}

	public void addParam(VarSymbolEntry var){
		params.add(var);
	}

	public String getReturnType() {
		return returnType;
	}

	public String toString(){
		String baseStr =  "(" + this.getName() + " | params: ";
		for(VarSymbolEntry param : params){

			baseStr += param.getName();
			baseStr += " : " + param.getType().getName();
			baseStr += ", ";
		}

		if (returnType != null) {
			baseStr += "; returnType : " + returnType;
		}

		return baseStr += ")";
	}
}
