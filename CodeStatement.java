import java.util.ArrayList;

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

	//	Returns values in array
	public ArrayList<String> getArrayValues() {
		ArrayList<String> returnList = new ArrayList<>();
		for (int i = 2; i < components.size(); i++) {
			returnList.add(components.get(i));
		}

		return returnList;
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
				str = str.substring(0, i) + str.substring(i + 1, str.length());
				if (getOutputRegister().equals(".word")) {
					str = str.substring(0, str.indexOf(".word") + 5) + str.substring(str.indexOf(".word") + 6, str.length());
				}
			}


			return str;
		}
	}
	
	public int getNumAddr(){
		return components.size();
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
