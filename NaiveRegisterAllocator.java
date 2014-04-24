import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NaiveRegisterAllocator implements RegisterAllocator{

		private ArrayList<String> storesResult;
		private ArrayList<CodeStatement> allocatedIR;
		private ArrayList<CodeStatement> conditionals;

		public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> irCode){
			allocatedIR = new ArrayList<CodeStatement>();

			if(irCode == null){
				System.out.println("GAVE NAIVE REGISTER ALLOCATOR NULL IRCODE INPUT");
				return null;
			}

			for(CodeStatement stmt : irCode){

				if((stmt == null) || ((stmt.getNumAddr() == 0) && (!stmt.isLabel()))){
					System.out.println("EMPTY STATEMENT FROM IR????");
					continue;
				}

				allocatedIR.add(new CodeStatement("####### " + stmt.toString() + " #######"));
				if(stmt.isLabel()){
					allocatedIR.add(stmt);
					continue;
				}

				String op = stmt.getOperator();
				String dest  = stmt.getOutputRegister();
				String leftOp = stmt.getLeftOperand();
				String destReg = "$t2";
				String leftReg = "$t0";

				if(storesResult(op)){

					String rightReg = "$t1";

					String rightOp = stmt.getRightOperand();
					allocatedIR.add(load(leftReg, leftOp));
					// check if we need to load right operand
					if(!isNumeric(rightOp)){
						allocatedIR.add(load(rightReg, rightOp));
					}else{
						rightReg = rightOp; // value was immediate
					}

					// exec instruction
					allocatedIR.add(new CodeStatement(op, destReg, leftReg, rightReg));
					// store result
					allocatedIR.add(store(destReg, dest));

				} else if(isConditional(op)){
					// just have to check for loads
					if(!isNumeric(dest)){
						allocatedIR.add(load(destReg, dest));
					} else{
						destReg = dest;
					}

					if(!isNumeric(leftOp)){
						allocatedIR.add(load(leftReg, leftOp));
					} else{
						leftReg = leftOp;
					}


					allocatedIR.add(new CodeStatement(op, destReg, leftReg, stmt.getRightOperand()));
				


				} else if(op.equals("assign")){

					if(stmt.getNumAddr() == 4){
						String rightOp = stmt.getRightOperand();
						// array assignment statement
						allocatedIR.add(new CodeStatement(op, destReg, leftOp, rightOp));
						allocatedIR.add(store(destReg, dest));
						continue;
					}

					// check if we need to load
					if(!isNumeric(leftOp)){
						allocatedIR.add(load(leftReg, leftOp));
					}else{
						leftReg = leftOp;
					}

					allocatedIR.add(new CodeStatement(op, destReg, leftReg));
					allocatedIR.add(store(destReg, dest));
				}
			}

			return allocatedIR;
		}

		public void printCode(){
			if(allocatedIR == null){
				System.out.println("NAIVE REG ALLOC PRODUCED NULL");
				return;
			}

			for(CodeStatement stmt : allocatedIR){
				System.out.println(stmt.toString());
			}
		}
		

		private CodeStatement store(String sr, String dest){
			return new CodeStatement("store", sr, dest);
		}

		private CodeStatement load(String dr, String src){
			return new CodeStatement("load", dr, src);
		}

		private boolean storesResult(String op){
			if(storesResult != null){
				return storesResult.contains(op);
			}

			storesResult = new ArrayList<String>();
			storesResult.add("add");
			return storesResult.contains(op);
		}


		private boolean isConditional(String s){
			if(conditionals != null){
				return condionals.contains(s);
			}

			conditionals = new ArrayList<String>();
			conditionals.add("breq");
		}

		private boolean isNumeric(String s){
			try {			
				Double.parseDouble(s);
			} catch (NumberFormatException e) {
				return false;
			}

			return true;
		}
}

