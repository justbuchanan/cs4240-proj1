import java.util.ArrayList;

/**
 * Represents a single line in the intermediate code.
 * Can be an operation: op, y, z, x
 * OR a label
 */
public class CodeStatement {
	private ArrayList<String> components;
	private String labelName;

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

	public String toString() {
		if (isLabel()) {
			return labelName + ":";
		} else {
			String str = "";
			for (String arg : components) {
				str += arg;
				if (arg.length() > 0) {
					str += ", ";
				}
			}

			if(str.length() > 0) {
				str = str.substring(0, str.length() - 2);
				int i;
				for (i = 0; i < str.length(); i++) {
					if (str.substring(i, i + 1).equals(",")) {
						break;
					}
				}
				str = str.substring(0, i) + str.substring(i + 1, str.length());
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
