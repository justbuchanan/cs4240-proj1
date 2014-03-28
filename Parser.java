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


	/**
	 * Throws an exception if the node's symbol doesn't match the given type.
	 */
	private void assertNodeType(TreeNode node, Enum type) {
		if ( !nodeIsType(node, type) ) {
			throw new RuntimeException("Expected node type '" + type + "', got node: " + node);
		}
	}

	private boolean nodeIsType(TreeNode node, Enum type) {
		if (type instanceof State) {
			return ((Token)node.getSymbol()).type().equals((State)type);
		} else {
			return ((NonTerminalParserSymbol)node.getSymbol()).getNonTerminal().equals((NonTerminals)type);
		}
	}


	private void buildSymbolTableFromAST(ParseTree ast) {
		TreeNode declSeg = ast.getRoot().getChildren().get(0);
		assertNodeType(declSeg, NonTerminals.DECLARATION_SEGMENT);

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
					assertNodeType(typeNode, NonTerminals.TYPE);

					TreeNode typeIDNode = typeNode.getChildren().get( typeNode.getChildren().size() - 1 );
					assertNodeType(typeIDNode, NonTerminals.TYPE_ID);

					//	the element type (first child of the typeIDNode)
					String eltType = getTypeOfNode(typeIDNode.getChildren().get(0));

					//	look at everything under TYPE that comes before typeID.  these will be INTLITs specifying array dimension sizes
					ArrayList<Integer> arrDims = new ArrayList<>();
					for (int i = 0; i <= typeNode.getChildren().size() - 2; i++) {
						TreeNode intlit = typeNode.getChildren().get(i);
						assertNodeType(intlit, State.INTLIT);

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
							assertNodeType(varTypeNode, State.ID);
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
					
					String funcReturnType = "";
					TreeNode secondToLastChild = funcDecl.getChildren().get( funcDecl.getChildren().size() - 2 );
					if (secondToLastChild.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.RET_TYPE))) {
						funcReturnType = getTypeOfNode(secondToLastChild.getChildren().get(0));
					}
					
						
					symbolTable.addFunc(funcName, funcReturnType);

					symbolTable.beginScope(funcName); {

						//	if there's a PARAM_LIST, it'll be the second child
						TreeNode secondChild = funcDecl.getChildren().get(1);
						if (secondChild.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.PARAM_LIST))) {

							//	add each param to the symbol table
							for (TreeNode param : secondChild.getChildren()) {
								String paramName = ((Token)param.getChildren().get(0).getSymbol()).value();
								String paramType = getTypeOfNode(param.getChildren().get(1).getChildren().get(0));

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

	private void ArrayList<TreeNode> addOccurrencesOfNodeType(TreeNode tree, Enum nodeType, ArrayList<TreeNode> list) {
		//	add tree if it matches
		if (nodeIsType(tree, nodeType)) {
			list.add(tree);
		}

		//	recurse
		for (TreeNode child : tree.getChildren()) {
			addOccurrencesOfNodeType(child, nodeType, list);
		}
	}

	public boolean semanticCheck(ParseTree ast) {
		return checkBinaryOperands(ast.getRoot()) && 
		checkInitialization(ast.getRoot()) && 
		checkFuncParams(ast.getRoot());
	}
	
	public boolean checkFuncReturnTypes(TreeNode treeNode){
		for(TreeNode currNode : treeNode.getChildren()){
			if(currNode.getSymbol().equals(NonTerminals.FUNCT_DECLARATION)){
				ArrayList<TreeNode> funcVals = currNode.getChildren();
				String funcName = ((Token)funcVals.get(0).getSymbol()).value();
				ArrayList<TreeNode> funcBody = funcVals.get(funcVals.size() - 1).getChildren(); // STAT, STAT_SEQ_PRIME
				ArrayList<TreeNode> statement = funcBody.get(0).getChildren();
				if(statement.get(0).equals(new Token(State.RETURN))){
					// check return type of function
					FuncSymbolEntry func = symbolTable.getFunc(funcName);
					ArrayList<TreeNode> expr = statement.get(1).getChildren();
					
				}
				
				
			}
			else{
				checkFuncReturnTypes(currNode);
			}
		}
		return false;
	}
	
	
	public boolean checkInitialization(TreeNode treeNodeParam) {
		boolean pass = true;
		for (TreeNode treeNode : treeNodeParam.getChildren()) {
			if (treeNode.getSymbol().isTerminal() && ((Token) treeNode.getSymbol()).ordinal() == State.ASSIGN.ordinal()) {
				TreeNode assignTo = treeNode.getChildren().get(0);
				if (assignTo == null || !symbolTable.containsVar(((Token) assignTo.getSymbol()).value())) {
					System.out.println("ERROR: VARIABLE " + ((Token) assignTo.getSymbol()).value() + " IS NOT DEFINED");
				}
			}
			checkInitialization(treeNode);	
		}

		return pass;
	}

	public boolean checkBinaryOperands(TreeNode treeNodeParam) {
		boolean pass = true;

		for (TreeNode treeNode : treeNodeParam.getChildren()) {			
			if (treeNode.getSymbol().isTerminal() && (((Token) treeNode.getSymbol()).ordinal() == State.PLUS.ordinal() || 
				((Token) treeNode.getSymbol()).ordinal() == State.MINUS.ordinal() ||
				((Token) treeNode.getSymbol()).ordinal() == State.MULT.ordinal() ||
				((Token) treeNode.getSymbol()).ordinal() == State.DIV.ordinal())) {
				System.out.println("FOUND PLUS/SUB/MULT/DIV...Checking operands");
				ArrayList<TreeNode> operatorChildren = treeNode.getChildren();
				if (operatorChildren.size() != 2) {
					System.out.println("ERROR: WRONG NUMBER OF OPERANDS FOR PLUS/SUB/MULT/DIV");
					pass = false;
					continue;
				}
				TreeNode left = operatorChildren.get(0);
				TreeNode right = operatorChildren.get(1);
				
				if (getTypeOfNode(left) == null || !getTypeOfNode(left).equals("int")) {
					System.out.println("ERROR: Variable needs to be int");
					pass = false;
				}
				if (getTypeOfNode(right) == null || getTypeOfNode(right) != null && !getTypeOfNode(right).equals("int")) {
					System.out.println("ERROR: Variable needs to be int");
					pass = false;
				}
				if (pass == true) {
					System.out.println("OPERANDS CORRECT!!!");
				}
			}
			checkBinaryOperands(treeNode);
		}

		return pass;
	}
	
	
	

	//	Recursively get the type of a given tree
	//
	//	note: returns null if there's no type info for a given ID
	//	note: throws an exception if type isn't meaningful for the given tree
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


		if (topLevelSymbol instanceof Token) {
			Token token = (Token)topLevelSymbol;
			State type = token.type();

			if (type.equals(State.INTLIT)) {
				return "int";
			} else if (type.equals(State.ID)) {
				if (symbolTable.containsVar(token.value())) {
					String funcName = symbolTable.getVar(token.value()).getScope().getFuncName();
					String realFuncName = "";
					TreeNode treeNodeLoop = treeNode;
					while (true) {
						treeNodeLoop = treeNodeLoop.getParent();
						if (treeNodeLoop.getSymbol().isTerminal() && ((Token) treeNodeLoop.getSymbol()).ordinal() == State.FUNC.ordinal()) {
							realFuncName = ((Token) treeNodeLoop.getSymbol()).value();
							break;
						} else if(!treeNodeLoop.getSymbol().isTerminal() && ((NonTerminalParserSymbol) treeNodeLoop.getSymbol()).ordinal() == NonTerminals.TIGER_PROGRAM.ordinal()) {
							realFuncName = "LET";
							break;
						}
					}
					System.out.println("Found func name: " + realFuncName);
					System.out.println("Actual func name: " + funcName);
					if (!realFuncName.equals(funcName)) {
						System.out.println("ERROR: NOT IN SCOPE");
					} else {
						System.out.println("SCOPE CORRECT");
					}
					return symbolTable.getVar(token.value()).getType().typeString();
				} else if (symbolTable.getType(token.value()) != null) {
					return symbolTable.getType(token.value()).typeString();
				} else {
					//	couldn't find the ID
					System.out.println("ERROR: Unknown ID: " + token.value());
					return null;
				}
			} else if (type.equals(State.STRLIT)) {
				return "string";
			} else if (operators.contains(type)) {
				return getTypeOfNode(treeNode.getChildren().get(0));	//	return left operand for binary op nodes
			} else {
				throw IllegalArgumentException("Type doesn't make sense for the given tree: " + treeNode);
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
			} else {
				throw IllegalArgumentException("Type doesn't make sense for the given tree: " + treeNode);
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
						String paramName = ((Token)funcASTRow.get(i).getSymbol()).value();
						if(tableFuncParams.size() >= (i - 1)){ // make sure param is in func sig
							TypeSymbolEntry tableFuncParamType = tableFuncParams.get(i-1).getType();
							TypeSymbolEntry passedParamType = symbolTable.getVar(((Token)funcASTRow.get(i).getSymbol()).value()).getType();
							if(((Token)funcASTRow.get(i).getSymbol()).type() == State.INTLIT || 
								((Token)funcASTRow.get(i).getSymbol()).type() == State.STRLIT){
								if(tableFuncParamType.equals(passedParamType)){
									continue;
								}
								else{
									System.out.println("Passed wrong type!!!!!");
									return false;
								}
							}
							else if(((Token)funcASTRow.get(i).getSymbol()).type() == State.ID){
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
}
