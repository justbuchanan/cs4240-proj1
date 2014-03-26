import java.util.LinkedList;
import java.util.Stack;
import java.util.Set;
import java.util.ArrayList;

public class Parser{

	private Scanner scanner;
	private ProductionRule[][] parserTable;
	private Grammar grammar;
	private int NUM_NONTERMINALS = 49;
	private int NUM_TERMINALS = 49;
	private ParseTree parseTree;
	private SymbolTable symbolTable;

	public Parser(Scanner scanner, Grammar grammar){
		this.scanner = scanner;
		this.grammar = grammar;
		parseTree = new ParseTree();
		buildParserTable();
		symbolTable = new SymbolTable();
	}

	public boolean parseText() {
		boolean debug = false;
		
		boolean inFunc = false;
		boolean inVar = false;
		boolean inType = false;
		LinkedList<Token> currFunc = new LinkedList<Token>();
		LinkedList<Token> currVar = new LinkedList<Token>();
		LinkedList<Token> currType = new LinkedList<Token>();
		NonTerminalParserSymbol funcRule = new NonTerminalParserSymbol(NonTerminals.FUNCT_DECLARATION);
		NonTerminalParserSymbol varRule = new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION);
		NonTerminalParserSymbol typeRule = new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION);
		Set<Token> funcFollow = grammar.findFollowSet(funcRule);
		Set<Token> varFollow = grammar.findFollowSet(varRule);
		Set<Token> typeFollow = grammar.findFollowSet(typeRule);
		
		if (debug) 		System.out.println("-------------------------------STARTING PARSE-------------------------------");

		Stack<ParserSymbol> symbolStack = new Stack<>();
		symbolStack.push(new Token(State.$));
		symbolStack.push(new NonTerminalParserSymbol(NonTerminals.TIGER_PROGRAM));
		Token prevToken = null;
		Token token = scanner.peekToken();
		boolean newToken = false;
		
		while (true) {
			prevToken = token;
			token = scanner.peekToken();
			if(prevToken != token) newToken = true;
			else newToken = false;

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
			
			// did we begin a variable, function, or type?
			if(token.type() == State.VAR) inVar = true;
			if(token.type() == State.TYPE)inType = true;
			if(token.type() == State.FUNC)inFunc = true; 
			
			// Are we reading function/var/type?
			if(newToken){
				if(inFunc){
					if(funcFollow.contains(token) && !currFunc.isEmpty()){
						// have seen entire method signature
						inFunc = false;
						buildFuncAndAddToTable(currFunc);
						currFunc.clear();
					}
					else{
						currFunc.addLast(token);
					}
				}
				if(inVar){
					if(varFollow.contains(token) && !currVar.isEmpty()){
						// reached the end of the variable
						inVar = false;
						buildVarsAndAddToTable(currVar);
						currVar.clear();
					}
					else{
						currVar.addLast(token);
					}
				}
				if(inType){
					if(typeFollow.contains(token) && !currType.isEmpty()){
						// reached end of type
						inType = false;
						buildTypeAndAddToTable(currType);
						currType.clear();
					}
					else{
						currType.addLast(token);
					}
				}
			}
			
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
		System.out.println("\nSuccessful parse!!!");
		
		// print symbol table on success
		symbolTable.printSymbolTable();

		parseTree.reduceToAST();
		semanticCheck(parseTree);

		return true;
	}
	
	
	/**
	 *  Assumes last token popped was var. Reads var/type name, sticks it in symbol table 
	 */
	private void buildVarsAndAddToTable(LinkedList<Token> varDecl){
		LinkedList<String> varNames = new LinkedList();
		String typeName = null;
		String varVal;
		Token currToken = varDecl.removeFirst();
		while(currToken.type() != State.COLON){
			if(currToken.type() == State.ID){
				varNames.push(currToken.value());
			}
			currToken = varDecl.removeFirst();
		}
		
		// currToken now = : 
		currToken = varDecl.removeFirst();
		// currToken now = TYPE 
		typeName = currToken.value();
		
		for(String var : varNames){
			symbolTable.addVar(var, typeName);
		}
	}
	

	/*
	 * Assumes last symbol popped was TYPE_DECLARATION
	 */
	private void buildTypeAndAddToTable(LinkedList<Token> typeDecl){
		String typeName = "";
		String eltTypeName = "";
		int arrSize = 0;
		Token currToken;
		currToken = typeDecl.removeFirst();
		while(currToken.type() != State.OF){ // Read everything up to 'of'
			if(currToken.type() == State.ID){
				typeName = currToken.value();
			}
			else if(currToken.type() == State.INTLIT) arrSize = Integer.parseInt(currToken.value());
			currToken = typeDecl.removeFirst();
		}
		
		// curr token now = of
		currToken = typeDecl.removeFirst(); // of eltType
		eltTypeName = currToken.value();

		symbolTable.addType(typeName, eltTypeName, arrSize);
	}

	/*
	 *  Assumes last symbol popped was FUNCT_DECLARATION
	 */
	private void buildFuncAndAddToTable(LinkedList<Token> funcDecl){
		LinkedList<Token> currParam = new LinkedList<Token>();
		Token currToken;
		String funcName = null;
		LinkedList<VarSymbolEntry> funcParams = new LinkedList();
		currToken = funcDecl.removeFirst(); // func
		while(currToken.type() != State.LPAREN){ // get function name
			if(currToken.type() == State.ID){
				funcName = currToken.value();
				symbolTable.beginScope(funcName); // tell symbol table we are in a new function
				symbolTable.addFunc(funcName);
			}
			currToken = funcDecl.removeFirst();
		}
		
		while(currToken.type() != State.RPAREN){ // get function params
			
			while(currToken.type() != State.COMMA && currToken.type() != State.RPAREN){ // read each var in param list
				currParam.addLast(currToken);
				currToken = funcDecl.removeFirst();
			}
			buildVarsAndAddToTable(currParam);
			currParam.clear();
			if(currToken.type() != State.RPAREN) currToken = funcDecl.removeFirst();
		}
		
	}

	public void semanticCheck(ParseTree ast) {
		checkBinaryOperands(ast.getRoot());
		checkFuncParams(ast.getRoot());
	}

	public void checkBinaryOperands(TreeNode treeNodeParam) {
		for (TreeNode treeNode : treeNodeParam.getChildren()) {			
			if (treeNode.getSymbol().isTerminal() && (((Token) treeNode.getSymbol()).equals(State.PLUS) || 
				((Token) treeNode.getSymbol()).equals(State.MINUS) ||
				((Token) treeNode.getSymbol()).equals(State.MULT) ||
				((Token) treeNode.getSymbol()).equals(State.DIV))) {
				System.out.println("FOUND PLUS/SUB/MULT/DIV...Checking operands");
				ArrayList<TreeNode> operatorChildren = treeNode.getChildren();
				if (operatorChildren.size() != 2) {
					System.out.println("ERROR: WRONG NUMBER OF OPERANDS FOR PLUS/SUB/MULT/DIV");
					continue;
				}
				TreeNode left = operatorChildren.get(0);
				TreeNode right = operatorChildren.get(1);
				if (!left.getSymbol().isTerminal() || !right.getSymbol().isTerminal()) {
					System.out.println("ERROR: CHILDREN SHOULD BOTH BE TERMINALS");
					continue;
				}
				if (!(((Token) left.getSymbol()).equals(State.PLUS) ||
                                	((Token) left.getSymbol()).equals(State.MINUS) ||
                                	((Token) left.getSymbol()).equals(State.MULT) ||
                                	((Token) left.getSymbol()).equals(State.DIV) ||
					((Token) left.getSymbol()).equals(State.INTLIT))) {
					System.out.println("ERROR: LEFT CHILD IS NOT OF TYPE INT/PLUS/MINUS/MULT/DIV");
				}
				if (!(((Token) right.getSymbol()).equals(State.PLUS) ||
                                        ((Token) right.getSymbol()).equals(State.MINUS) ||
                                        ((Token) right.getSymbol()).equals(State.MULT) ||
                                        ((Token) right.getSymbol()).equals(State.DIV) ||
                                        ((Token) right.getSymbol()).equals(State.INTLIT))) {
                                        System.out.println("ERROR: RIGHT CHILD IS NOT OF TYPE INT/PLUS/MINUS/MULT/DIV");
                                }
				System.out.println("OPERANDS CORRECT!!!");
			} else {
				checkBinaryOperands(treeNode);
			}
		}
	}

	private void checkFuncParams(TreeNode treeNodeParam){

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
								}
							}
							else if(((Token)funcASTRow.get(i).getSymbol()).type() == State.ID){
								// check if var is initialized
								if(symbolTable.containsVar(paramName)){
									TypeSymbolEntry varType = symbolTable.getVar(paramName).getType();
									if(varType.equals(tableFuncParamType)) continue;
									else{
										System.out.println("PASSED VARIABLE OF WRONG TYPE!!!");
									}
								}
							}
						}
						else{
							System.out.println("ERROR, FUNC SIGNATURE DOES NOT CONTAIN PARAM AT I");
						}
					}	
				}
				else{
					System.out.println("FUNCTION HAS NOT BEEN DECLARED!!!");
				}
			}
			else{
				// symbol tree node is not function call, so check children
				for(TreeNode child : node.getChildren()){
					checkFuncParams(child);
				}
			}
		}
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
