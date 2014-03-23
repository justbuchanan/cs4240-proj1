
public class TypeSymbolEntry extends SymbolTableEntry{
	
	private PrimitiveTypes.PrimitiveType primitiveType;
	private int arrSize;
	
	public TypeSymbolEntry(String name, Scope scope, PrimitiveTypes.PrimitiveType primitiveType, int arrSize){
		super(name, scope);
		this.primitiveType = primitiveType;
		this.arrSize = arrSize;
	}
	
	public PrimitiveTypes.PrimitiveType getPrimType(){
		return primitiveType;
	}
	
	public int getArrSize(){
		return arrSize;
	}
	
	public String toString(){
		return "(name: " + this.getName() + " | level: " + this.getScope().getLevel() + " | func: "
				+ this.getScope().getFuncName() + " | primType " + primitiveType.values()[primitiveType.ordinal()]+
				" | arrSize " + arrSize +  ")";
	}
}
