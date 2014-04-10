import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NaiveRegisterAllocator implements RegisterAllocator{
	private ArrayList<CodeStatement> finalCode;
	private ArrayList<CodeStatement> origMips;
	Language lang;
	
	public NaiveRegisterAllocator(ArrayList<CodeStatement> origMips, Language lang){
		this.lang = lang;
		this.origMips = origMips;
	}
	public  void allocRegisters(){
		finalCode = new ArrayList();
		
		for(CodeStatement stmt : origMips){
			if(stmt.isLabel()){
				finalCode.add(stmt);
				continue;
			}
			int numReg = lang.numRegInstr(stmt.getOperator());
			
			switch(numReg){
				case 3:
					// ld operands
					finalCode.add(new CodeStatement("LW" , "$t0", stmt.getLeftOperand()));
					finalCode.add(new CodeStatement("LW", "$t1", stmt.getRightOperand()));
					
					// exec instruction, output register is now $t2
					finalCode.add(new CodeStatement(stmt.getOperator(), "$t2", "$t0", "$t1"));

					// store result
					finalCode.add(new CodeStatement("SW", "$t2", stmt.getOutputRegister()));
					break;
				case 2:
					String leftOp = stmt.getLeftOperand();
					if(leftOp.equals("$0")){
					}
				default:
					finalCode.add(stmt);
			}
		}
	}
	
	public void printCode(){
		for(CodeStatement stmt : finalCode){
			System.out.println(stmt.toString());
		}
	}
}