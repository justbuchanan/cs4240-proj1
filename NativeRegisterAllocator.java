import java.util.ArrayList;

public class NaiveRegisterAllocator implements IRegisterAllocator{
	private ArrayList<CodeStatement> finalCode;

	public static ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> origMips){
		int currReg = 0; 

		String[] threeRegInstr = {"ADD", ""}
		
		for(CodeStatement stmt : origMips){
			if(stmt.getOperator().equals()){ // TODO: make this any 4-address instuction

				// ld operands
				finalCode.add(new CodeStatement("LW" , "$t0", stmt.getLeftOperand()));
				finalCode.add(new CodeStatement("LW", "$t1", stmt.getRightOperand()));
				
				// exec instruction, output register is now $t2
				finalCode.add(new CodeStatement(stmt.getOperator(), "$t2", "$t0", "$t1"));

				// store result
				finalCode.add(new CodeStatement("SW", "$t2", stmt.getOutputRegister()));

			}else if(stmt.getOperator().equal(""))
		}
	}
}