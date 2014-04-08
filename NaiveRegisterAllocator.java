import java.util.ArrayList;

public class NaiveRegisterAllocator implements IRegisterAllocator{

	public  ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> origMips){
		int currReg = 0; 
		ArrayList<CodeStatement> finalCode = new ArrayList();
		
		for(CodeStatement stmt : origMips){
			if(stmt.getOperator().equals("")){ // TODO: make this any 4-address instuction

				// ld operands
				finalCode.add(new CodeStatement("LW" , "$t0", stmt.getLeftOperand()));
				finalCode.add(new CodeStatement("LW", "$t1", stmt.getRightOperand()));
				
				// exec instruction, output register is now $t2
				finalCode.add(new CodeStatement(stmt.getOperator(), "$t2", "$t0", "$t1"));

				// store result
				finalCode.add(new CodeStatement("SW", "$t2", stmt.getOutputRegister()));

			}
		}
		return finalCode;
	}
}