
public class TypeSymbolEntry extends SymbolTableEntry{
	
	private String eltType;
	private int arrSize;
	
	public TypeSymbolEntry(String name, Scope scope, String eltType, int arrSize){
		super(name, scope);
		this.eltType = eltType;
		this.arrSize = arrSize;
	}
	
	public String getEltType(){
		return eltType;
	}
	
	public int getArrSize(){
		return arrSize;
	}
	
	public String toString(){
		return "(name: " + this.getName() + " | level: " + this.getScope().getLevel() + " | func: "
				+ this.getScope().getFuncName() + " | eltType " + eltType +
				" | arrSize " + arrSize +  ")";
	}
}
