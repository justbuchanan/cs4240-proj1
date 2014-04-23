import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;
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
		this.finalCode = new ArrayList<>();
	}

	public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> irCode) {
		boolean debug = true;

		ControlFlowGraph cfg = new ControlFlowGraph(irCode);

		//	defs and uses indexed by BB index
		ArrayList<Set<String>> bbDefs = new ArrayList<>();
		ArrayList<Set<String>> bbUses = new ArrayList<>();

		//	find defs and uses for all the basic blocks
		for (int i = 0; i < cfg.getBasicBlocks().size(); i++) {
			Set<String> defs = new HashSet<>();
			Set<String> uses = new HashSet<>();

			BasicBlock bb = cfg.getBasicBlocks().get(i);

			for (CodeStatement stmt : bb.getCode()) {
				getVariableDefsAndUses(stmt, defs, uses);
			}

			bbDefs.add(defs);
			bbUses.add(uses);
		}


		//	debug print defs and uses
		if (debug) {
			for (int i = 0; i < bbDefs.size(); i++) {
				System.out.println("BasicBlock" + i + ": defs=" + bbDefs.get(i) + "; uses=" + bbUses.get(i));
			}
			System.out.println();
		}






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
				if (var.length() == 0) {
					itr.remove();
				} else {
					char firstChar = var.charAt(0);
					if ('0' <= firstChar && firstChar <= '9') {
						itr.remove();
					}
				}
			}
		}
	}


	public void printCode() {
		for(CodeStatement stmt : finalCode){
			System.out.println(stmt.toString());
		}
	}
}
