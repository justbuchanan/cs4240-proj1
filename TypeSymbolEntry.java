import java.util.ArrayList;

public class TypeSymbolEntry extends SymbolTableEntry{
	
	private String eltType;
	private int arrSize;
	private ArrayList<Integer> arrDims;
	private String typeString;
	
	public TypeSymbolEntry(String name, Scope scope, String eltType, ArrayList<Integer> arrDims) {
		super(name, scope);

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
	
	public ArrayList<Integer> getArrDims(){
		return arrDims;
	}
	
	public String toString(){
		return "(name: " + this.getName() + " | level: " + this.getScope().getLevel() + " | func: "
				+ this.getScope().getFuncName() + " | eltType " + eltType +
				" | arrDims " + arrDims +  ")";
	}
}
