import java.util.ArrayList;

/**
 * Used to create intermediate representation code for a given program
 */
public class IRCodeGenerator {

	/** 
	 * Generates the entire IR code for the program represented by @ast.  Modifies @symTab
	 * to add temporary variables, etc.
	 */
	public static ArrayList<ICStatement> generateIRCode(ParseTree ast, SymbolTable symTab) {

	}

	/**
	 * Generates ICStatements and adds them in order to @codeOut.
	 * @return the name of the register where the resulting value is placed.  may be null
	 */
	private String generateIRCodeForNode(TreeNode tree, ArrayList<ICStatement> codeOut) {
		
	}
}
