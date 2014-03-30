import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to create intermediate representation code for a given program
 */
public class IRCodeGenerator {
	//	track allocated names so we don't have any collisions
	private Set<String> labels;
	private Set<String> variables;

	/** 
	 * Generates the entire IR code for the program represented by @ast.  Modifies @symTab
	 * to add temporary variables, etc.
	 */
	public static ArrayList<ICStatement> generateIRCode(ParseTree ast, SymbolTable symTab) {
		IRCodeGenerator generator = new IRCodeGenerator();

		ArrayList<ICStatement> allCode = new ArrayList<>();
		try {
			generator.generateIRCodeForNode(ast.getRoot(), symTab, allCode);
		}
		catch (RuntimeException exc) {
			System.out.println("IR Code gen ERROR: " + exc);
		}
		
		return allCode;
	}

	private IRCodeGenerator() {
		labels = new HashSet<String>();
		variables = new HashSet<String>();
	}

	/**
	 * Generates ICStatements and adds them in order to @codeOut.  This is meant to be used recursively.
	 * @return the name of the register where the resulting value is placed.  may be null
	 */
	private String generateIRCodeForNode(TreeNode tree, SymbolTable symTab, ArrayList<ICStatement> codeOut) {
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

		//	TODO: handle the rest of the necessary parent symbols below

		if (parentSymbol.equals(NonTerminals.TIGER_PROGRAM)) {
			//	declaration segment
			generateIRCodeForNode(tree.getChildren().get(0), symTab, codeOut);

			//	see if there's a STAT_SEQ
			if (tree.getChildren().size() > 1) {
				//	add a main label to start the program
				String mainLabel = unique_label("main");
				if (!mainLabel.equals("main")) {
					throw new RuntimeException("Unable to allocate label 'main'");
				}
				codeOut.add(new ICStatement(mainLabel));

				//	code for STAT_SEQ
				generateIRCodeForNode(tree.getChildren().get(1), symTab, codeOut);
			}

			return null;
		} else if (parentSymbol.equals(NonTerminals.STAT_SEQ)) {
			//	generate code for each statement in series
			for (TreeNode statement : tree.getChildren()) {
				generateIRCodeForNode(statement, symTab, codeOut);
			}
			return null;
		} else if (parentSymbol.equals(NonTerminals.DECLARATION_SEGMENT)) {
			//	most of the declaration segment just adds entries to the symbol table
			// 	we need to generate code for each function AND for each variable initialization

			//	may have TYPE_DECLARATION_LIST, VAR_DECLARATION_LIST, FUNCT_DECLARATION_LIST
			for (TreeNode subtree : tree.getChildren()) {
				generateIRCodeForNode(subtree, symTab, codeOut);
			}

			return null;
		} else if (parentSymbol.equals(NonTerminals.TYPE_DECLARATION_LIST)) {
			//	nothing to do
			return null;
		} else if (parentSymbol.equals(NonTerminals.VAR_DECLARATION_LIST)) {
			for (TreeNode varDecl : tree.getChildren()) {
				generateIRCodeForNode(varDecl, symTab, codeOut);
			}
			return null;
		} else if (parentSymbol.equals(NonTerminals.VAR_DECLARATION)) {
			//	handle variable initializations

			//	VAR_DECLARATION has ID_LIST, TYPE_ID, and [INTLIT, STRLIT] children
			//	initialization is optional, so we only have to generate IR if there are 3 children
			//	(the third being the value to set the variables to)

			//	TODO: implement.  remember that array initialization needs to be handled separately
			// if (tree.getChildren().size() == 3) {
			// 	//	the value to set the variable(s) to
			// 	TreeNode constExpr = tree.getChildren().get(2);

			// 	TreeNode idListNode = tree.getChildren().get(0);
			// }

			return null;
		} else if (parentSymbol.equals(NonTerminals.FUNCT_DECLARATION_LIST)) {
			//	generate code for each function
			for (TreeNode funcDecl : tree.getChildren()) {
				generateIRCodeForNode(funcDecl, symTab, codeOut);
			}
			return null;
		} else {
			throw new RuntimeException("Don't know how to generate IR for node type: " + parentSymbol);
		}
	}

	/**
	 * Allocates and returns a new variable with the given prefix.
	 * Appends something to the end to make sure it's unique (if necessary)
	 */
	private String unique_var(String prefix) {
		return allocate_name(variables, prefix);
	}

	/**
	 * Allocates and returns a new label with the given prefix.
	 * Appends something to the end to make sure it's unique (if necessary)
	 */
	private String unique_label(String prefix) {
		return allocate_name(labels, prefix);
	}

	/**
	 * Finds a unique name beginning with @prefix and adds it to @names and returns it
	 */
	private String allocate_name(Set<String> names, String prefix) {
		//	use just the prefix if possible
		if (!names.contains(prefix)) {
			names.add(prefix);
			return prefix;
		}

		//	append a number to @prefix to make it unique
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
}
