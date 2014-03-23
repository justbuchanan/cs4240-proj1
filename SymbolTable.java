import java.util.ArrayList;
import java.util.HashMap;


public class SymbolTable {
	private Scope currScope = new Scope("LET", 0);
	private HashMap<String, ArrayList<VarSymbolEntry>> vars;
	private HashMap<String, ArrayList<FuncSymbolEntry>> functions;
	private HashMap<String, TypeSymbolEntry> types;
	
	public SymbolTable() {
		vars = new HashMap<String, ArrayList<VarSymbolEntry>>();
		functions = new HashMap<String, ArrayList<FuncSymbolEntry>>();
		types = new HashMap<String, TypeSymbolEntry>();
		types.put("int", new TypeSymbolEntry("int", currScope, PrimitiveTypes.PrimitiveType.INT, 0));
		types.put("string", new TypeSymbolEntry("string", currScope, PrimitiveTypes.PrimitiveType.STRING, 0));
	}
	
	public boolean containsVar(String varName){
		return false;
	}

	public void addVar(String name, String type){
		if(!vars.containsKey(name)){
			vars.put(name, new ArrayList<VarSymbolEntry>());
		}
		
		vars.get(name).add(new VarSymbolEntry(name, currScope, types.get(type)));
	}
	
	public void addType(String name, PrimitiveTypes.PrimitiveType primType, int arrSize ){
			types.put(name, new TypeSymbolEntry(name, currScope, primType, arrSize));
	}
	
	public void beginScope(String funcName){
		
	}
	
	public void endScope(){
		
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
