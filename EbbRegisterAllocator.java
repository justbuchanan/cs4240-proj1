import java.util.Arrays;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * A register-allocator based on extended basic blocks
 */
public class EbbRegisterAllocator implements RegisterAllocator {
	private int registerCount;
	private ArrayList<CodeStatement> finalCode;
	
	public EbbRegisterAllocator() {
		this.registerCount = 30;	//	FIXME: set registerCount
	}

	public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> irCode) {
		ControlFlowGraph cfg = new ControlFlowGraph(irCode);

		for (ControlFlowGraph.ExtendedBasicBlock ebb : cfg.getExtendedBasicBlocks()) {
			//	FIXME: def-use chains

			for (ControlFlowGraph.BasicBlock bb : ebb.getBasicBlocks()) {
				//	TODO
			}
		}

		//	TODO

		return finalCode;
	}

	/**
	 * Examines the given code statement and finds the variables defined and used, returning them through the output variables.
	 */
	public void getVariableDefsAndUses(CodeStatement stmt, Set<String> defOut, Set<String> useOut) {
		if (stmt == null || defOut == null || useOut == null) {
			throw new IllegalArgumentException("Parameters should not be null");
		}


		if (!stmt.isLabel() && !stmt.isEmpty()) {

			ArrayList<String> condBranchOps = new ArrayList<String>(
				Arrays.asList(new String[]{
					"breq",
					"brneq",
					"brlt",
					"brgt",
					"brgeq",
					"brleq"
				})
			);


			String operator = stmt.getOperator();

			if (operator.equals("assign")) {
				if (stmt.getRightOperand().equals("")) {
					//	this is an assignment to a regular variable
					defOut.add(stmt.getOutputRegister());
				} else {
					//	this is an assignment to set the whole contents of an array
					//	FIXME: is this a def?  arrays are different...
				}
			} else if (operator.equals("return")) {
				if (stmt.getOutputRegister().equals("")) {
					//	return operation with no ret val
				} else {
					//	return a value
					useOut.add(stmt.getOutputRegister());
				}
			} else if (operator.equals("call")) {
				useOut.addAll(stmt.getFuncParams());
			} else if (operator.equals("callr")) {
				defOut.add(stmt.getOutputRegister());
				useOut.addAll(stmt.getFuncParams());
			} else if (condBranchOps.contains(stmt.getOperator())) {
				//	these are of the form:
				//	condBranchOp var1, var2, gotoLabel
				//	we add var1 and var2 to the use set

				//	yes, the method names are wierd - they were named for the general case, which isn't this case
				useOut.add(stmt.getOutputRegister());
				useOut.add(stmt.getLeftOperand());
			} else {
				//	general case: "op outVar, inVar1, inVar2"
				defOut.add(stmt.getOutputRegister());
				useOut.add(stmt.getLeftOperand());
				useOut.add(stmt.getRightOperand());
			}


			//	remove any CONST numbers from @useOut
			Iterator<String> itr = useOut.iterator();
			while (itr.hasNext()) {
				String var = itr.next();
				char firstChar = var.charAt(0);
				if ('0' <= firstChar && firstChar <= '9') {
					itr.remove();
				}
			}
		}
	}

	// private class VarAccess {
	// 	public String varName;
	// 	public AccessType accessType;

	// 	public enum AccessType {
	// 		AccessTypeUse,
	// 		AccessTypeDef
	// 	};

	// 	public VarAccess(AccessType accessType, String varName) {
	// 		this.accessType = accessType;
	// 		this.varName = varName;
	// 	}
	// }


	public void printCode() {
		for(CodeStatement stmt : finalCode){
			System.out.println(stmt.toString());
		}
	}
}
