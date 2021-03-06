import java.util.LinkedList;
import java.util.Stack;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;

public class Parser{

	
	private ProductionRule[][] parserTable;
	private Grammar grammar;
	private int NUM_NONTERMINALS = 49;
	private int NUM_TERMINALS = 49;
	private ParseTree parseTree;
	private SymbolTable symbolTable;
	

	public Parser(Grammar grammar){
		this.grammar = grammar;
		buildParserTable();
		symbolTable = new SymbolTable();
	}

	public boolean parseText(Scanner scanner, boolean verbose) {
		boolean debug = false;
		parseTree = new ParseTree(); // clear previous parse tree
		
		if (debug)	System.out.println("-------------------------------STARTING PARSE-------------------------------");

		Stack<ParserSymbol> symbolStack = new Stack<>();
		symbolStack.push(new Token(State.$));
		symbolStack.push(new NonTerminalParserSymbol(NonTerminals.TIGER_PROGRAM));
		Token token = scanner.peekToken();
		
		while (true) {
			token = scanner.peekToken();

			//	when the scanner returns null, it means we're at the end of the file
			if (token == null) {
				token = new Token(State.$);
			}
			// invalid
			if(token.ordinal() == State.ERROR.ordinal()){
				System.out.println("Invalid token on line: " + token.lineNumber);
				System.out.println("unsuccessful parse");
				return false;
			}

			//	eat comments
			if (token.ordinal() == State.COMMENT.ordinal()) {
				if (debug) System.out.println("Ate a comment\n");
				scanner.nextToken();
				continue;
			}

			if (debug) System.out.println("Peeked token: " + token + ":" + token.value());
			
			
			ParserSymbol parserSymbol = symbolStack.pop();

			if (debug) System.out.println("< Popped parser symbol: " + parserSymbol);

			//	we hit a null, which is our marker in the symbol stack that we've matched all of the right-hand-side of some non-terminal
			//	this is our signal to pop the current level on the parse tree
			if (parserSymbol == null) {
				if (debug) System.out.println("Popped parse tree level");
				parseTree.popLevel();
				continue;
			}

			if(parserSymbol instanceof Token) {
				if(token.equals(parserSymbol)) {
					parseTree.add(token);	//	add it to the parse tree
					scanner.nextToken();	//	eat the token we just peeked and
					if (debug) System.out.println("Matched the token!\n");

					if (token.type() == State.$) {
						break;	//	we completed the parse!
					} else {
						continue;
					}
				} else {
					System.out.println("ERROR: Found " + token + ", expecting " + parserSymbol);
					return false;
				}
			} else {
				ProductionRule productionRule = parserTable[((NonTerminalParserSymbol)parserSymbol).getNonTerminal().ordinal()][token.ordinal()];

				if (productionRule == null) {
					System.out.println("ERROR: Trying to match '" + parserSymbol + "', but found: '" + token + "' on line: " + token.lineNumber);
					return false;
				}
				
				ParserSymbol rightSymbol = productionRule.right()[0];
				if (productionRule.right().length == 1 &&
					rightSymbol.isTerminal() &&
					((Token)rightSymbol).ordinal() == State.NULL.ordinal()
					) {

				} else {
					parseTree.pushLevel(productionRule.left());

					symbolStack.push(null);	//	push null onto the stack so we know when to pop this level on the parse tree

					for(int i = productionRule.right().length - 1; i >= 0; i--) {
						if (debug) System.out.println(">> Parser Push: " + productionRule.right()[i]);
						symbolStack.push(productionRule.right()[i]);
					}
				}
			}
		}


		if (scanner.nextToken() != null) {
			System.out.println("\nERROR: extra tokens left after parser finished");
			return false;
		}


		//	reduce to AST and print before and after
		System.out.println("ParseTree:\n" + parseTree);
		parseTree.reduceToAST();
		System.out.println("AST:\n" + parseTree);

		//	build and print symbol table
		buildSymbolTableFromAST(parseTree);
		if (verbose) symbolTable.printSymbolTable();

		//	semantic check and return success
		boolean success = semanticCheck(parseTree);
		if (verbose) {
			if (success) {
				System.out.println("\nSuccessful parse!!!");
			} else {
				System.out.println("\nUnsuccessful parse :(");
			}
		}
		return success;
	}


	private void buildSymbolTableFromAST(ParseTree ast) {
		TreeNode declSeg = ast.getRoot().getChildren().get(0);
		declSeg.assertNodeType(NonTerminals.DECLARATION_SEGMENT);

		//	look for the different declaration lists (var, type, funct)
		for (TreeNode declList : declSeg.getChildren()) {

			if (declList.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION_LIST))) {
				//	handle type declarations

				//	look at each type declaration
				for (TreeNode typeDecl : declList.getChildren()) {
					//	first child is an ID node for the type name
					String typeName = ((Token)typeDecl.getChildren().get(0).getSymbol()).value();

					//	nonterminal TYPE
					TreeNode typeNode = typeDecl.getChildren().get(1);
					typeNode.assertNodeType(NonTerminals.TYPE);

					TreeNode typeIDNode = typeNode.getChildren().get( typeNode.getChildren().size() - 1 );
					typeIDNode.assertNodeType(NonTerminals.TYPE_ID);

					//	the element type (first child of the typeIDNode)
					String eltType = getTypeOfNode(typeIDNode.getChildren().get(0));

					//	look at everything under TYPE that comes before typeID.  these will be INTLITs specifying array dimension sizes
					ArrayList<Integer> arrDims = new ArrayList<>();
					for (int i = 0; i <= typeNode.getChildren().size() - 2; i++) {
						TreeNode intlit = typeNode.getChildren().get(i);
						intlit.assertNodeType(State.INTLIT);

						Integer arrDim = Integer.parseInt( ((Token)intlit.getSymbol()).value() );
						arrDims.add(arrDim);
					}

					symbolTable.addType(typeName, eltType, arrDims);
				}
			} else if (declList.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION_LIST))) {
				//	handle var declarations

				//	look at each var declaration
				for (TreeNode varDecl : declList.getChildren()) {
					ArrayList<String> idList = new ArrayList<>();
					String varType = null;

					//	get variable names and type info
					for (TreeNode varDeclChild : varDecl.getChildren()) {
						//	the list of IDs (variable names) being declared
						if (varDeclChild.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.ID_LIST))) {
							for (TreeNode idNode : varDeclChild.getChildren()) {
								String varName = ((Token)idNode.getSymbol()).value();
								idList.add(varName);
							}
						} else if (varDeclChild.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.TYPE_ID))) {
							TreeNode varTypeNode = varDeclChild.getChildren().get(0);
							varTypeNode.assertNodeType(State.ID);
							varType = ((Token)varTypeNode.getSymbol()).value();
						}
					}

					//	add entries to the table
					for (String id : idList) {
						symbolTable.addVar(id, varType);
					}
				}
			} else if (declList.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION_LIST))) {
				//	handle function declarations

				TreeNode funcDeclList = declList;	//	found the func declarations!

				//	function declaration has children: ID (func name), PARAM_LIST (optional?), RET_TYPE (optional), STAT_SEQ
				for (TreeNode funcDecl : funcDeclList.getChildren()) {
					String funcName = ((Token)funcDecl.getChildren().get(0).getSymbol()).value();	//	first child is an ID for the function name
					
					String funcReturnType = null;
					TreeNode secondToLastChild = funcDecl.getChildren().get( funcDecl.getChildren().size() - 2 );
					if (secondToLastChild.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.RET_TYPE))) {
						funcReturnType = getTypeOfNode(secondToLastChild.getChildren().get(0).getChildren().get(0));
					}
					
						
					symbolTable.addFunc(funcName, funcReturnType);

					symbolTable.beginScope(funcName); {

						//	if there's a PARAM_LIST, it'll be the second child
						TreeNode secondChild = funcDecl.getChildren().get(1);
						if (secondChild.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.PARAM_LIST))) {

							//	add each param to the symbol table
							for (TreeNode param : secondChild.getChildren()) {
								String paramName = ((Token)param.getChildren().get(0).getSymbol()).value();
								TreeNode idNode = param.getChildren().get(1).getChildren().get(0);
								String paramType = getTypeOfNode(idNode);
								symbolTable.addVar(paramName, paramType);
							}
						}
					} symbolTable.endScope();
				}
			}
		}
	}


	private ArrayList<TreeNode> findOccurrencesOfNodeType(TreeNode tree, Enum nodeType) {
		ArrayList<TreeNode> result = new ArrayList<>();
		addOccurrencesOfNodeType(tree, nodeType, result);
		return result;
	}

	private void addOccurrencesOfNodeType(TreeNode tree, Enum nodeType, ArrayList<TreeNode> list) {
		//	add tree if it matches
		if (tree.isNodeType(nodeType)) {
			list.add(tree);
		}

		//	recurse
		for (TreeNode child : tree.getChildren()) {
			addOccurrencesOfNodeType(child, nodeType, list);
		}
	}

	public boolean semanticCheck(ParseTree ast) {
		return checkInitialization(ast.getRoot()) &&
		findAssignmentStatement(ast.getRoot()) &&
		checkFuncParams(ast.getRoot()) &&
		checkFuncReturnTypes(ast.getRoot());
	}
	
	public boolean checkFuncReturnTypes(TreeNode treeNode){
		for(TreeNode currNode : treeNode.getChildren()){
			if(currNode.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION))){
				TreeNode idNode = currNode.getChildren().get(0);
				String funcName = ((Token)idNode.getSymbol()).value();
				FuncSymbolEntry func = symbolTable.getFunc(funcName);
				// descend subtree and look for return type
				ArrayList<TreeNode> returnStatements = findSymbolSubTree(currNode, new Token(State.RETURN), new ArrayList<TreeNode>());
				if(func.getReturnType() == null && !returnStatements.isEmpty()){
					System.out.println("ERROR: WRONG RETURN TYPE FOR FUNC " + funcName + " , "
							+ "expected void" );
					return false;
				}
				for(TreeNode retNode : returnStatements){
					String retType = checkFuncReturnTypeHelper(retNode);
					if(retType != null && !retType.equals(symbolTable.getFunc(funcName).getReturnType())){
						System.out.println("ERROR: WRONG RETURN TYPE FOR FUNCTION " + funcName + " "
								+ "expected " + symbolTable.getFunc(funcName).getReturnType() + " but found " + retType);
						return false;
					}
				}
			}
			else{
				checkFuncReturnTypes(currNode);
			}
		}
		return true;
	}

	public String checkFuncReturnTypeHelper(TreeNode retNode){
		if(retNode.getSymbol().isTerminal() && ((Token)retNode.getSymbol()).type() == State.RETURN){
			ArrayList<TreeNode> returnVals = retNode.getChildren();
			return getTypeOfNode(returnVals.get(0));
		}
		else{
			throw new RuntimeException("Called checkFuncReturnTypeHelper with wrong type!");
		}
	}

	public ArrayList<TreeNode> findSymbolSubTree(TreeNode currNode, ParserSymbol target, ArrayList<TreeNode> foundTargets){
		if(currNode.getSymbol().equals(target)){
			foundTargets.add(currNode);
		}
		else{
			for(TreeNode child : currNode.getChildren()){
				findSymbolSubTree(child, target, foundTargets);
			}
		}
		return foundTargets;
	}
	
	
	public boolean checkInitialization(TreeNode treeNodeParam) {
		boolean pass = true;
		for (TreeNode treeNode : treeNodeParam.getChildren()) {
			if (treeNode.getSymbol().isTerminal() && ((Token) treeNode.getSymbol()).ordinal() == State.ASSIGN.ordinal()) {
				TreeNode assignTo = treeNode.getChildren().get(0);
				if (assignTo == null || (assignTo.getSymbol() instanceof Token && (!symbolTable.containsVar(((Token) assignTo.getSymbol()).value()) && ((Token) assignTo.getSymbol()).ordinal() != State.INTLIT.ordinal() && ((Token) assignTo.getSymbol()).ordinal() != State.MINUS.ordinal() && ((Token) assignTo.getSymbol()).ordinal() != State.PLUS.ordinal()))) {
					System.out.println("ERROR: VARIABLE " + ((Token) assignTo.getSymbol()).value() + " AT LINE " + ((Token) assignTo.getSymbol()).lineNumber + " IS NOT DEFINED");
				} else if (assignTo.getSymbol() instanceof NonTerminalParserSymbol && ((NonTerminalParserSymbol) assignTo.getSymbol()).ordinal() == NonTerminals.ARRAY_LOOKUP.ordinal()) {
					if (!symbolTable.containsVar(((Token) assignTo.getChildren().get(0).getSymbol()).value())) {
						System.out.println("ERROR: VARIABLE " + ((Token) assignTo.getChildren().get(0).getSymbol()).value() + " AT LINE " + ((Token) assignTo.getChildren().get(0).getSymbol()).lineNumber + " IS NOT DEFINED");
					}
				}
			}
			checkInitialization(treeNode);	
		}

		return pass;
	}

	public boolean findAssignmentStatement(TreeNode treeNodeParam) {
		boolean pass = true;
		for (TreeNode treeNode : treeNodeParam.getChildren()) {
			if (treeNode.getSymbol().isTerminal() && ((Token) treeNode.getSymbol()).ordinal() == State.ASSIGN.ordinal() && treeNode.getChildren().size() > 0) {
				TreeNode assignTo = treeNode.getChildren().get(0);
				if (assignTo.getSymbol() instanceof Token && symbolTable.containsVar(((Token) assignTo.getSymbol()).value())) {
					if ( !checkBinaryOperands(treeNode, symbolTable.getVar(((Token) assignTo.getSymbol()).value()).getType().getName() )) pass = false;
				} else if (assignTo.getSymbol() instanceof NonTerminalParserSymbol && ((NonTerminalParserSymbol) assignTo.getSymbol()).ordinal() == NonTerminals.ARRAY_LOOKUP.ordinal()) {
					assignTo = assignTo.getChildren().get(0);
					if ( !checkBinaryOperands(treeNode, symbolTable.getVar(((Token) assignTo.getSymbol()).value()).getType().getName() )) pass = false;
				}
			}
			if( !findAssignmentStatement(treeNode)) pass = false;
		}

		return pass;
	}

	public boolean checkBinaryOperands(TreeNode treeNodeParam, String type) {
		boolean pass = true;

		//	operators that work only on ints
		ArrayList<Token> intOperators = new ArrayList<Token>(
			Arrays.asList(new Token[]{
				new Token(State.MULT),
				new Token(State.DIV),
				new Token(State.PLUS),
				new Token(State.MINUS),
				new Token(State.GREATER),
				new Token(State.LESSER),
				new Token(State.GREATEREQ),
				new Token(State.LESSEREQ),
				new Token(State.EQ),
				new Token(State.NEQ),
				new Token(State.AND),
				new Token(State.OR),
			})
		);

		for (TreeNode treeNode : treeNodeParam.getChildren()) {	
			if (intOperators.contains(treeNode.getSymbol())) {
				// System.out.println("FOUND int-only operator (MULT, LESSER, etc)... Checking operands");
				ArrayList<TreeNode> operatorChildren = treeNode.getChildren();
				if (operatorChildren.size() != 2) {
					System.out.println("ERROR: WRONG NUMBER OF OPERANDS FOR PLUS/SUB/MULT/DIV");
					pass = false;
				} else {
					TreeNode left = operatorChildren.get(0);
					TreeNode right = operatorChildren.get(1);
					String varName = "";				
					if (type.equals("ArrayInt")) {
						type = "int";
					}	
					if (getTypeOfNode(left) == null || !getTypeOfNode(left).equals(type)) {						
						if (symbolTable.containsVar(((Token) left.getSymbol()).value())) {
							System.out.println("ERROR: Variable " + symbolTable.getVar(((Token) left.getSymbol()).value()) + " at line number " + ((Token) left.getSymbol()).lineNumber + " needs to be " + type);
						} else {
							System.out.println("ERROR: Variable " + ((Token)left.getSymbol()).value() + " at line number " + ((Token) left.getSymbol()).lineNumber + " needs to be " + type);
						}
						pass = false;
					}
					if (getTypeOfNode(right) == null || !getTypeOfNode(right).equals(type)) {
						if (symbolTable.containsVar(((Token) right.getSymbol()).value())) {
							System.out.println("ERROR: Variable " + symbolTable.getVar(((Token) right.getSymbol()).value()) + " at line number " + ((Token) left.getSymbol()).lineNumber + " needs to be " + type);
						} else {
							System.out.println("ERROR: Variable " + ((Token)right.getSymbol()).value() + " at line number " + ((Token) right.getSymbol()).lineNumber +" needs to be " + type);
						}
						pass = false;
					}
					if (pass == true) {
						// System.out.println("OPERANDS CORRECT!!!");
					}
				}
			}

			//	if a subtree fails, the whole thing fails
			if ( !checkBinaryOperands(treeNode, type) ) pass = false;
		}

		//	FIXME: what operators can apply to strings as well as ints?
		//	FIXME: ID values for error messages

		return pass;
	}
	
	
	

	//	Recursively get the type of a given tree
	//
	//	note: returns null if there's no type info for a given ID
	//	note: throws an exception if type isn't meaningful for the given tree
	//	note: returns "int" for comparison operators
	//	note: returns the type of the left operand for operator nodes
	//		example: getTypeOfNode( (+ "abc" 123) ) --> "string";  getTypeOfNode( (+ 123 "abc") ) --> "int"
	private String getTypeOfNode(TreeNode treeNode) {
		ParserSymbol topLevelSymbol = treeNode.getSymbol();


		ArrayList<State> operators = new ArrayList<State>(
			Arrays.asList(new State[]{
				State.MULT,
				State.DIV,
				State.PLUS,
				State.MINUS,
				State.GREATER,
				State.LESSER,
				State.GREATEREQ,
				State.LESSEREQ,
				State.EQ,
				State.NEQ,
				State.ASSIGN,
			})
		);

		ArrayList<State> comparisonOperators = new ArrayList<State>(
			Arrays.asList(new State[]{
				State.MULT,
				State.DIV,
				State.PLUS,
				State.MINUS,
				State.GREATER,
				State.LESSER,
				State.GREATEREQ,
				State.LESSEREQ,
				State.EQ,
				State.NEQ,
				State.ASSIGN,
			})
		);		

		if (topLevelSymbol instanceof Token) {
			Token token = (Token)topLevelSymbol;
			State type = token.type();
			
			if (type.equals(State.INTLIT)) {
				return "int";
			} else if (type.equals(State.ID)) {
				if (symbolTable.containsVar(token.value())) {
					String funcName = symbolTable.getVar(token.value()).getScope().getFuncName();
					String variableName = symbolTable.getVar(token.value()).getName();
					String realFuncName = "";
					TreeNode treeNodeLoop = treeNode;
					while (true) {
						treeNodeLoop = treeNodeLoop.getParent();
						if (!treeNodeLoop.getSymbol().isTerminal() && ((NonTerminalParserSymbol) treeNodeLoop.getSymbol()).ordinal() == NonTerminals.FUNCT_DECLARATION.ordinal()) {
							realFuncName = ((Token) treeNodeLoop.getChildren().get(0).getSymbol()).value();
							break;
						} else if(!treeNodeLoop.getSymbol().isTerminal() && ((NonTerminalParserSymbol) treeNodeLoop.getSymbol()).ordinal() == NonTerminals.TIGER_PROGRAM.ordinal()) {
							realFuncName = "LET";
							break;
						}
					}
					boolean varMatched = false;
					for(VarSymbolEntry var : symbolTable.getVarList(token.value())){
						if(var.getName().equals("ASCII_NUMERIC_OFFSET")){
							System.out.println();
						}
						if(var.getScope().getFuncName().equals(realFuncName) || var.getScope().getLevel() == 0) varMatched = true;
					}
					if (!varMatched) {
						System.out.println("ERROR: variable " + variableName + " AT LINE: " + token.lineNumber + " NOT IN SCOPE");
					} else {
						// System.out.println("SCOPE CORRECT");
					}
					return symbolTable.getVar(token.value()).getType().typeString();
				} else if (symbolTable.getType(token.value()) != null) {
					return token.value();
				} else {
					//	couldn't find the ID
					System.out.println("ERROR: Unknown ID: " + token.value());
					return null;
				}
			} else if (type.equals(State.STRLIT)) {
				return "string";
			} else if (operators.contains(type)) {
				if (comparisonOperators.contains(type)) {
					return "int";
				} else {
					return getTypeOfNode(treeNode.getChildren().get(0));	//	return left operand for binary op nodes
				}
			} else if (type.equals(State.NIL)) {
				return null;
			} else {
				throw new IllegalArgumentException("Type doesn't make sense for the given tree: " + treeNode);
			}
		} else {
			NonTerminalParserSymbol topNonterminalSymbol = (NonTerminalParserSymbol)topLevelSymbol;
			NonTerminals nonterminal = topNonterminalSymbol.getNonTerminal();

			if (nonterminal.equals(NonTerminals.FUNCTION_CALL)) {
				TreeNode idNode = treeNode.getChildren().get(0);
				String funcName = ((Token)idNode.getSymbol()).value();
				return symbolTable.getFunc(funcName).getReturnType();
			} else if (nonterminal.equals(NonTerminals.ARRAY_LOOKUP)) {
				TreeNode idNode = treeNode.getChildren().get(0);
				String arrayName = ((Token)idNode.getSymbol()).value();

				if (symbolTable.containsVar(arrayName)) {
					return symbolTable.getVar(arrayName).getType().getEltType();
				} else {
					//	couldn't find the ID
					System.out.println("ERROR: Unknown array: " + arrayName);
					return null;
				}
			} else if (nonterminal.equals(NonTerminals.TYPE_ID)) {
				System.out.println("GET TYPE OF TYPE_ID NODE: " + treeNode);
				return getTypeOfNode(treeNode.getChildren().get(0));
			} else {
				throw new IllegalArgumentException("Type doesn't make sense for the given tree: " + treeNode);
			}
		}
	}


	private boolean checkFuncParams(TreeNode treeNodeParam){

		for(TreeNode node : treeNodeParam.getChildren()){
			//TODO: FIX NonTerminalParserSymbol.equals()
			if(!node.getSymbol().isTerminal() && ((NonTerminalParserSymbol)node.getSymbol()).ordinal() == NonTerminals.FUNCTION_CALL.ordinal()){
				ArrayList<TreeNode> funcASTRow = node.getChildren();
				ParserSymbol funcNameSymbol = funcASTRow.get(0).getSymbol();
				if(funcNameSymbol.isTerminal() && 
				symbolTable.containsFunc(((Token)funcNameSymbol).value())){ // function name
					FuncSymbolEntry tableFunc = symbolTable.getFunc(((Token)funcNameSymbol).value());
					ArrayList<VarSymbolEntry> tableFuncParams = tableFunc.getParams();
					// check params
					for(int i = 1; i < funcASTRow.size(); i++){			
						if(tableFuncParams.size() >= (i - 1)){ // make sure param is in func sig
							TypeSymbolEntry tableFuncParamType = tableFuncParams.get(i-1).getType();
							if(!funcASTRow.get(i).getSymbol().isTerminal()){
								System.out.println();
							}
							
							TypeSymbolEntry passedParamType = null;
							if(funcASTRow.get(i).getSymbol().isTerminal()){
								Token tokFuncParam = (Token)funcASTRow.get(i).getSymbol();
								if(tokFuncParam.type().equals(State.INTLIT)){
									passedParamType = symbolTable.getType("int");
								}
								else if (tokFuncParam.type().equals(State.STRLIT)){
									passedParamType = symbolTable.getType("string");
								}
								else if (tokFuncParam.type().equals(State.ID)){
									if(symbolTable.getVar(tokFuncParam.value() ) == null){
										System.out.println();
									}
									passedParamType = symbolTable.getVar(tokFuncParam.value()).getType();
								}
								if(tokFuncParam.type() == State.INTLIT || 
										tokFuncParam.type() == State.STRLIT){
										if(tableFuncParamType.equals(passedParamType)){
											continue;
										}
										else{
											System.out.println("Passed wrong type!!!!!");
											return false;
										}
									}
									else if(((Token)funcASTRow.get(i).getSymbol()).type() == State.ID){
										String paramName = ((Token)funcASTRow.get(i).getSymbol()).value();
										// check if var is initialized
										if(symbolTable.containsVar(paramName)){
											TypeSymbolEntry varType = symbolTable.getVar(paramName).getType();
											if(varType.equals(tableFuncParamType)) continue;
											else{
												System.out.println("PASSED VARIABLE OF WRONG TYPE!!!");
												return false;
											}
										}
									}
							}
							else{
								
							}
						}
						else{
							System.out.println("ERROR, FUNC SIGNATURE DOES NOT CONTAIN PARAM AT I");
							return false;
						}
					}	
				}
				else{
					System.out.println("FUNCTION HAS NOT BEEN DECLARED!!!");
					return false;
				}
			}
			else{
				// symbol tree node is not function call, so check children
				for(TreeNode child : node.getChildren()){
					checkFuncParams(child);
				}
			}
		}
		return true;
	}

	private void buildParserTable(){
		parserTable = new ProductionRule[NUM_NONTERMINALS][NUM_TERMINALS];

		Token nullSymbol = new Token(State.NULL);

		//	add entries based on first && follow sets
		for (ProductionRule rule : grammar.allRules()) {
			Set<Token> firstSet = grammar.findFirstSet(rule);
			for (Token terminal : firstSet) {
				if (terminal.ordinal() != State.NULL.ordinal()) {
					if (parserTable[rule.left().ordinal()][terminal.ordinal()] != null) {
						throw new RuntimeException("ERROR: Rule collision at column " + terminal + ", grammar is not LL(1): " + parserTable[rule.left().ordinal()][terminal.ordinal()] + " ******* " + rule);
					}

					parserTable[rule.left().ordinal()][terminal.ordinal()] = rule;
				}
			}

			if (firstSet.contains(nullSymbol)) {
				NonTerminalParserSymbol nonterminal = rule.left();
				Set<Token> followSet = grammar.findFollowSet(nonterminal);

				for (Token terminal : followSet) {
					ProductionRule newNullRule = new ProductionRule(nonterminal.getNonTerminal(), State.NULL);
					ProductionRule existingRule = parserTable[nonterminal.ordinal()][terminal.ordinal()];
					if (existingRule != null && !existingRule.equals(newNullRule)) {
						System.out.println(existingRule + " != " + newNullRule);
						throw new RuntimeException("ERROR: Rule collision at column " + terminal + ", grammar is not LL(1): " + existingRule + " ******* " + newNullRule);
					}
					parserTable[nonterminal.ordinal()][terminal.ordinal()] = newNullRule;
				}
			}
		}
	}

	public String generateParseTableCSV() {
		String csv = new String();

		csv += ",";	//	leave upper-left corner blank
		for (int termIdx = 0; termIdx < NUM_TERMINALS; termIdx++) {
			csv += State.values()[termIdx] + ",";
		}

		csv += "\n";

		for (int nontermIdx = 0; nontermIdx < NUM_NONTERMINALS; nontermIdx++) {
			csv += NonTerminals.values()[nontermIdx] + ",";

			for (int termIdx = 0; termIdx < NUM_TERMINALS; termIdx++) {
				ProductionRule rule = parserTable[nontermIdx][termIdx];

				if (rule != null) {
					csv += rule.left().getNonTerminal() + " --> ";
					for (ParserSymbol rightSmbl : rule.right()) {
						if (rightSmbl.isTerminal()) {
							csv += ((Token)rightSmbl).type() + " ";
						} else {
							csv += ((NonTerminalParserSymbol)rightSmbl).getNonTerminal() + " ";
						}
					}
				}

				csv += ",";
			}

			csv += "\n";
		}

		return csv;
	}

	public String prettyPrintedFirstSets() {
		String output = "";

		//	add entries based on first && follow sets
		for (ArrayList<ProductionRule> rules : grammar.rulesByNonTerminal()) {
			for (ProductionRule rule : rules) {
				output += "FIRST(" + rule + ") = " + grammar.findFirstSet(rule) + "\n";
			}
			output += "\n";	//	newline between sets of rules
		}

		return output;
	}

	public String prettyPrintedFollowSets() {
		String output = "";

		Token nullSymbol = new Token(State.NULL);

		for (NonTerminalParserSymbol nonterminal : grammar.allNonTerminalSymbols()) {
			Set<Token> followSet = grammar.findFollowSet(nonterminal);

			output += "FOLLOW(" + nonterminal + ") = " + followSet + "\n";
		}

		return output;
	}

	public void printTree() {
		System.out.println("AST:\n" + parseTree.toString());
	}

	/** 
	 * Returns the reduced parse tree.
	 * Assumes that parseText() has already been called.
	 */
	public ParseTree getAST() {
		return parseTree;
	}

	/**
	 * Assumes that parseText() has already been called.
	 */
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}
}
