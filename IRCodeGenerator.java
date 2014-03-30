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
		ArrayList<ICStatement> allCode = new ArrayList<>();
		generateIRCodeForNode(ast.getRoot(), symTab, allCode);
		return allCode;
	}

	/**
	 * Generates ICStatements and adds them in order to @codeOut.  This is meant to be used recursively.
	 * @return the name of the register where the resulting value is placed.  may be null
	 */
	private static String generateIRCodeForNode(TreeNode tree, SymbolTable symTab, ArrayList<ICStatement> codeOut) {
		if (tree == null || symTab == null || codeOut == null) {
			throw new IllegalArgumentException("generateIRCodeForNode() doesn't like null args");
		}

		//	find parent symbol type
		Enum parentSymbol = null;
		if (tree.getSymbol() instanceof Token) {
			parentSymbol = ((Token)tree.getSymbol()).type();
		} else {
			parentSymbol = ((NonTerminalParserSymbol)tree.getSymbol()).getNonTerminal();
		}

		//	handle each type of parent symbol
		if (parentSymbol.equals(NonTerminals.TIGER_PROGRAM)) {
			//	TODO
			
			return null;
		} else {
			throw new RuntimeException("Don't know how to generate IR for node type: " + parentSymbol);
		}
	}
}
