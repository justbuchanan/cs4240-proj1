import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a single line in the intermediate code.
 * Can be an operation: op, y, z, x
 * OR a label
 */
public class CodeStatement {
	private ArrayList<String> components;
	private String labelName;
	private int numbAddr;

	public boolean isLabel() {
		return labelName != null;
	}

	public boolean isEmpty() {
		return (components == null || components.size() == 0) && !isLabel();
	}

	//	empty line
	public CodeStatement() {
		components = new ArrayList<>();
	}

	//	label
	public CodeStatement(String labelName) {
		this.labelName = labelName;
	}

	public CodeStatement(String op, String op1) {
		components = new ArrayList<>();
		components.add(op);
		components.add(op1);
	}

	// 3-address code
	public CodeStatement(String op, String op1, String op2){
		components = new ArrayList<>();
		components.add(op);
		components.add(op1);
		components.add(op2);
	}

	//	4-address code
	public CodeStatement(String op, String outReg, String operand1Reg, String operand2Reg) {
		components = new ArrayList<>();
		components.add(op);
		components.add(outReg);
		components.add(operand1Reg);
		components.add(operand2Reg);
	}

	public CodeStatement(String name, String word, String firstValue, ArrayList<String> values) {
		components = new ArrayList<>();
		components.add(name);
		components.add(word);
		components.add(firstValue);
		components.addAll(values);
	}

	//	function call w/return value
	public CodeStatement(String funcName, String retValVar, ArrayList<String> params) {
		components = new ArrayList<>();
		components.add("callr");
		components.add(retValVar);
		components.add(funcName);
		components.addAll(params);
	}

	//	function call w/out return value
	public CodeStatement(String funcName, ArrayList<String> params) {
		components = new ArrayList<>();
		components.add("call");
		components.add(funcName);
		components.addAll(params);
	}

	public ArrayList<String> getFuncParams() {
		ArrayList<Integer> paramIndices = getFuncParamIndices();
		ArrayList<String> params = new ArrayList<>();
		for (Integer i : paramIndices) {
			params.add(components.get(i));
		}
		return params;
	}

	public ArrayList<Integer> getFuncParamIndices() {
		int i;
		if (getOperator().equals("call")) {
			i = 1;
		} else if (getOperator().equals("callr")) {
			i = 2;
		} else {
			throw new RuntimeException("Non-function code statement asked for function parameters");
		}

		ArrayList<Integer> paramIndices = new ArrayList<>();
		for (; i < components.size(); i++) {
			paramIndices.add(i);
		}
		return paramIndices;
	}

	public String toString() {
		if (isLabel()) {
			if(labelName.equals(".data") || labelName.equals(".text")){
				return labelName;
			}
			return labelName + ":";
		} else {
			String str = "";
			for (String arg : components) {
				str += arg;
				if (arg.length() > 0) {
					str += ", ";
				}
			}

			if (str.length() > 0) {
				str = str.substring(0, str.length() - 2);
				int i;
				for (i = 0; i < str.length(); i++) {
					if (str.substring(i, i + 1).equals(",")) {
						break;
					}
				}
				if (str.contains(",")) {
					str = str.substring(0, i) + str.substring(i + 1, str.length());
				}
				if (getOutputRegister().equals(".word")) {
					str = str.substring(0, str.indexOf(".word") + 5) + str.substring(str.indexOf(".word") + 6, str.length());
				}
			}


			return str;
		}
	}
	
	public int getNumAddr(){
		if(components == null) return 0;
		int count = 0;
		for(int i = 0; i < components.size(); i++){
			if(!"".equals(components.get(i))){
				count++;
			}
		}
		return count;
	}


	//	Operation Statement
	////////////////////////////////////////////////////////////////////////////////

	public String getOperator() {
		return components.get(0);
	}

	public String getOutputRegister() {
		return components.get(1);
	}

	public String getLeftOperand() {
		return components.get(2);
	}

	public String getRightOperand() {
		return components.get(3);
	}


	//	Label "Statement"
	////////////////////////////////////////////////////////////////////////////////

	public String getLabelName() {
		return labelName;
	}

	//	* the keys are names of variables that may exist in the receiver.
	//	  if found, these are replaced by the values in @varMapping
	//	* used by the register allocator when it assigns a variable to a register
	public void replaceVariableOccurrences(Map<String, String> varMapping) {
		if (varMapping == null) {
			throw new IllegalArgumentException("Give a non-null varMapping");
		}

		for (Integer i : getVariableIndices()) {
			if (varMapping.containsKey(components.get(i))) {
				components.set(i, varMapping.get(components.get(i)));
			}
		}
	}

	//	returns the indices into @components where there are variables
	public ArrayList<Integer> getVariableIndices() {
		ArrayList<Integer> varIndices = new ArrayList<>();

		if (!isLabel() && !isEmpty()) {

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


			String operator = getOperator();

			if (operator.equals("assign")) {
				if (getRightOperand().equals("")) {
					//	this is an assignment to a regular variable
					varIndices.add(1);
				} else {
					//	this is an assignment to set the whole contents of an array
					//	FIXME: is this a def?  arrays are different...
				}
			} else if (operator.equals("return")) {
				if (getOutputRegister().equals("")) {
					//	return operation with no ret val
				} else {
					//	return a value
					varIndices.add(1);
				}
			} else if (operator.equals("call")) {
				varIndices.addAll(getFuncParamIndices());
			} else if (operator.equals("callr")) {
				varIndices.add(1);
				varIndices.addAll(getFuncParamIndices());
			} else if (condBranchOps.contains(getOperator())) {
				//	these are of the form:
				//	condBranchOp var1, var2, gotoLabel
				//	we add var1 and var2 to the use set

				varIndices.add(1);
				varIndices.add(2);
			} else {
				//	general case: "op outVar, inVar1, inVar2"
				varIndices.add(1);
				varIndices.add(2);
				varIndices.add(3);
			}


			//	remove any CONST numbers from @useOut
			Iterator<Integer> itr = varIndices.iterator();
			while (itr.hasNext()) {
				String var = components.get(itr.next());
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

		return varIndices;
	}


	//	analysis

	/**
	 * Examines the given code statement and finds the variables defined and used, returning them through the output variables.
	 */
	public void getVariableDefsAndUses(Set<String> defOut, Set<String> useOut) {
		if (defOut == null || useOut == null) {
			throw new IllegalArgumentException("Parameters should not be null");
		}


		if (!isLabel() && !isEmpty()) {

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


			String operator = getOperator();

			if (operator.equals("assign")) {
				if (getRightOperand().equals("")) {
					//	this is an assignment to a regular variable
					defOut.add(getOutputRegister());
				} else {
					//	this is an assignment to set the whole contents of an array
					//	FIXME: is this a def?  arrays are different...
				}
			} else if (operator.equals("return")) {
				if (getOutputRegister().equals("")) {
					//	return operation with no ret val
				} else {
					//	return a value
					useOut.add(getOutputRegister());
				}
			} else if (operator.equals("call")) {
				useOut.addAll(getFuncParams());
			} else if (operator.equals("callr")) {
				defOut.add(getOutputRegister());
				useOut.addAll(getFuncParams());
			} else if (condBranchOps.contains(getOperator())) {
				//	these are of the form:
				//	condBranchOp var1, var2, gotoLabel
				//	we add var1 and var2 to the use set

				//	yes, the method names are wierd - they were named for the general case, which isn't this case
				useOut.add(getOutputRegister());
				useOut.add(getLeftOperand());
			} else {
				//	general case: "op outVar, inVar1, inVar2"
				defOut.add(getOutputRegister());
				useOut.add(getLeftOperand());
				useOut.add(getRightOperand());
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
}
