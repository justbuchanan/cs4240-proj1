import java.util.ArrayList;

public class ParseTree {
	private TreeNode root = null;
	private TreeNode current = null;
	private int size;

	public ParseTree() {
		size = 0;
	}

	public void pushLevel(NonTerminalParserSymbol nonTerminal) {
		if (root == null) {
			root = new TreeNode(null, nonTerminal);
			current = root;
		} else {
			TreeNode newNode = new TreeNode(current, nonTerminal);
			current.addChild(newNode);
			current = newNode;
		}
	}

	public void popLevel() {
		current = current.getParent();
	}

	public void add(Token token) {
		if (token == null) {
			throw new IllegalArgumentException("token can not be null!");
		}

		if (current == null) {
			throw new RuntimeException("current == null at add()");
		}

		size++;
		current.addChild(token);
	}

	public int getSize() {
		return size;
	}
	
	public void helper(TreeNode treeNodeVar) {
		for (TreeNode treeNode : treeNodeVar.getChildren()) {
			System.out.println(treeNode.getSymbol().toString());
			if(!treeNode.getSymbol().isTerminal()) {
				System.out.println("\n" + treeNode.getSymbol().toString());
				helper(treeNode);
				System.out.println("Bottom of tree...");
			}
		}
	}

	public TreeNode getRoot() {
		return root;
	}

	public void reduceToAST() {
		//	un-needed nonterminals
		removeNonTerminal(NonTerminals.CONST);
		removeNonTerminal(NonTerminals.STAT_SEQ_PRIME);

		removeNonTerminal(NonTerminals.MULT_DIV_OP);
		removeNonTerminal(NonTerminals.ADD_SUB_OP);
		removeNonTerminal(NonTerminals.BOOL_OP);

		removeNonTerminal(NonTerminals.EXPR_ANY_TAIL);
		removeNonTerminal(NonTerminals.ADD_SUB_EXPR_TAIL);
		removeNonTerminal(NonTerminals.MULT_DIV_EXPR_TAIL);
		removeNonTerminal(NonTerminals.BOOL_EXPR_TAIL);
		removeNonTerminal(NonTerminals.AND_EXPR_TAIL);
		removeNonTerminal(NonTerminals.OR_EXPR_TAIL);

		removeNonTerminal(NonTerminals.EXPR_OR_ID);
		removeNonTerminal(NonTerminals.ID_LIST_PRIME);
		removeNonTerminal(NonTerminals.OPTIONAL_INIT);


		//	un-needed terminals
		removeTerminal(State.$);
		removeTerminal(State.LET);
		removeTerminal(State.IN);
		removeTerminal(State.END);
		removeTerminal(State.SEMI);
		removeTerminal(State.COMMA);
		removeTerminal(State.VAR);


		//	flatten ID_LIST, VAR_DECLARATION_LIST, TYPE_DECLARATION_LIST
		NonTerminals[] toFlatten = new NonTerminals[]{
			NonTerminals.ID_LIST,
			NonTerminals.TYPE_DECLARATION_LIST,
			NonTerminals.VAR_DECLARATION_LIST,
			NonTerminals.FUNCT_DECLARATION_LIST,
			NonTerminals.LVALUE_TAIL
		};
		for (NonTerminals nonterminal : toFlatten) {
			applyTransformer(new NonTerminalParserSymbol(nonterminal), new NonTerminalParserSymbol(nonterminal), false,
				new TreeTransformer() {
					public TreeNode transform(ParserSymbol parentSymbol,
						ArrayList<TreeNode> left,
						TreeNode subSymbolTree,
						ArrayList<TreeNode> right) {

						ArrayList<TreeNode> children = new ArrayList<>();
						children.addAll(left);
						children.addAll(subSymbolTree.getChildren());
						children.addAll(right);

						TreeNode newTree = new TreeNode(null, parentSymbol);
						newTree.setChildren(children);
						return newTree;
					}
				});
		}

		// function calls
		for (NonTerminals nonterminal : new NonTerminals[]{NonTerminals.EXPR_OR_FUNC, NonTerminals.STAT_AFTER_ID}) {
			applyTransformer(null, new NonTerminalParserSymbol(nonterminal), false,
				new TreeTransformer() {
					public TreeNode transform(ParserSymbol parentSymbol,
						ArrayList<TreeNode> left,
						TreeNode subSymbolTree,
						ArrayList<TreeNode> right) {

						//	note: right should always be empty
						if (right.size() > 0) {
							throw new RuntimeException("right should never have stuff in it");
						}

						//	see if first child of EXPR_OR_FUNC is a LPAREN
						if (subSymbolTree.getChildren().size() > 0) {
							TreeNode firstChild = subSymbolTree.getChildren().get(0);
							if (firstChild.getSymbol() instanceof Token) {
								State tokVal = ((Token)firstChild.getSymbol()).type();
								if (tokVal.equals(State.LPAREN)) {

									//	it's a function call!

									TreeNode funcCallTree = new TreeNode(null, new NonTerminalParserSymbol(NonTerminals.FUNCTION_CALL));
									ArrayList<TreeNode> funcChildren = new ArrayList<>();
									funcChildren.add(left.get(left.size() - 1));	//	add the ID
									funcChildren.addAll(subSymbolTree.getChildren());
									funcCallTree.setChildren(funcChildren);

									//	we matched based on a child node, so add our new tree back into the parent
									TreeNode newTree = new TreeNode(null, parentSymbol);
									ArrayList<TreeNode> children = new ArrayList<>();

									//	add anything that came before the ID (the func name) to the parent
									for (int i = 0; i < left.size() - 1; i++) {
										children.add(left.get(i));
									}

									children.add(funcCallTree);
									newTree.setChildren(children);

									return newTree;
								}
							}
						}

						//	if it wasn't a function, we'll just let it be, but remove the EXPR_OR_FUNC
						TreeNode newTree = new TreeNode(null, parentSymbol);
						left.addAll(subSymbolTree.getChildren());
						newTree.setChildren(left);
						return newTree;
					}
				});
		}


		//	EXPR
		applyTransformer(new NonTerminalParserSymbol(NonTerminals.ATOM_EXPR), new Token(State.LPAREN), false,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {

					ArrayList<TreeNode> children = new ArrayList<>();
					children.add(right.get(0));	//	take the center part of the expression, we don't need the PARENs

					TreeNode newTree = new TreeNode(null, parentSymbol);
					newTree.setChildren(children);

					return newTree;
				}
			});
		removeNonTerminal(NonTerminals.EXPR);		//	handle EXPRs that don't have parentheses
		removeNonTerminal(NonTerminals.ATOM_EXPR);	//


		//	IF statement transforms
		{
			removeTerminal(State.ELSE);
			removeTerminal(State.THEN);
			removeTerminal(State.ENDIF);

			//	transform STAT IF
			applyTransformer(new NonTerminalParserSymbol(NonTerminals.STAT), new Token(State.IF), true,
				new TreeTransformer() {
					public TreeNode transform(ParserSymbol parentSymbol,
						ArrayList<TreeNode> left,
						TreeNode subSymbolTree,
						ArrayList<TreeNode> right) {
						
						//	after the transform, the IF becomes the root symbol, not the STAT
						TreeNode newSubtree = new TreeNode(null, new Token(State.IF));

						for (TreeNode subNode : right) {

							if (subNode.getSymbol().equals(new NonTerminalParserSymbol(NonTerminals.STAT_IF_TAIL))) {
								//	add everything under STAT_IF_TAIL to be under the IF
								for (TreeNode subSubNode : subNode.getChildren()) {
									newSubtree.getChildren().add(subSubNode);
									subSubNode.setParent(newSubtree);
								}
							} else {
								newSubtree.getChildren().add(subNode);
								subNode.setParent(newSubtree);
							}
						}

						return newSubtree;
					}
				});
		}


		//	ARRAY_LOOKUP
		removeNonTerminal(NonTerminals.LVALUE);
		removeTerminal(State.RBRACK);
		removeTerminal(State.LBRACK);
		applyTransformer(null, new NonTerminalParserSymbol(NonTerminals.LVALUE_TAIL), false,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {

					//	LVALUE_TAIL is null, delete it from the tree
					if (subSymbolTree.getChildren().size() == 0) {
						TreeNode newTree = new TreeNode(null, parentSymbol);
						ArrayList<TreeNode> children = new ArrayList<>();
						children.addAll(left);
						children.addAll(right);
						newTree.setChildren(children);
						return newTree;
					} else {
						if (left.size() == 0) {
							throw new RuntimeException("LVALUE_TAIL should have ID on the left side");
						}

						TreeNode arrayID = left.get(left.size() - 1);
						arrayID.assertNodeType(State.ID);

						TreeNode arrLookupTree = new TreeNode(null, new NonTerminalParserSymbol(NonTerminals.ARRAY_LOOKUP));
						ArrayList<TreeNode> arrLookupChildren = new ArrayList<>();
						arrLookupChildren.add(arrayID);
						arrLookupChildren.addAll(subSymbolTree.getChildren());	//	array indices
						arrLookupTree.setChildren(arrLookupChildren);

						TreeNode newTree = new TreeNode(null, parentSymbol);
						ArrayList<TreeNode> children = new ArrayList<>();
						for (int i = 0; i < left.size() - 1; i++) {
							children.add(left.get(i));
						}
						children.add(arrLookupTree);
						children.addAll(right);

						newTree.setChildren(children);

						return newTree;
					}
				}
			});



		//	NEGATED_EXPR transform
		applyTransformer(new NonTerminalParserSymbol(NonTerminals.NEGATED_EXPR), null, false,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,	//	the parent will be NEGATED_EXPR
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {
					
					//	if there are 2 args, it's a "-x", otherwise it's just "x"
					if (left.size() == 2) {
						//	new tree with 
						TreeNode newTree = new TreeNode(null, parentSymbol);
						TreeNode arg = left.get(1);
						newTree.getChildren().add(arg);
						arg.setParent(newTree);

						return newTree;
					} else {
						return left.get(0);
					}
				}
			});


		//	FIXME: most of this infix stuff doesn't behave as expected because there are other transformations that have to happen first
		//	infix operators
		State[] infixOps = new State[]{
			State.PLUS,
			State.MINUS,
			State.MULT,
			State.DIV,
			State.EQ,
			State.NEQ,
			State.GREATER,
			State.LESSER,
			State.GREATEREQ,
			State.LESSEREQ,
			State.AND,
			State.OR,
			State.ASSIGN};
		for (State infixOp : infixOps) {
			applyTransformer(null, new Token(infixOp), false,
			new TreeTransformer() {
					public TreeNode transform(ParserSymbol parentSymbol,
						ArrayList<TreeNode> left,
						TreeNode subSymbolTree,	//	this is the infix operator "tree"
						ArrayList<TreeNode> right) {
						
						//	add the args to the left and right of the operator as children of the infix operator

						int leftArgIndex = left.size() - 1;
						TreeNode leftArg = left.get(leftArgIndex);
						left.remove(leftArgIndex);

						int rightArgIndex = 0;
						TreeNode rightArg = right.get(0);
						right.remove(rightArgIndex);

						subSymbolTree.getChildren().add(leftArg);
						subSymbolTree.getChildren().add(rightArg);
						for (TreeNode arg : subSymbolTree.getChildren()) {
							arg.setParent(subSymbolTree);
						}

						TreeNode newTree = new TreeNode(null, parentSymbol);

						ArrayList<TreeNode> children = new ArrayList<>();
						children.addAll(left);
						children.add(subSymbolTree);	//	the "collected" infix tree
						children.addAll(right);
						newTree.setChildren(children);

						return newTree;
					}
				});
		}



		//	assignment operator in var declaration (optional init)
		applyTransformer(new NonTerminalParserSymbol(NonTerminals.VAR_DECLARATION), new Token(State.ASSIGN), true,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,	//	the parent will be NEGATED_EXPR
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {
					
					//	note: right should be empty because the assign comes last
					
					TreeNode newTree = new TreeNode(null, parentSymbol);
					ArrayList<TreeNode> children = new ArrayList<>();
					children.addAll(left);
					children.addAll(subSymbolTree.getChildren());
					newTree.setChildren(children);

					return newTree;
				}
			});

		//	EQ in TYPE_DECLARATION
		applyTransformer(new NonTerminalParserSymbol(NonTerminals.TYPE_DECLARATION), new Token(State.EQ), true,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,	//	the parent will be TYPE_DECLARATION
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {
					
					//	note: right and left should be empty
					
					TreeNode newTree = new TreeNode(null, parentSymbol);
					ArrayList<TreeNode> children = new ArrayList<>();
					children.addAll(subSymbolTree.getChildren());
					newTree.setChildren(children);

					return newTree;
				}
			});



		//	WHILE statement transforms
		removeTerminal(State.DO);
		applyTransformer(new NonTerminalParserSymbol(NonTerminals.STAT), new Token(State.WHILE), true,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {
					
					//	(STAT WHILE EXPR STAT_SEQ) -> (WHILE EXPR STAT_SEQ)
					subSymbolTree.getChildren().addAll(right);
					for (TreeNode child : subSymbolTree.getChildren()) {
						child.setParent(subSymbolTree);
					}

					return subSymbolTree;
				}
			});


		//	FOR statement
		removeTerminal(State.TO);
		applyTransformer(new NonTerminalParserSymbol(NonTerminals.STAT), new Token(State.FOR), false,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {
					
					//	FOR will be first, so @left will be empty

					//	@right should contain: ASSIGN(ID, EXPR), EXPR, STAT_SEQ

					TreeNode assignTree = right.get(0);

					ArrayList<TreeNode> forChildren = new ArrayList<>();
					forChildren.addAll(assignTree.getChildren());
					forChildren.add(right.get(1));	//	EXPR
					forChildren.add(right.get(2));	//	STAT_SEQ

					TreeNode forTree = new TreeNode(null, new Token(State.FOR));
					forTree.setChildren(forChildren);

					TreeNode newTree = new TreeNode(null, parentSymbol);	//	STAT
					newTree.getChildren().add(forTree);
					forTree.setParent(newTree);

					return newTree;
				}
			});


		//	RETURN statements
		applyTransformer(new NonTerminalParserSymbol(NonTerminals.STAT), new Token(State.RETURN), true,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {

					ArrayList<TreeNode> children = new ArrayList<>();
					children.addAll(right);
					subSymbolTree.setChildren(children);

					return subSymbolTree;
				}
			});


		//	remove infix expressions (none: this must be done after handling the infix operators)
		NonTerminals[] infixExprs = new NonTerminals[]{
			NonTerminals.MULT_DIV_EXPR,
			NonTerminals.ADD_SUB_EXPR,
			NonTerminals.BOOL_EXPR,
			NonTerminals.AND_EXPR,
			NonTerminals.OR_EXPR
		};
		for (NonTerminals infixExpr : infixExprs) {
			removeNonTerminal(infixExpr);
		}


		removeNonTerminal(NonTerminals.STAT);
		removeNonTerminal(NonTerminals.EXPR_LIST);
		removeNonTerminal(NonTerminals.EXPR_NO_LVALUE);
		removeNonTerminal(NonTerminals.EXPR);
		removeNonTerminal(NonTerminals.PARAM_LIST_TAIL);
		removeNonTerminal(NonTerminals.EXPR_LIST_TAIL);

		removeTerminal(State.LPAREN);
		removeTerminal(State.RPAREN);
		removeTerminal(State.COLON);
		removeTerminal(State.TYPE);
		removeTerminal(State.BEGIN);
		removeTerminal(State.FUNC);
		removeTerminal(State.OF);
		removeTerminal(State.ARRAY);
	}

	/**
	 * Replaces all subtrees with the given nonterminal as the root with just the arguments of the subtree.
	 * (parent1 (symbol a, b)) --> (parent1 a, b)
	 */
	public void removeNonTerminal(NonTerminals nonterminal) {
		applyTransformer(null, new NonTerminalParserSymbol(nonterminal), false,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {

					ArrayList<TreeNode> children = new ArrayList<>();
					children.addAll(left);
					children.addAll(subSymbolTree.getChildren());
					children.addAll(right);

					TreeNode newTree = new TreeNode(null, parentSymbol);
					newTree.setChildren(children);

					return newTree;
				}
			});
	}

	/**
	 * Removes ALL occurrences of the given Token from the tree
	 */
	public void removeTerminal(State terminal) {
		applyTransformer(new Token(terminal), null, false,
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {
					return null;
				}
			});
	}

	/**
	 * Returns a String in a format similar to:
	 *
	 * + parent
	 * |--= Child
	 * |-+= Child
	 * | +--= Subchild
	 * |--= Child3
	 */
	public String toString() {
		if (root != null) {
			return root.toString(0);
		} else {
			return "null";
		}
	}

	/**
	 * Recursively applies the transformer to the tree.
	 * It does this bottom-up and ensures that transformed trees are not re-transformed.
	 * Note: if @symbol == null, it will act as a wildcard
	 * Note: when doing transformations, remember to set the parent if it changes
	 *
	 * @param matchNonLeafTokens - when reducing, sometimes (example binary operators), Tokens that started out as leaf nodes in the parse tree
	 *  become parent nodes.  typically matchNonLeafTokens should be false to avoid re-converting those things
	 */
	public void applyTransformer(ParserSymbol symbol, ParserSymbol subSymbol, boolean matchNonLeafTokens, TreeTransformer transformer) {
		if (root != null) {
			root = root.applyTransformer(symbol, subSymbol, matchNonLeafTokens, transformer);
		}
	}
}
