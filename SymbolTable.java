import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class SymbolTable {
	private LinkedList<Scope> scopes;
	private HashMap<String, ArrayList<VarSymbolEntry>> vars;
	private HashMap<String, FuncSymbolEntry> functions;
	private HashMap<String, TypeSymbolEntry> types;
	
	public SymbolTable() {
		vars = new HashMap<String, ArrayList<VarSymbolEntry>>();
		functions = new HashMap<String, FuncSymbolEntry>();
		types = new HashMap<String, TypeSymbolEntry>();
		scopes = new LinkedList<Scope>();
		scopes.push(new Scope("LET", 0));
		types.put("int", new TypeSymbolEntry("int", scopes.peek(), null, null));
		types.put("string", new TypeSymbolEntry("string", scopes.peek(), null, null));
	}
	

	public void addVar(String name, String type){
		if(!vars.containsKey(name)){
			vars.put(name, new ArrayList<VarSymbolEntry>());
		}
		VarSymbolEntry var = new VarSymbolEntry(name, scopes.peek(), types.get(type));
		if(functions.containsKey(scopes.peek().getFuncName())){
			FuncSymbolEntry func = functions.get(scopes.peek().getFuncName());
			func.addParam(var);
		}
		vars.get(name).add(var);
	}
	
	public boolean containsVar(String name){
		// TODO: Is everything in scope LET,0?
		return vars.get(name).get(0) != null;
	}

	public VarSymbolEntry getVar(String varName){
		return vars.get(varName).get(0);
	}

	public FuncSymbolEntry getFunc(String funcName){
		return functions.get(funcName);
	}

	public void addType(String name, String primType, ArrayList<Integer> arrDims){
			types.put(name, new TypeSymbolEntry(name, scopes.peek(), primType, arrDims));
	}
	
	public void beginScope(String funcName){
		Scope currScope = scopes.peek();
		scopes.push(new Scope(funcName, currScope.getLevel() + 1));
	}
	
	public void addFunc(String funcName){
		functions.put(funcName, new FuncSymbolEntry(funcName, scopes.peek()));
	}

	public boolean containsFunc(String funcName){
		return functions.get(funcName) != null;
	}

	public void endScope(){
		scopes.pop();
	}
	
	public void printSymbolTable(){
		System.out.println(":::::::PRINTING SYMBOL TABLE::::::::::");
		System.out.println("== VARS ==");
		for(String key : vars.keySet()){
			ArrayList<VarSymbolEntry> scopeVars = vars.get(key);
			for(VarSymbolEntry var : scopeVars){
				System.out.println("VAR: " + var.toString());
			}
		}
		System.out.println("== END VARS ==");
		System.out.println("== FUNCS == ");
		for(String key : functions.keySet()){
			FuncSymbolEntry func = functions.get(key);
			System.out.println("FUNC: " + func.toString());
		}
		System.out.println("== END FUNCS ==");
		System.out.println("== TYPES == ");
		for(String key : types.keySet()){
			TypeSymbolEntry type = types.get(key);
			System.out.println("TYPE: " + type.toString());
		}
		System.out.println("== END TYPES ==");
		System.out.println("::::    END PRINT SYMBOL TABLE    :::::");
	}
	
}
