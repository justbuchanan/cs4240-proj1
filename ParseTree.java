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

	public ParseTree getAST() {
		//	un-needed nonterminals
		removeNonTerminal(NonTerminals.CONST);
		removeNonTerminal(NonTerminals.ATOM_EXPR);

		//	un-needed terminals
		removeTerminal(State.$);
		removeTerminal(State.SEMI);
		removeTerminal(State.COMMA);



		//	IF statement transforms
		{
			removeTerminal(State.ELSE);
			removeTerminal(State.THEN);
			removeTerminal(State.ENDIF);

			//	transform STAT IF
			applyTransformer(new NonTerminalParserSymbol(NonTerminals.STAT), new Token(State.IF),
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


		return this;
	}

	/**
	 * Replaces all subtrees with the given nonterminal as the root with just the arguments of the subtree.
	 * (parent1 (symbol a, b)) --> (parent1 a, b)
	 */
	public void removeNonTerminal(NonTerminals nonterminal) {
		applyTransformer(null, new NonTerminalParserSymbol(nonterminal),
			new TreeTransformer() {
				public TreeNode transform(ParserSymbol parentSymbol,
					ArrayList<TreeNode> left,
					TreeNode subSymbolTree,
					ArrayList<TreeNode> right) {

					left.addAll(subSymbolTree.getChildren());
					left.addAll(right);

					TreeNode newTree = new TreeNode(null, parentSymbol);
					newTree.setChildren(left);
					for (TreeNode childNode : left) {
						childNode.setParent(newTree);
					}

					return newTree;
				}
			});
	}

	/**
	 * Removes ALL occurrences of the given Token from the tree
	 */
	public void removeTerminal(State terminal) {
		applyTransformer(new Token(terminal), null,
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
			return "ParseTree:\n" + root.toString(0);
		} else {
			return "ParseTree: null";
		}
	}

	/**
	 * Recursively applies the transformer to the tree.
	 * It does this bottom-up and ensures that transformed trees are not re-transformed.
	 * Note: if @symbol == null, it will act as a wildcard
	 */
	public void applyTransformer(ParserSymbol symbol, ParserSymbol subSymbol, TreeTransformer transformer) {
		if (root != null) {
			root = root.applyTransformer(symbol, subSymbol, transformer);
		}
	}
}
