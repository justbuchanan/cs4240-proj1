import java.util.ArrayList;

/**
 * Represents a single line in the intermediate code.
 * Can be an operation: op, y, z, x
 * OR a label
 */
public class ICStatement {
	private ArrayList<String> components;
	private String labelName;

	public boolean isLabel() {
		return labelName != null;
	}

	//	empty line
	public ICStatement() {
		components = new ArrayList<>();
	}

	//	label
	public ICStatement(String labelName) {
		this.labelName = labelName;
	}

	//	4-address code
	public ICStatement(String op, String outReg, String operand1Reg, String operand2Reg) {
		components = new ArrayList<>();
		components.add(op);
		components.add(outReg);
		components.add(operand1Reg);
		components.add(operand2Reg);
	}

	//	function call w/return value
	public ICStatement(String funcName, String retValVar, ArrayList<String> params) {
		components = new ArrayList<>();
		components.add("callr");
		components.add(retValVar);
		components.add(funcName);
		components.addAll(params);
	}

	//	function call w/out return value
	public ICStatement(String funcName, ArrayList<String> params) {
		components = new ArrayList<>();
		components.add("call");
		components.add(funcName);
		components.addAll(params);
	}

	public String toString() {
		if (isLabel()) {
			return labelName + ":";
		} else {
			String str = "";
			for (String arg : components) {
				str += arg + ", ";
			}
			return str;
		}
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
}
