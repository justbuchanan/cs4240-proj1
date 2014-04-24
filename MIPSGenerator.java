import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Stack;

public class MIPSGenerator {

	private ArrayList<CodeStatement> irCode;
	public ArrayList<CodeStatement> mipsCode; //TODO: MADE PUBLIC FOR DEBUGGING, FIND BETTER WAY
											// TO INCORPORATE REGISTER ALLOCATION
	private Set<String> variables;
	private SymbolTable symbolTable;

	public MIPSGenerator(ArrayList<CodeStatement> irCode, SymbolTable symbolTable) {
		this.irCode = irCode;
		mipsCode = new ArrayList<CodeStatement>();

		variables = new HashSet<String>();
        variables.addAll(symbolTable.getAllVarNames());
		
		this.symbolTable = symbolTable;
 	}

	private static boolean isInteger(String string) {
		try {			
			Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	private void generateDataSegment() {
		for (String varName : symbolTable.getAllVarNames()) {
			VarSymbolEntry var = symbolTable.getVar(varName);
			TypeSymbolEntry type = var.getType();

			int numWords = 1;
			if (type.getArrDims() != null) {
				for (Integer dim : type.getArrDims()) {
					numWords *= dim;
				}
			}

			String staticData = "0";
			for (int i = 1; i < numWords; i++) {
				staticData += ", 0";
			}

			mipsCode.add(0, new CodeStatement(varName + ":", ".word", staticData));
		}
		mipsCode.add(0, new CodeStatement(".data"));
	}

	public void generateMips() {
		mipsCode.add(new CodeStatement(".text"));
		for (CodeStatement codeStatement : irCode) {
			if (codeStatement.isLabel()) {
				mipsCode.add(new CodeStatement(codeStatement.getLabelName()));
			} else if (codeStatement.toString().length() > 0) {
				switch (codeStatement.getOperator()) {
					case "add":
						if (isInteger(codeStatement.getRightOperand())) {
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						} else if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getOutputRegister(), codeStatement.getRightOperand(), codeStatement.getLeftOperand()));
						} else {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
						break;
					case "sub":
						if (isInteger(codeStatement.getRightOperand())) {
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), "-" + codeStatement.getRightOperand()));
						} else if(isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getOutputRegister(), "$fp", codeStatement.getRightOperand()));
						} else {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
						break;
					case "mult":
						if (isInteger(codeStatement.getRightOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", codeStatement.getRightOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getLeftOperand(), "$fp"));
							mipsCode.add(new CodeStatement("mflo", codeStatement.getOutputRegister()));
						} else if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), "$fp", codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("mflo", codeStatement.getOutputRegister()));
						} else {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("mflo", codeStatement.getOutputRegister()));
						}
						break;
					case "div":
						if (isInteger(codeStatement.getRightOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", codeStatement.getRightOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getLeftOperand(), "$fp"));
							mipsCode.add(new CodeStatement("mflo", codeStatement.getOutputRegister()));
						} else if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), "$fp", codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("mflo", codeStatement.getOutputRegister()));
						} else {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), "$LO", "0"));
						}
						break;
					case "or":
						mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						break;
					case "and":
						mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						break;
					case "goto":
						mipsCode.add(new CodeStatement("j", codeStatement.getOutputRegister()));
						break;
					case "assign":
						if (codeStatement.getRightOperand().length() == 0) {
							if (isInteger(codeStatement.getLeftOperand())) {
								mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), "$0", codeStatement.getLeftOperand()));
							} else {
								mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), "0"));
							}
						}
						break;
					case "array_load":
						mipsCode.add(new CodeStatement("la", "$fp", codeStatement.getLeftOperand()));
						mipsCode.add(new CodeStatement("addi", "$sp", codeStatement.getRightOperand(), "0"));
						mipsCode.add(new CodeStatement("add", "$sp", "$sp", "$sp"));
						mipsCode.add(new CodeStatement("add", "$sp", "$sp", "$sp"));
						mipsCode.add(new CodeStatement("add", "$sp", "$sp", "$fp"));
						mipsCode.add(new CodeStatement("lw", codeStatement.getOutputRegister(), "0($fp)"));
						break;
					case "array_store":
						mipsCode.add(new CodeStatement("la", "$fp", codeStatement.getOutputRegister()));
						if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", "$fp", codeStatement.getLeftOperand()));
						} else {
							mipsCode.add(new CodeStatement("add", "$fp", "$fp", codeStatement.getLeftOperand()));
						}
						if (isInteger(codeStatement.getRightOperand())) {
							mipsCode.add(new CodeStatement("addi", "$sp", "$0", codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("sw", "$sp", "0($fp)"));
						} else {
							mipsCode.add(new CodeStatement("sw", codeStatement.getRightOperand(), "0($fp)"));
						}
						break;
					case "load":
						//load register, label
						mipsCode.add(new CodeStatement("la", "$fp", codeStatement.getLeftOperand()));
						mipsCode.add(new CodeStatement("lw", codeStatement.getOutputRegister(), "0($fp)"));
						break;
					case "store":
						mipsCode.add(new CodeStatement("la", "$fp", codeStatement.getLeftOperand()));
						mipsCode.add(new CodeStatement("sw", codeStatement.getOutputRegister(), "0($fp)"));
						//store label, register
						break;
					case "breq":
						if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", "$0", codeStatement.getLeftOperand()));
							mipsCode.add(new CodeStatement("beq", codeStatement.getOutputRegister(), "$fp", codeStatement.getRightOperand()));
						} else {
							mipsCode.add(new CodeStatement("beq", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
						break;
					case "brneq":
						if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", "$0", codeStatement.getLeftOperand()));
							mipsCode.add(new CodeStatement("bne", codeStatement.getOutputRegister(), "$fp", codeStatement.getRightOperand()));
						} else {
							mipsCode.add(new CodeStatement("bne", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
					case "brlt":
						if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", "$0", codeStatement.getLeftOperand()));
							mipsCode.add(new CodeStatement("bltz", codeStatement.getOutputRegister(), "$fp", codeStatement.getRightOperand()));
						} else {
							mipsCode.add(new CodeStatement("bltz", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
					case "brgt":
						if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", "$0", codeStatement.getLeftOperand()));
							mipsCode.add(new CodeStatement("bgtz", codeStatement.getOutputRegister(), "$fp", codeStatement.getRightOperand()));
						} else {
							mipsCode.add(new CodeStatement("bgtz", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
					case "brleq":
						if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", "$0", codeStatement.getLeftOperand()));
							mipsCode.add(new CodeStatement("blez", codeStatement.getOutputRegister(), "$fp", codeStatement.getRightOperand()));
						} else {
							mipsCode.add(new CodeStatement("blez", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
					case "brgeq":
						if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", "$fp", "$0", codeStatement.getLeftOperand()));
							mipsCode.add(new CodeStatement("bgez", codeStatement.getOutputRegister(), "$fp", codeStatement.getRightOperand()));
						} else {
							mipsCode.add(new CodeStatement("bgez", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
				}
			}
		}
		generateDataSegment();
		
		mipsCode.add(new CodeStatement("jr", "$ra"));
	}
	
	public String toString() {
		String returnString = "";

		for (CodeStatement codeStatement : mipsCode) {
			returnString += codeStatement.toString() + "\n";
		}

		return returnString;
	}
}
