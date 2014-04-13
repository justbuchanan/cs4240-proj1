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

	private String unique_var(String prefix) {
        	String varName = allocate_name(variables, prefix);
                symbolTable.addVar(varName, "int");
                return varName;
        }
 
        private String allocate_name(Set<String> names, String prefix) {
        	//      use just the prefix if possible
                if (!names.contains(prefix)) {
                	names.add(prefix);
                        return prefix;
                }
 
                //      append a number to @prefix to make it unique
                int suffix = 1;
                while (true) {
                        String newName = prefix + suffix;
                        if (!names.contains(newName)) {
                                names.add(newName);
                                return newName;
                        }
                        suffix++;
                }
	}

	private void generateDataSegment() {
		Set<String> set = new HashSet<String>();
		for (CodeStatement codeStatement : irCode) {
			if (!codeStatement.isLabel() && codeStatement.toString().length() > 0 && codeStatement.getOperator().equals("assign") && codeStatement.getRightOperand().length() > 0 && !set.contains(codeStatement.getOutputRegister())) {
				ArrayList<String> valueList = new ArrayList<>();
				for (int i = 0; i < (Integer.parseInt(codeStatement.getLeftOperand()) - 1); i++) {
					valueList.add(codeStatement.getRightOperand());
				}
				mipsCode.add(new CodeStatement(codeStatement.getOutputRegister() + ":", ".word", codeStatement.getRightOperand(), valueList));
				set.add(codeStatement.getOutputRegister());
			} else if (!codeStatement.isLabel() && codeStatement.toString().length() > 0 && codeStatement.getOperator().equals("assign") && !set.contains(codeStatement.getOutputRegister())) {
				mipsCode.add(new CodeStatement(codeStatement.getOutputRegister() + ":", ".word", codeStatement.getLeftOperand()));
				set.add(codeStatement.getOutputRegister());
			}		
		}
	}

	public void generateMips() {
		mipsCode.add(new CodeStatement(".data"));
		generateDataSegment();
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
							String newVar = unique_var(codeStatement.getRightOperand());
							mipsCode.add(new CodeStatement("addi", newVar, codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getOutputRegister(), newVar, codeStatement.getRightOperand()));	
						} else {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
						break;
					case "mult":
						if (isInteger(codeStatement.getRightOperand())) {
							String newVar = unique_var(codeStatement.getLeftOperand());
							mipsCode.add(new CodeStatement("addi", newVar, codeStatement.getRightOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getLeftOperand(), newVar));
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), "$LO", "0"));
						} else if (isInteger(codeStatement.getLeftOperand())) {
						 	String newVar = unique_var(codeStatement.getRightOperand());
							mipsCode.add(new CodeStatement("addi", newVar, codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), newVar, codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), "$LO", "0"));
						} else {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), "$LO", "0"));
						}
						break;
					case "div":
						if (isInteger(codeStatement.getRightOperand())) {
							String newVar = unique_var(codeStatement.getLeftOperand());
							mipsCode.add(new CodeStatement("addi", newVar, codeStatement.getRightOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getLeftOperand(), newVar));
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), "$LO", "0"));
						} else if (isInteger(codeStatement.getLeftOperand())) {
						 	String newVar = unique_var(codeStatement.getRightOperand());
							mipsCode.add(new CodeStatement("addi", newVar, codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), newVar, codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), "$LO", "0"));
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
						String newVar = unique_var(codeStatement.getLeftOperand());
						mipsCode.add(new CodeStatement("la", newVar, codeStatement.getLeftOperand()));
						if (isInteger(codeStatement.getRightOperand())) {
							mipsCode.add(new CodeStatement("addi", newVar, newVar, codeStatement.getRightOperand()));
						} else {
							mipsCode.add(new CodeStatement("add", newVar, newVar, codeStatement.getRightOperand()));
						}
						mipsCode.add(new CodeStatement("lw", codeStatement.getOutputRegister(), "0(" + newVar + ")"));
						break;
					case "array_store":
						String newVar2 = unique_var(codeStatement.getOutputRegister());
						mipsCode.add(new CodeStatement("la", newVar2, codeStatement.getOutputRegister()));
						if (isInteger(codeStatement.getLeftOperand())) {
							mipsCode.add(new CodeStatement("addi", newVar2, newVar2, codeStatement.getLeftOperand()));
						} else {
							mipsCode.add(new CodeStatement("add", newVar2, newVar2, codeStatement.getLeftOperand()));
						}
						String newVar3 = unique_var(codeStatement.getOutputRegister());
						if (isInteger(codeStatement.getRightOperand())) {
							mipsCode.add(new CodeStatement("addi", newVar3, "$0", codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("sw", newVar3, "0(" + newVar2 + ")"));
						} else {
							mipsCode.add(new CodeStatement("sw", codeStatement.getRightOperand(), "0(" + newVar2 + ")"));
						}
						break;
					case "breq":
						mipsCode.add(new CodeStatement("beq", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						break;
					case "brneq":
						mipsCode.add(new CodeStatement("bne", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
					case "brlt":
						mipsCode.add(new CodeStatement("bltz", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
					case "brgt":
						mipsCode.add(new CodeStatement("bgtz", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
					case "brleq":
						mipsCode.add(new CodeStatement("blez", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
					case "brgeq":
						mipsCode.add(new CodeStatement("bgez", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
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
