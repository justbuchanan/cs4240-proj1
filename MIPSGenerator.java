import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Stack;

/**
 * Used to create intermediate representation code for a given program
 */
public class MIPSGenerator {

	private ArrayList<ICStatement> irCode;
	private ArrayList<ICStatement> mipsCode;

	public MIPSGenerator(ArrayList<ICStatement> irCode) {
		this.irCode = irCode;
 	}

	public void generateMips() {
		for (ICStatement icStatement : irCode) {
			System.out.println(icStatement.toString());
		}
	}
}
