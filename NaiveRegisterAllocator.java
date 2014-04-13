import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NaiveRegisterAllocator implements RegisterAllocator{
	private ArrayList<CodeStatement> finalCode;
	Language lang;
	
	public NaiveRegisterAllocator(Language lang){
		this.lang = lang;
	}
	public  ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> origMips){
		finalCode = new ArrayList();
		
		for(CodeStatement stmt : origMips){
			//TODO: REMOVE FOR DEBUGGING 
			finalCode.add(new CodeStatement("######## " + stmt.toString() + " #########"));
			if(stmt.isLabel()){
				finalCode.add(stmt);
				continue;
			}
			int numReg = lang.numRegInstr(stmt.getOperator());
			switch(numReg){
				case 3: // i.e. op DR, SR1, SR2
					allocateThreeRegInstr(stmt);
					break;
				case 2: // i.e. op DR, SR1, imm
					allocateTwoRegInstr(stmt);
					break;
				case 1:
					allocateOneRegInstr(stmt);
					break;
				default:
					finalCode.add(stmt);
			}
		}
		return finalCode;
	}
	
	private void allocateThreeRegInstr(CodeStatement stmt){
		String operator = stmt.getOperator();
		String dest = stmt.getOutputRegister();
		String leftOp = stmt.getLeftOperand();
		String rightOp = stmt.getRightOperand();
		
		
		// ld operands
		finalCode.add(lang.load("$t0", leftOp));
		finalCode.add(lang.load("$t1", rightOp));
		
		// exec instruction, output register is now $t2
		finalCode.add(new CodeStatement(operator, "$t2", "$t0", "$t1"));

		// store result
		finalCode.add(lang.store("$t2", dest));
	}
	
	private void allocateTwoRegInstr(CodeStatement stmt){
		// make sure where not looking at 3 instr
		
		String operator = stmt.getOperator();
		String op1 = stmt.getOutputRegister();
		String op2 = stmt.getLeftOperand();
		String r1 = "";
		String r2 = "";
		
		
		// load needed operands
		if(lang.resultStored(operator) && !lang.storesToLo(operator)){
			r1 = "$t0";
		}else if(op1.charAt(0) == '$'){
			r1 = op1;
		} else{
			// must load
			r1 = "$t0";
			finalCode.add(lang.load(r1, op1));
		}
		
		if(op2.charAt(0) == '$'){
			r2 = op2;
		} else if(op2.charAt(0) == '0'){
			r2 = "$0";
		} else{
			r2 = "$t1";
			finalCode.add(lang.load(r2, op2));
		}
		
		// execute command and store result
		if(stmt.getNumAddr() > 3){
			finalCode.add(new CodeStatement(operator, r1, r2, stmt.getRightOperand()));
		}
		else{
			// only three ops
			finalCode.add(new CodeStatement(operator, r1, r2));
		}
		
		
		//store results
		if(lang.resultStored(operator) && !lang.storesToLo(operator)){
				finalCode.add(lang.store(r1, op1));
			}
		
		
	}
	
	private void allocateOneRegInstr(CodeStatement stmt){
			// load op1 and execute instruction
			finalCode.add(new CodeStatement(stmt.getOperator(), "$t0", stmt.getLeftOperand()));
			// store
			finalCode.add(lang.store("$t0", stmt.getOutputRegister()));			
	
	}
	
	public void printCode(){
		for(CodeStatement stmt : finalCode){
			System.out.println(stmt.toString());
		}
	}
}