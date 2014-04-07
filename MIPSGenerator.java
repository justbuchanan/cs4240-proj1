import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Stack;

/**
 * Used to create intermediate representation code for a given program
 */
public class MIPSGenerator {

	private ArrayList<CodeStatement> irCode;
	private ArrayList<CodeStatement> mipsCode;

	public MIPSGenerator(ArrayList<CodeStatement> irCode) {
		this.irCode = irCode;
		mipsCode = new ArrayList<CodeStatement>();
 	}

	private static boolean isInteger(String string) {
		try {			
			Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	public void generateMips() {
		for (CodeStatement codeStatement : irCode) {
			System.out.println(codeStatement.toString());
			if (codeStatement.isLabel()) {
				mipsCode.add(new CodeStatement(codeStatement.getLabelName()));
			} else if (codeStatement.toString().length() > 0) {
				switch (codeStatement.getOperator()) {
					case "add":
						if (isInteger(codeStatement.getRightOperand())) {
							mipsCode.add(new CodeStatement("addi", "$" + codeStatement.getOutputRegister(), "$" + codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						} else if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), "$" + codeStatement.getOutputRegister(), "$" + codeStatement.getRightOperand(), codeStatement.getLeftOperand()));
						} else { 
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), "$" + codeStatement.getOutputRegister(), "$" + codeStatement.getLeftOperand(), "$" + codeStatement.getRightOperand()));
						}
						break;
					case "sub":
						if (isInteger(codeStatement.getRightOperand())) {
							mipsCode.add(new CodeStatement("addi", "$" + codeStatement.getOutputRegister(), "$" + codeStatement.getLeftOperand(), "-" + codeStatement.getRightOperand()));
						} else if(isInteger(codeStatement.getLeftOperand())) {
							// TODO:
						} else {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), "$" + codeStatement.getOutputRegister(), "$" + codeStatement.getLeftOperand(), "$" + codeStatement.getRightOperand()));
						}
						break;
					
				}
			}
		}
	}

	public String toString() {
		String returnString = "";

		for (CodeStatement codeStatement : mipsCode) {
			returnString += codeStatement.toString() + "\n";
		}

		return returnString;
	}
}
