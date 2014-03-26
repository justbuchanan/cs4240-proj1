import java.util.ArrayList;

public class TypeSymbolEntry extends SymbolTableEntry{
	
	private String eltType;
	private int arrSize;
	private ArrayList<Integer> arrDims;
	
	public TypeSymbolEntry(String name, Scope scope, String eltType, ArrayList<Integer> arrDims) {
		super(name, scope);
		this.eltType = eltType;
		this.arrDims = arrDims;
	}
	
	public String getEltType(){
		return eltType;
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
