import java.util.ArrayList;

public class SymbolTable {
	private ArrayList<SymbolTableEntry> symbolTable;

	public SymbolTable() {
		symbolTable = new ArrayList<SymbolTableEntry>();
	}

	public void add(String name, String type, String scope) {
		symbolTable.add(new SymbolTableEntry(name, type, scope));
	}
}
