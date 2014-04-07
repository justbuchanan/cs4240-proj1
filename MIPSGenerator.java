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
 	}

	public void generateMips() {
		for (CodeStatement codeStatement : irCode) {
			System.out.println(codeStatement.toString());
		}
	}
}
