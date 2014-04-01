import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Stack;

/**
 * Used to create intermediate representation code for a given program
 */
public class IRCodeGenerator {
	//	track allocated names so we don't have any collisions
	private Set<String> labels;
	private Set<String> variables;
	private SymbolTable symbolTable;
	private Stack<String> enclosingLoopEnds;	//	keeps track of where to go if we hit a BREAK
	private ArrayList<ICStatement> intermediateCode;

	/** 
	 * Generates the entire IR code for the program represented by @ast.  Modifies @symbolTable
	 * to add temporary variables, etc.
	 */
	public static ArrayList<ICStatement> generateIRCode(ParseTree ast, SymbolTable symbolTable) {
		IRCodeGenerator generator = new IRCodeGenerator(symbolTable);
		try {
			generator.generateIRCodeForNode(ast.getRoot());
		}
		catch (RuntimeException exc) {
			System.out.println("IR Code gen ERROR: " + exc);
		}
		
		return generator.intermediateCode;
	}

	private IRCodeGenerator(SymbolTable symbolTable) {
		labels = new HashSet<String>();

		intermediateCode = new ArrayList<>();

		//	build unique var name set, taking into account what variables already exist
		variables = new HashSet<String>();
		variables.addAll(symbolTable.getAllVarNames());

		this.enclosingLoopEnds = new Stack<>();

		this.symbolTable = symbolTable;
	}

	private void emit(ICStatement stmt) {
		intermediateCode.add(stmt);
	}

	/**
	 * Generates ICStatements and adds them in order to @codeOut.  This is meant to be used recursively.
	 * @return the name of the register where the resulting value is placed.  may be null
	 */
	private String generateIRCodeForNode(TreeNode tree) {
		if (tree == null || symbolTable == null) {
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
			if (funcDeclList != null) generateIRCodeForNode(funcDeclList);

			//	add a main label to start the program
			String mainLabel = unique_label("main");
			if (!mainLabel.equals("main")) {
				throw new RuntimeException("Unable to allocate label 'main'");
			}
			emit(new ICStatement());
			emit(new ICStatement(mainLabel));

			//	variable declarations
			if (varDeclList != null) {
				generateIRCodeForNode(varDeclList);
				emit(new ICStatement());	//	newline
			}

			//	see if there's a STAT_SEQ
			if (tree.getChildren().size() > 1) {
				//	code for STAT_SEQ
				generateIRCodeForNode(tree.getChildren().get(1));
			}

			return null;
		} else if (parentSymbol.equals(NonTerminals.STAT_SEQ)) {
			//	generate code for each statement in series
			for (TreeNode statement : tree.getChildren()) {
				generateIRCodeForNode(statement);
			}
			return null;
		} else if (parentSymbol.equals(NonTerminals.DECLARATION_SEGMENT)) {
			//	most of the declaration segment just adds entries to the symbol table
			// 	we need to generate code for each function AND for each variable initialization

			//	may have TYPE_DECLARATION_LIST, VAR_DECLARATION_LIST, FUNCT_DECLARATION_LIST
			for (TreeNode subtree : tree.getChildren()) {
				generateIRCodeForNode(subtree);
			}

			return null;
		} else if (parentSymbol.equals(NonTerminals.TYPE_DECLARATION_LIST)) {
			//	nothing to do
			return null;
		} else if (parentSymbol.equals(NonTerminals.VAR_DECLARATION_LIST)) {
			for (TreeNode varDecl : tree.getChildren()) {
				generateIRCodeForNode(varDecl);
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
				String constExprVar = generateIRCodeForNode(constExpr);

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

						emit(new ICStatement("assign", varName, Integer.toString(arrSize), constExprVar));
					} else {
						//	value initialization
						emit(new ICStatement("assign", varName, constExprVar, ""));
					}
				}
			}

			return null;
		} else if (parentSymbol.equals(NonTerminals.FUNCT_DECLARATION_LIST)) {
			//	generate code for each function
			for (TreeNode funcDecl : tree.getChildren()) {
				generateIRCodeForNode(funcDecl);
			}
			return null;
		} else if (parentSymbol.equals(NonTerminals.FUNCT_DECLARATION)) {
			String funcName = ((Token)tree.getChildren().get(0).getSymbol()).value();
			String labelName = unique_label("func_" + funcName);

			FuncSymbolEntry funcEntry = symbolTable.getFunc(funcName);

			emit(new ICStatement());	//	newline
			emit(new ICStatement(labelName));

			//	statement sequence is the last node
			TreeNode statSeqNode = tree.getChildren().get(tree.getChildren().size() - 1);
			generateIRCodeForNode(statSeqNode);

			//	add a return stmt if it's a void function
			if (funcEntry.isVoid()) {
				emit(new ICStatement("return", "", "", ""));	//	FIXME: is this the right format?
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
			String leftArgVar = generateIRCodeForNode(tree.getChildren().get(0));
			String rightArgVar = generateIRCodeForNode(tree.getChildren().get(1));

			//	add the code for this operation
			emit(new ICStatement(opCode, resultVar, leftArgVar, rightArgVar));

			return resultVar;
		} else if (compareOps.contains(parentSymbol)) {
			String resultVar = unique_var("t");
			
			//	evaluate subexpressions
			String leftArgVar = generateIRCodeForNode(tree.getChildren().get(0));
			String rightArgVar = generateIRCodeForNode(tree.getChildren().get(1));

			if (parentSymbol.equals(State.LESSER)) {
				emit(new ICStatement("slt", resultVar, leftArgVar, rightArgVar));
			} else if (parentSymbol.equals(State.GREATER)) {
				emit(new ICStatement("slt", resultVar, rightArgVar, leftArgVar));
			} else if (parentSymbol.equals(State.LESSEREQ)) {
				//	(a <= b) == !(b < a)
				emit(new ICStatement("slt", resultVar, rightArgVar, leftArgVar));
				emit(new ICStatement("sub", resultVar, resultVar, "1"));
			} else if (parentSymbol.equals(State.GREATEREQ)) {
				//	(a >= b) == !(a < b)
				emit(new ICStatement("slt", resultVar, leftArgVar, rightArgVar));
				emit(new ICStatement("sub", resultVar, resultVar, "1"));
			} else if (parentSymbol.equals(State.EQ)) {
				String afterEqLabel = unique_label("after_eq");

				emit(new ICStatement("assign", resultVar, "1", ""));
				emit(new ICStatement("breq", leftArgVar, rightArgVar, afterEqLabel));
				emit(new ICStatement("assign", resultVar, "0", ""));
				emit(new ICStatement(afterEqLabel));
			} else if (parentSymbol.equals(State.NEQ)) {
				String afterNeqLabel = unique_label("after_neq");

				emit(new ICStatement("assign", resultVar, "1", ""));
				emit(new ICStatement("brneq", leftArgVar, rightArgVar, afterNeqLabel));
				emit(new ICStatement("assign", resultVar, "0", ""));
				emit(new ICStatement(afterNeqLabel));
			} else {
				//	make sure I didn't forget any...
				throw new RuntimeException("Comparison operator '" + parentSymbol + "' not implemented");
			}

			return resultVar;
		} else if (parentSymbol.equals(State.OR)) {
			String endLabel = unique_label("end_or");

			//	starts out as true
			String resultVar = unique_var("orResult");
			emit(new ICStatement("assign", resultVar, "1", ""));

			//	evaluate the left arg and skip the right arg if left is true
			String leftArgVar = generateIRCodeForNode(tree.getChildren().get(0));
			emit(new ICStatement("brneq", leftArgVar, "0", endLabel));

			//	evaluate the right arg
			String rightArgVar = generateIRCodeForNode(tree.getChildren().get(1));
			emit(new ICStatement("brneq", rightArgVar, "0", endLabel));

			//	if rightArgVar was zero, set result to false
			emit(new ICStatement("assign", resultVar, "0", ""));

			//	end label
			emit(new ICStatement(endLabel));

			return resultVar;
		} else if (parentSymbol.equals(State.AND)) {
			String endLabel = unique_label("end_and");

			//	starts out as false
			String resultVar = unique_var("andResult");
			emit(new ICStatement("assign", resultVar, "0", ""));

			//	evaluate the left arg and skip the right arg if left is false
			String leftArgVar = generateIRCodeForNode(tree.getChildren().get(0));
			emit(new ICStatement("breq", leftArgVar, "0", endLabel));

			//	evaluate the right arg
			String rightArgVar = generateIRCodeForNode(tree.getChildren().get(1));
			emit(new ICStatement("breq", rightArgVar, "0", endLabel));

			//	if rightArgVar was nonzero, set result to true
			emit(new ICStatement("assign", resultVar, "1", ""));

			//	end label
			emit(new ICStatement(endLabel));

			return resultVar;
		} else if (parentSymbol.equals(State.ID)) {
			//	return the variable name
			return ((Token)tree.getSymbol()).value();
		} else if (parentSymbol.equals(State.ASSIGN)) {
			TreeNode lvalue = tree.getChildren().get(0);
			TreeNode rvalue = tree.getChildren().get(1);

			//	calculate the value of the right hand side
			String valueVariable = generateIRCodeForNode(rvalue);

			//	do the assignment.  depends on what type of lvalue we have
			if (lvalue.isNodeType(State.ID)) {
				//	assignment to a named variable
				String lvalueVarName = ((Token)lvalue.getSymbol()).value();
				emit(new ICStatement("assign", lvalueVarName, valueVariable, ""));
			} else if (lvalue.isNodeType(NonTerminals.ARRAY_LOOKUP)) {
				//	assignment to an index into an array
				String linearizedIndex = generateIRCodeForArrayOffset(lvalue);
				String arrName = ((Token)lvalue.getChildren().get(0).getSymbol()).value();
				emit(new ICStatement("array_store", arrName, linearizedIndex, valueVariable));
			} else {
				//	this shouldn't ever happen, it's just here to make sure I didn't miss something
				throw new RuntimeException("Unhandled case in ASSIGN code generator");
			}

			//	assignment operation evaluates to the value that was assigned
			return valueVariable;
		} else if (parentSymbol.equals(NonTerminals.ARRAY_LOOKUP)) {
			//	extract a value from an array
			String linearizedIndex = generateIRCodeForArrayOffset(tree);
			String arrName = ((Token)tree.getChildren().get(0).getSymbol()).value();
			String outputVar = unique_var("arrLookup");
			emit(new ICStatement("array_load", outputVar, arrName, linearizedIndex));
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
				String param = generateIRCodeForNode(tree.getChildren().get(i));
				params.add(param);
			}

			FuncSymbolEntry funcEntry = symbolTable.getFunc(funcName);
			if (funcEntry.isVoid()) {
				emit(new ICStatement(funcName, params));
				return null;
			} else {
				String retValVar = unique_var("retVal");
				emit(new ICStatement(funcName, retValVar, params));
				return retValVar;
			}
		} else if (parentSymbol.equals(State.FOR)) {
			//	children: ID, expr, expr, STAT_SEQ

			//	label that goes after the contents of the loop
			String endLabelName = unique_label("for_loop_end");

			enclosingLoopEnds.push(endLabelName); {
				//	loop variable
				String loopVarName = ((Token)tree.getChildren().get(0).getSymbol()).value();
				String loopVarInitial = ((Token)tree.getChildren().get(1).getSymbol()).value();
				String loopVarFinal = ((Token)tree.getChildren().get(2).getSymbol()).value();

				//	init loop variable
				emit(new ICStatement("assign", loopVarName, loopVarInitial, ""));

				//	label for the top of the loop
				String labelName = unique_label("for_loop");
				emit(new ICStatement(labelName));

				//	check condition, jump to end label if we're done
				emit(new ICStatement("breq", loopVarName, loopVarFinal, endLabelName));

				//	loop content
				TreeNode loopStatements = tree.getChildren().get(3);
				generateIRCodeForNode(loopStatements);

				//	increment loop variable
				emit(new ICStatement("add", loopVarName, loopVarName, "1"));

				//	jump to top of loop
				emit(new ICStatement("goto", labelName, "", ""));
				
				//	loop end label
				emit(new ICStatement(endLabelName));
			} enclosingLoopEnds.pop();

			return null;
		} else if (parentSymbol.equals(State.WHILE)) {
			//	label that goes after the contents of the loop
			String endLabelName = unique_label("while_loop_end");

			enclosingLoopEnds.push(endLabelName); {
				//	label for the top of the loop
				String labelName = unique_label("while_loop");
				emit(new ICStatement(labelName));

				// //	evaluate loop expr
				TreeNode loopExprNode = tree.getChildren().get(0);
				String loopVar = generateIRCodeForNode(loopExprNode);

				//	check condition, jump to end if we're done
				emit(new ICStatement("breq", loopVar, "0", endLabelName));

				//	loop contents
				TreeNode loopContents = tree.getChildren().get(1);
				generateIRCodeForNode(loopContents);

				//	jump to top of loop
				emit(new ICStatement("goto", labelName, "", ""));

				//	end label
				emit(new ICStatement(endLabelName));
			} enclosingLoopEnds.pop();

			return null;
		} else if (parentSymbol.equals(State.IF)) {
			boolean hasElseClause = tree.getChildren().size() == 3;
			String elseLabel = unique_label("else");
			String endIfLabel = unique_label("end_if");

			//	evaluate condition
			String conditionVar = generateIRCodeForNode(tree.getChildren().get(0));
			String jumpTo = hasElseClause ? elseLabel : endIfLabel;
			emit(new ICStatement("breq", conditionVar, "0", jumpTo));

			//	THEN clause
			generateIRCodeForNode(tree.getChildren().get(1));

			if (hasElseClause) {
				//	skip over ELSE clause at the end of the THEN clause
				emit(new ICStatement("goto", endIfLabel, "", ""));

				//	ELSE
				emit(new ICStatement(elseLabel));
				generateIRCodeForNode(tree.getChildren().get(2));
			}

			//	end label
			emit(new ICStatement(endIfLabel));

			return null;
		} else if (parentSymbol.equals(State.RETURN)) {
			String returnExprVar = generateIRCodeForNode(tree.getChildren().get(0));
			emit(new ICStatement("return", returnExprVar, "", ""));
			return null;
		} else if (parentSymbol.equals(State.BREAK)) {
			String jumpTo = enclosingLoopEnds.peek();
			emit(new ICStatement("goto", jumpTo, "", ""));
			return null;
		} else {
			throw new RuntimeException("Don't know how to generate IR for node type: " + parentSymbol);
		}
	}

	private String generateIRCodeForArrayOffset(TreeNode arrayLookupNode) {
		arrayLookupNode.assertNodeType(NonTerminals.ARRAY_LOOKUP);

		//	get type info on the array
		String arrayName = ((Token)arrayLookupNode.getChildren().get(0).getSymbol()).value();
		ArrayList<Integer> arrDims = symbolTable.getVar(arrayName).getType().getArrDims();

		if (arrDims.size() == 1) {
			return generateIRCodeForNode(arrayLookupNode.getChildren().get(1));
		} else {
			String offsetVarName = unique_var("arrOffset");
			String tmpVarName = unique_var("arrOffsetTmp");

			for (int i = 1; i < arrayLookupNode.getChildren().size(); i++) {
				String idxExprResult = generateIRCodeForNode(arrayLookupNode.getChildren().get(i));
				emit(new ICStatement("mult", tmpVarName, idxExprResult, arrDims.get(i-1).toString()));
				emit(new ICStatement("add", offsetVarName, offsetVarName, tmpVarName));
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
