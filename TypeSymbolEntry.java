import java.util.ArrayList;

public class TypeSymbolEntry extends SymbolTableEntry{
	
	private SymbolTable symbolTable;
	private String eltType;
	private int arrSize;
	private ArrayList<Integer> arrDims;
	private String typeString;
	private String canonicalTypeString;
	
	public TypeSymbolEntry(SymbolTable symbolTable, String name, Scope scope, String eltType, ArrayList<Integer> arrDims) {
		super(name, scope);

		this.symbolTable = symbolTable;

		if (eltType == null) eltType = name;
		this.eltType = eltType;

		this.arrDims = arrDims;

		//	build typeString
		this.typeString = this.eltType;
		if (arrDims != null) {
			for (Integer dim : arrDims) {
				this.typeString += "[]";
			}
		}

		//	canonicalTypeString
		//	traverse until we hit a base type
		TypeSymbolEntry ancestor = this;
		while (ancestor != null && !ancestor.getName().equals("int") && !ancestor.getName().equals("string")) {
			ancestor = symbolTable.getType(eltType);
		}

		if (ancestor != null) {
			this.canonicalTypeString = ancestor.getName();
			if (arrDims != null) {
				for (Integer dim : arrDims) {
					this.canonicalTypeString += "[]";
				}
			}
		}
	}
	
	public String getEltType(){
		return eltType;
	}

	/**
	 * Describes the type.  Use it for comparison between other types.
	 *
	 * Examples: "int", "int[]", "int[][]", "string", "string[][][]"
	 */
	public String typeString() {
		return typeString;
	}

	/**
	 * This can be used to see the "base" or "canonical" type of a type.
	 *
	 * If "bar" is a type defined to be an "int", then the canonical type of "bar" is "int".
	 */
	public String canonicalTypeString() {
		return canonicalTypeString;
	}
	
	public ArrayList<Integer> getArrDims(){
		return arrDims;
	}
	
	public String toString(){
		return "(name: " + this.getName() + " | level: " + this.getScope().getLevel() + " | func: "
				+ this.getScope().getFuncName() + " | eltType " + eltType +
				" | arrDims: " + arrDims + " | canonTypeStr " + canonicalTypeString +  ")";
	}
}
