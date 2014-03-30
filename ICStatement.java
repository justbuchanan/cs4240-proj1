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

	public ICStatement(String labelName) {
		this.labelName = labelName;
	}

	public ICStatement(String op, String outReg, String operand1Reg, String operand2Reg) {
		components = new ArrayList<>();
		components.add(op);
		components.add(outReg);
		components.add(operand1Reg);
		components.add(operand2Reg);
	}

	public String toString() {
		if (isLabel()) {
			return "label: " + labelName;
		} else {
			return getOperator() + " " + getOutputRegister() + ", " + getLeftOperand() + ", " + getRightOperand();
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


	//	Label Statement
	////////////////////////////////////////////////////////////////////////////////

	public String getLabelName() {
		return labelName;
	}
}
