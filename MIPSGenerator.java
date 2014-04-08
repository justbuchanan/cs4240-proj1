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


	public void generateMips() {
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
							
							// TODO:
						} else {
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
						}
						break;
					case "mult":
						if (isInteger(codeStatement.getRightOperand())) {
							String newVar = unique_var(codeStatement.getLeftOperand());
							mipsCode.add(new CodeStatement("addi", newVar, codeStatement.getRightOperand(), "0"));
							String newVar2 = unique_var(codeStatement.getLeftOperand());
							mipsCode.add(new CodeStatement("addi", newVar2, codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getLeftOperand(), newVar));
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement("addi", codeStatement.getLeftOperand(), newVar2, "0"));
						} else if (isInteger(codeStatement.getLeftOperand())) {
						 	String newVar = unique_var(codeStatement.getRightOperand());
							mipsCode.add(new CodeStatement("addi", newVar, codeStatement.getLeftOperand(), "0"));
							String newVar2 = unique_var(codeStatement.getRightOperand());
							mipsCode.add(new CodeStatement("addi", newVar2, codeStatement.getRightOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getRightOperand(), newVar));
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), codeStatement.getRightOperand(), "0"));
							mipsCode.add(new CodeStatement("addi", codeStatement.getRightOperand(), newVar2, "0"));	
						} else {
							String newVar = unique_var(codeStatement.getLeftOperand());
							mipsCode.add(new CodeStatement("addi", newVar, codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement(codeStatement.getOperator(), codeStatement.getLeftOperand(), codeStatement.getRightOperand()));
							mipsCode.add(new CodeStatement("addi", codeStatement.getOutputRegister(), codeStatement.getLeftOperand(), "0"));
							mipsCode.add(new CodeStatement("addi", codeStatement.getLeftOperand(), newVar, "0"));
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
