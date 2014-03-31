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

		//	build unique var name set, taking into account what variables already exist
		variables = new HashSet<String>();
		variables.addAll(symbolTable.getAllVarNames());

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
			})
		);

		ArrayList<Enum> compareOps = new ArrayList<Enum>(
			Arrays.asList(new Enum[]{
				State.EQ,
				State.NEQ,
				State.GREATER,
				State.LESSER,
				State.GREATEREQ,
				State.LESSEREQ
			})
		);


		if (parentSymbol.equals(NonTerminals.TIGER_PROGRAM)) {
			//	declaration segment
			TreeNode declSeg = tree.getChildren().get(0);

			TreeNode varDeclList = null;
			TreeNode funcDeclList = null;
			for (TreeNode declList : declSeg.getChildren()) {
				if (declList.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION_LIST))) {
					varDeclList = declList;
				} else if (declList.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION_LIST))) {
					funcDeclList = declList;
				}
			}

			//	do function declarations first
			if (funcDeclList != null) generateIRCodeForNode(funcDeclList, codeOut);

			//	add a main label to start the program
			String mainLabel = unique_label("main");
			if (!mainLabel.equals("main")) {
				throw new RuntimeException("Unable to allocate label 'main'");
			}
			codeOut.add(new ICStatement());
			codeOut.add(new ICStatement(mainLabel));

			//	variable declarations
			if (varDeclList != null) {
				generateIRCodeForNode(varDeclList, codeOut);
				codeOut.add(new ICStatement());	//	newline
			}

			//	see if there's a STAT_SEQ
			if (tree.getChildren().size() > 1) {
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
				String constExprVar = generateIRCodeForNode(constExpr, codeOut);

				//	initialize each variable in the list
				TreeNode idListNode = tree.getChildren().get(0);
				idListNode.assertNodeType(NonTerminals.ID_LIST);
				for (TreeNode idNode : idListNode.getChildren()) {
					idNode.assertNodeType(State.ID);
					String varName = ((Token)idNode.getSymbol()).value();

					VarSymbolEntry varEntry = symbolTable.getVar(varName);
					TypeSymbolEntry varType = varEntry.getType();

					//	see if it's an array
					if (varType.getArrDims() != null && varType.getArrDims().size() > 0) {
						//	array initialization

						int arrSize = 1;
						for (Integer dim : varType.getArrDims()) {
							arrSize *= dim;
						}

						codeOut.add(new ICStatement("assign", varName, Integer.toString(arrSize), constExprVar));
					} else {
						//	value initialization
						codeOut.add(new ICStatement("assign", varName, constExprVar, ""));
					}
				}
			}

			return null;
		} else if (parentSymbol.equals(NonTerminals.FUNCT_DECLARATION_LIST)) {
			//	generate code for each function
			for (TreeNode funcDecl : tree.getChildren()) {
				generateIRCodeForNode(funcDecl, codeOut);
			}
			return null;
		} else if (parentSymbol.equals(NonTerminals.FUNCT_DECLARATION)) {
			String funcName = ((Token)tree.getChildren().get(0).getSymbol()).value();
			String labelName = unique_label("func_" + funcName);

			FuncSymbolEntry funcEntry = symbolTable.getFunc(funcName);

			codeOut.add(new ICStatement());	//	newline
			codeOut.add(new ICStatement(labelName));

			//	statement sequence is the last node
			TreeNode statSeqNode = tree.getChildren().get(tree.getChildren().size() - 1);
			generateIRCodeForNode(statSeqNode, codeOut);

			//	add a return stmt if it's a void function
			if (funcEntry.isVoid()) {
				codeOut.add(new ICStatement("return", "", "", ""));	//	FIXME: is this the right format?
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
				//	just to make sure I didn't miss any...
				throw new RuntimeException("Don't know how to make opcode for: " + parentSymbol);
			}

			//	evaluate subexpressions
			String leftArgVar = generateIRCodeForNode(tree.getChildren().get(0), codeOut);
			String rightArgVar = generateIRCodeForNode(tree.getChildren().get(1), codeOut);

			//	add the code for this operation
			codeOut.add(new ICStatement(opCode, resultVar, leftArgVar, rightArgVar));

			return resultVar;
		} else if (compareOps.contains(parentSymbol)) {
			String resultVar = unique_var("t");
			
			//	evaluate subexpressions
			String leftArgVar = generateIRCodeForNode(tree.getChildren().get(0), codeOut);
			String rightArgVar = generateIRCodeForNode(tree.getChildren().get(1), codeOut);

			if (parentSymbol.equals(State.LESSER)) {
				codeOut.add(new ICStatement("slt", resultVar, leftArgVar, rightArgVar));
			} else if (parentSymbol.equals(State.GREATER)) {
				codeOut.add(new ICStatement("slt", resultVar, rightArgVar, leftArgVar));
			} else if (parentSymbol.equals(State.LESSEREQ)) {
				//	(a <= b) == !(b < a)
				codeOut.add(new ICStatement("slt", resultVar, rightArgVar, leftArgVar));
				codeOut.add(new ICStatement("sub", resultVar, resultVar, "1"));
			} else if (parentSymbol.equals(State.GREATEREQ)) {
				//	(a >= b) == !(a < b)
				codeOut.add(new ICStatement("slt", resultVar, leftArgVar, rightArgVar));
				codeOut.add(new ICStatement("sub", resultVar, resultVar, "1"));
			} else if (parentSymbol.equals(State.EQ)) {
				String afterEqLabel = unique_label("after_eq");

				codeOut.add(new ICStatement("assign", resultVar, "1", ""));
				codeOut.add(new ICStatement("breq", leftArgVar, rightArgVar, afterEqLabel));
				codeOut.add(new ICStatement("assign", resultVar, "0", ""));
				codeOut.add(new ICStatement(afterEqLabel));
			} else if (parentSymbol.equals(State.NEQ)) {
				String afterNeqLabel = unique_label("after_neq");

				codeOut.add(new ICStatement("assign", resultVar, "1", ""));
				codeOut.add(new ICStatement("brneq", leftArgVar, rightArgVar, afterNeqLabel));
				codeOut.add(new ICStatement("assign", resultVar, "0", ""));
				codeOut.add(new ICStatement(afterNeqLabel));
			} else {
				//	make sure I didn't forget any...
				throw new RuntimeException("Comparison operator '" + parentSymbol + "' not implemented");
			}

			return resultVar;
		} else if (parentSymbol.equals(State.ID)) {
			//	return the variable name
			return ((Token)tree.getSymbol()).value();
		} else if (parentSymbol.equals(State.ASSIGN)) {
			TreeNode lvalue = tree.getChildren().get(0);
			TreeNode rvalue = tree.getChildren().get(1);

			//	calculate the value of the right hand side
			String valueVariable = generateIRCodeForNode(rvalue, codeOut);

			//	do the assignment.  depends on what type of lvalue we have
			if (lvalue.isNodeType(State.ID)) {
				//	assignment to a named variable
				String lvalueVarName = ((Token)lvalue.getSymbol()).value();
				codeOut.add(new ICStatement("assign", lvalueVarName, valueVariable, ""));
			} else if (lvalue.isNodeType(NonTerminals.ARRAY_LOOKUP)) {
				//	assignment to an index into an array
				String linearizedIndex = generateIRCodeForArrayOffset(lvalue, codeOut);
				String arrName = ((Token)lvalue.getChildren().get(0).getSymbol()).value();
				codeOut.add(new ICStatement("array_store", arrName, linearizedIndex, valueVariable));
			} else {
				//	this shouldn't ever happen, it's just here to make sure I didn't miss something
				throw new RuntimeException("Unhandled case in ASSIGN code generator");
			}

			//	assignment operation evaluates to the value that was assigned
			return valueVariable;
		} else if (parentSymbol.equals(NonTerminals.ARRAY_LOOKUP)) {
			//	extract a value from an array
			String linearizedIndex = generateIRCodeForArrayOffset(tree, codeOut);
			String arrName = ((Token)tree.getChildren().get(0).getSymbol()).value();
			String outputVar = unique_var("arrLookup");
			codeOut.add(new ICStatement("array_load", outputVar, arrName, linearizedIndex));
			return outputVar;
		} else if (parentSymbol.equals(State.STRLIT)) {
			return ((Token)tree.getSymbol()).value();
		} else if (parentSymbol.equals(State.INTLIT)) {
			return ((Token)tree.getSymbol()).value();	//	just return the value straight up
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
		} else if (parentSymbol.equals(State.FOR)) {
			//	children: ID, expr, expr, STAT_SEQ

			//	label that goes after the contents of the loop
			String endLabelName = unique_label("for_loop_end");

			//	loop variable
			String loopVarName = ((Token)tree.getChildren().get(0).getSymbol()).value();
			String loopVarInitial = ((Token)tree.getChildren().get(1).getSymbol()).value();
			String loopVarFinal = ((Token)tree.getChildren().get(2).getSymbol()).value();

			//	init loop variable
			codeOut.add(new ICStatement("assign", loopVarName, loopVarInitial, ""));

			//	label for the top of the loop
			String labelName = unique_label("for_loop");
			codeOut.add(new ICStatement(labelName));

			//	check condition, jump to end label if we're done
			codeOut.add(new ICStatement("breq", loopVarName, loopVarFinal, endLabelName));

			//	loop content
			TreeNode loopStatements = tree.getChildren().get(3);
			generateIRCodeForNode(loopStatements, codeOut);

			//	increment loop variable
			codeOut.add(new ICStatement("add", loopVarName, loopVarName, "1"));

			//	loop end label
			codeOut.add(new ICStatement(endLabelName));

			return null;
		} else if (parentSymbol.equals(State.WHILE)) {
			//	label that goes after the contents of the loop
			String endLabelName = unique_label("while_loop_end");

			//	label for the top of the loop
			String labelName = unique_label("while_loop");
			codeOut.add(new ICStatement(labelName));

			// //	evaluate loop expr
			TreeNode loopExprNode = tree.getChildren().get(0);
			String loopVar = generateIRCodeForNode(loopExprNode, codeOut);

			//	check condition, jump to end if we're done
			codeOut.add(new ICStatement("breq", loopVar, "0", endLabelName));

			//	loop contents
			TreeNode loopContents = tree.getChildren().get(1);
			generateIRCodeForNode(loopContents, codeOut);

			//	end label
			codeOut.add(new ICStatement(endLabelName));

			return null;
		} else if (parentSymbol.equals(State.IF)) {
			boolean hasElseClause = tree.getChildren().size() == 3;
			String elseLabel = unique_label("else");
			String endIfLabel = unique_label("end_if");

			//	evaluate condition
			String conditionVar = generateIRCodeForNode(tree.getChildren().get(0), codeOut);
			String jumpTo = hasElseClause ? elseLabel : endIfLabel;
			codeOut.add(new ICStatement("breq", conditionVar, "0", jumpTo));

			//	THEN clause
			generateIRCodeForNode(tree.getChildren().get(1), codeOut);

			if (hasElseClause) {
				//	skip over ELSE clause at the end of the THEN clause
				codeOut.add(new ICStatement("goto", endIfLabel, "", ""));

				//	ELSE
				codeOut.add(new ICStatement(elseLabel));
				generateIRCodeForNode(tree.getChildren().get(2), codeOut);
			}

			//	end label
			codeOut.add(new ICStatement(endIfLabel));

			return null;
		} else if (parentSymbol.equals(State.RETURN)) {
			String returnExprVar = generateIRCodeForNode(tree.getChildren().get(0), codeOut);
			codeOut.add(new ICStatement("return", returnExprVar, "", ""));
			return null;
		} else {
			throw new RuntimeException("Don't know how to generate IR for node type: " + parentSymbol);
		}
	}

	private String generateIRCodeForArrayOffset(TreeNode arrayLookupNode, ArrayList<ICStatement> codeOut) {
		arrayLookupNode.assertNodeType(NonTerminals.ARRAY_LOOKUP);

		//	get type info on the array
		String arrayName = ((Token)arrayLookupNode.getChildren().get(0).getSymbol()).value();
		ArrayList<Integer> arrDims = symbolTable.getVar(arrayName).getType().getArrDims();

		if (arrDims.size() == 1) {
			return generateIRCodeForNode(arrayLookupNode.getChildren().get(1), codeOut);
		} else {
			String offsetVarName = unique_var("arrOffset");
			String tmpVarName = unique_var("arrOffsetTmp");

			for (int i = 1; i < arrayLookupNode.getChildren().size(); i++) {
				String idxExprResult = generateIRCodeForNode(arrayLookupNode.getChildren().get(i), codeOut);
				codeOut.add(new ICStatement("mult", tmpVarName, idxExprResult, arrDims.get(i-1).toString()));
				codeOut.add(new ICStatement("add", offsetVarName, offsetVarName, tmpVarName));
			}

			return offsetVarName;
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
