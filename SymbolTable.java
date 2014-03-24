import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class SymbolTable {
	private LinkedList<Scope> scopes;
	private HashMap<String, ArrayList<VarSymbolEntry>> vars;
	private HashMap<String, ArrayList<FuncSymbolEntry>> functions;
	private HashMap<String, TypeSymbolEntry> types;
	
	public SymbolTable() {
		vars = new HashMap<String, ArrayList<VarSymbolEntry>>();
		functions = new HashMap<String, ArrayList<FuncSymbolEntry>>();
		types = new HashMap<String, TypeSymbolEntry>();
		scopes = new LinkedList<Scope>();
		scopes.push(new Scope("LET", 0));
		types.put("int", new TypeSymbolEntry("int", scopes.peek(), null, 0));
		types.put("string", new TypeSymbolEntry("string", scopes.peek(), null, 0));
	}
	
	public boolean containsVar(String varName){
		return false;
	}

	public void addVar(String name, String type){
		if(!vars.containsKey(name)){
			vars.put(name, new ArrayList<VarSymbolEntry>());
		}
		
		vars.get(name).add(new VarSymbolEntry(name, scopes.peek(), types.get(type)));
	}
	
	public void addType(String name, String primType, int arrSize ){
			types.put(name, new TypeSymbolEntry(name, scopes.peek(), primType, arrSize));
	}
	
	public void beginScope(String funcName){
		Scope currScope = scopes.peek();
		scopes.push(new Scope(funcName, currScope.getLevel() + 1));
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
		/*System.out.println("== FUNCS == ");
		for(String key : functions.keySet()){
			FuncSymbolEntry func = functions.get(key);
			System.out.println("FUNC: " + func.toString());
		}
		System.out.println("== END FUNCS ==");*/
		System.out.println("== TYPES == ");
		for(String key : types.keySet()){
			TypeSymbolEntry type = types.get(key);
			System.out.println("TYPE: " + type.toString());
		}
		System.out.println("== END TYPES ==");
		System.out.println("::::    END PRINT SYMBOL TABLE    :::::");
	}
	
}
