import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NaiveRegisterAllocator implements IRegisterAllocator{
	private ArrayList<CodeStatement> finalCode;
	private ArrayList<CodeStatement> origMips;
	
	public NaiveRegisterAllocator(ArrayList<CodeStatement> origMips){
		this.origMips = origMips;
	}
	public  void allocRegisters(){
		int currReg = 0; 
		finalCode = new ArrayList();
		Set<String> threeRegInstr = new HashSet();
		//div is speciail case?
		threeRegInstr.add("add");
		threeRegInstr.add("addu");
		threeRegInstr.add("and");
		threeRegInstr.add("or");
		threeRegInstr.add("slt");
		threeRegInstr.add("sub");
		threeRegInstr.add("subu");
		threeRegInstr.add("xor");
		
		for(CodeStatement stmt : origMips){
			if(stmt.isLabel()){
				finalCode.add(stmt);
				continue;
			}
			if(threeRegInstr.contains(stmt.getOperator())){ // TODO: make this any 4-address instuction

				// ld operands
				finalCode.add(new CodeStatement("LW" , "$t0", stmt.getLeftOperand()));
				finalCode.add(new CodeStatement("LW", "$t1", stmt.getRightOperand()));
				
				// exec instruction, output register is now $t2
				finalCode.add(new CodeStatement(stmt.getOperator(), "$t2", "$t0", "$t1"));

				// store result
				finalCode.add(new CodeStatement("SW", "$t2", stmt.getOutputRegister()));

			}else{
				finalCode.add(stmt); //TODO: IMPLEMENT!
			}
		}
	}
	
	public void printCode(){
		for(CodeStatement stmt : finalCode){
			System.out.println(stmt.toString());
		}
	}
}