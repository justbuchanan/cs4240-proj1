import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

/**
 * Used to create intermediate representation code for a given program
 */
public class IRCodeGenerator {
	//	track allocated names so we don't have any collisions
	private Set<String> labels;
	private Set<String> variables;
	private SymbolTable symbolTable;

	/** 
	 * Generates the entire IR code for the program represented by @ast.  Modifies @symbolTable
	 * to add temporary variables, etc.
	 */
	public static ArrayList<ICStatement> generateIRCode(ParseTree ast, SymbolTable symbolTable) {
		IRCodeGenerator generator = new IRCodeGenerator(symbolTable);

		ArrayList<ICStatement> allCode = new ArrayList<>();
		try {
			generator.generateIRCodeForNode(ast.getRoot(), allCode);
		}
		catch (RuntimeException exc) {
			System.out.println("IR Code gen ERROR: " + exc);
		}
		
		return allCode;
	}

	private IRCodeGenerator(SymbolTable symbolTable) {
		labels = new HashSet<String>();
		variables = new HashSet<String>();
		this.symbolTable = symbolTable;
	}

	/**
	 * Generates ICStatements and adds them in order to @codeOut.  This is meant to be used recursively.
	 * @return the name of the register where the resulting value is placed.  may be null
	 */
	private String generateIRCodeForNode(TreeNode tree, ArrayList<ICStatement> codeOut) {
		if (tree == null || symbolTable == null || codeOut == null) {
			throw new IllegalArgumentException("generateIRCodeForNode() doesn't like null args");
		}

		//	find parent symbol type
		Enum parentSymbol = null;
		if (tree.getSymbol() instanceof Token) {
			parentSymbol = ((Token)tree.getSymbol()).type();
		} else {
			parentSymbol = ((NonTerminalParserSymbol)tree.getSymbol()).getNonTerminal();
		}

		ArrayList<Enum> binaryOps = new ArrayList<Enum>(
			Arrays.asList(new Enum[]{
				State.PLUS,
				State.MINUS,
				State.MULT,
				State.DIV,
				State.EQ,
				State.NEQ,
				State.GREATER,
				State.LESSER,
				State.GREATEREQ,
				State.LESSEREQ
			})
		);

		//	TODO: handle the rest of the necessary parent symbols below

		if (parentSymbol.equals(NonTerminals.TIGER_PROGRAM)) {
			//	declaration segment
			generateIRCodeForNode(tree.getChildren().get(0), codeOut);

			//	see if there's a STAT_SEQ
			if (tree.getChildren().size() > 1) {
				//	add a main label to start the program
				String mainLabel = unique_label("main");
				if (!mainLabel.equals("main")) {
					throw new RuntimeException("Unable to allocate label 'main'");
				}
				codeOut.add(new ICStatement(mainLabel));

				//	code for STAT_SEQ
				generateIRCodeForNode(tree.getChildren().get(1), codeOut);
			}

			return null;
		} else if (parentSymbol.equals(NonTerminals.STAT_SEQ)) {
			//	generate code for each statement in series
			for (TreeNode statement : tree.getChildren()) {
				generateIRCodeForNode(statement, codeOut);
			}
			return null;
		} else if (parentSymbol.equals(NonTerminals.DECLARATION_SEGMENT)) {
			//	most of the declaration segment just adds entries to the symbol table
			// 	we need to generate code for each function AND for each variable initialization

			//	may have TYPE_DECLARATION_LIST, VAR_DECLARATION_LIST, FUNCT_DECLARATION_LIST
			for (TreeNode subtree : tree.getChildren()) {
				generateIRCodeForNode(subtree, codeOut);
			}

			return null;
		} else if (parentSymbol.equals(NonTerminals.TYPE_DECLARATION_LIST)) {
			//	nothing to do
			return null;
		} else if (parentSymbol.equals(NonTerminals.VAR_DECLARATION_LIST)) {
			for (TreeNode varDecl : tree.getChildren()) {
				generateIRCodeForNode(varDecl, codeOut);
			}
			return null;
		} else if (parentSymbol.equals(NonTerminals.VAR_DECLARATION)) {
			//	handle variable initializations

			//	VAR_DECLARATION has ID_LIST, TYPE_ID, and [INTLIT, STRLIT] children
			//	initialization is optional, so we only have to generate IR if there are 3 children
			//	(the third being the value to set the variables to)

			if (tree.getChildren().size() == 3) {
				//	the value to set the variable(s) to
				TreeNode constExpr = tree.getChildren().get(2);

				TreeNode idListNode = tree.getChildren().get(0);
				idListNode.assertNodeType(NonTerminals.ID_LIST);
				for (TreeNode idNode : idListNode.getChildren()) {
					idNode.assertNodeType(State.ID);
					String varName = ((Token)idNode.getSymbol()).value();

					VarSymbolEntry varEntry = symbolTable.getVar(varName);
					TypeSymbolEntry varType = varEntry.getType();

					//	TODO: initialize the variable
					//	TODO: it'll be different for arrays and for strings
				}
			}

			return null;
		} else if (parentSymbol.equals(NonTerminals.FUNCT_DECLARATION_LIST)) {
			//	generate code for each function
			for (TreeNode funcDecl : tree.getChildren()) {
				generateIRCodeForNode(funcDecl, codeOut);
			}
			return null;
		} else if (binaryOps.contains(parentSymbol)) {
			String resultVar = unique_var("t");

			//	get the op code for this operator
			String opCode = null;
			if (parentSymbol.equals(State.PLUS)) opCode = "add";
			else if (parentSymbol.equals(State.MINUS)) opCode = "sub";
			else if (parentSymbol.equals(State.MULT)) opCode = "mult";
			else if (parentSymbol.equals(State.DIV)) opCode = "div";
			else if (parentSymbol.equals(State.AND)) opCode = "and";
			else if (parentSymbol.equals(State.OR)) opCode = "or";
			else {
				//	FIXME: implement comparison operators
				throw new RuntimeException("Don't know how to make opcode for: " + parentSymbol);
			}

			//	evaluate subexpressions
			String leftArgVar = generateIRCodeForNode(tree.getChildren().get(0), codeOut);
			String rightArgVar = generateIRCodeForNode(tree.getChildren().get(1), codeOut);

			//	add the code for this operation
			codeOut.add(new ICStatement(opCode, resultVar, leftArgVar, rightArgVar));

			return resultVar;
		} else if (parentSymbol.equals(State.ID)) {
			//	return the variable name
			return ((Token)tree.getSymbol()).value();
		} else if (parentSymbol.equals(State.ASSIGN)) {
			TreeNode lvalue = tree.getChildren().get(0);
			TreeNode rvalue = tree.getChildren().get(1);

			String valueVariable = generateIRCodeForNode(rvalue, codeOut);

			//	depends on what type of lvalue we have
			if (lvalue.isNodeType(State.ID)) {
				//	assignment to a named variable
				String lvalueVarName = ((Token)lvalue.getSymbol()).value();
				codeOut.add(new ICStatement("assign", lvalueVarName, valueVariable, ""));
			} else if (lvalue.isNodeType(NonTerminals.ARRAY_LOOKUP)) {
				//	FIXME: handle array case
				throw new RuntimeException("Unhandled case in ASSIGN code generator");
			} else {
				throw new RuntimeException("Unhandled case in ASSIGN code generator");
			}

			//	assignment operation evaluates to the value that was assigned
			return valueVariable;
		} else if (parentSymbol.equals(State.STRLIT)) {
			//	FIXME: does this work?
			return ((Token)tree.getSymbol()).value();
		} else if (parentSymbol.equals(NonTerminals.FUNCTION_CALL)) {
			String funcName = ((Token)tree.getChildren().get(0).getSymbol()).value();

			//	build param list
			ArrayList<String> params = new ArrayList<>();
			for (int i = 1; i < tree.getChildren().size(); i++) {
				String param = generateIRCodeForNode(tree.getChildren().get(i), codeOut);
				params.add(param);
			}

			FuncSymbolEntry funcEntry = symbolTable.getFunc(funcName);
			if (funcEntry.isVoid()) {
				codeOut.add(new ICStatement(funcName, params));
				return null;
			} else {
				String retValVar = unique_var("retVal");
				codeOut.add(new ICStatement(funcName, retValVar, params));
				return retValVar;
			}
		} else {
			throw new RuntimeException("Don't know how to generate IR for node type: " + parentSymbol);
		}
	}

	/**
	 * Allocates and returns a new variable with the given prefix.
	 * Appends something to the end to make sure it's unique (if necessary)
	 * Also adds the variable to the symbol table as an "int"
	 */
	private String unique_var(String prefix) {
		String varName = allocate_name(variables, prefix);
		symbolTable.addVar(varName, "int");
		return varName;
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
