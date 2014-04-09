import java.util.ArrayList;

public class TreeNode {
	private TreeNode parent;
	private ArrayList<TreeNode> children;
	private ParserSymbol parserSymbol;

	public TreeNode(TreeNode parent, ParserSymbol symbol) {
		if (symbol == null) {
			throw new IllegalArgumentException("TreeNode can't have a null symbol");
		}

		this.parserSymbol = symbol;
		this.parent = parent;
		this.children = new ArrayList<>();
	}

	public TreeNode(TreeNode parent, NonTerminalParserSymbol nonTerminal) {
		this(parent, (ParserSymbol)nonTerminal);
	}

	public TreeNode(TreeNode parent, Token token) {
		this(parent, (ParserSymbol)token);
	}
	
	public void addChild(Token token) {
		children.add(new TreeNode(this, token));
	}

	public void addChild(TreeNode node) {
		children.add(node);
	}

	public ArrayList<TreeNode> getChildren() {
		return children;
	}

	/**
	 * Throws an exception if the node's symbol doesn't match the given type.
	 */
	public void assertNodeType(Enum type) {
		if ( !isNodeType(type) ) {
			throw new RuntimeException("Expected node type '" + type + "', got node: " + this);
		}
	}

	public boolean isNodeType(Enum type) {
		try {
			if (type instanceof State) {
				return ((Token)getSymbol()).type().equals((State)type);
			} else {
				return ((NonTerminalParserSymbol)getSymbol()).getNonTerminal().equals((NonTerminals)type);
			}
		}
		catch (ClassCastException exc) {
			return false;
		}
	}

	/**
	 * Replaces the @children array with the given array.
	 * Also updates @parent for each new child node
	 */
	public void setChildren(ArrayList<TreeNode> children) {
		this.children = children;
		for (TreeNode child : children) {
			child.setParent(this);
		}
	}

	public ParserSymbol getSymbol() {
		return parserSymbol;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public String toString(int depth) {
		String prefix = "";
		if (depth > 0) {
			prefix = "|";
			for (int i = 1; i < depth; i++) {
				prefix += " |";
			}
		}
		
		String desc = prefix;

		if (depth > 0) {
			desc += "-";
		}
		desc += children.size() > 0 ? "+= " : "-= ";
		desc += parserSymbol.toString() + "\n";
		for (TreeNode childNode : children) {
			desc += childNode.toString(depth + 1);
		}

		return desc;
	}

	public TreeNode applyTransformer(ParserSymbol symbol, ParserSymbol subSymbol, boolean matchNonLeafTokens, TreeTransformer transformer) {
		boolean debug = false;

		for (int i = 0; i < children.size();) {
			TreeNode child = children.get(i);
			TreeNode newChild = child.applyTransformer(symbol, subSymbol, matchNonLeafTokens, transformer);

			//	replace the old subtree with the new
			if (newChild != child) {
				String toDesc = newChild == null ? "" : newChild.toString(1);
				String fromDesc = child.toString(1);
				if (debug) System.out.println("TRANSFORM match(" + symbol + ", " + subSymbol + ")\nFROM:\n" + fromDesc + "TO:\n" + toDesc);

				children.remove(i);	//	remove old child

				if (newChild != null) {
					children.add(i, newChild);
					newChild.setParent(this);
				} else {
					continue;	//	don't increment the index
				}
			}

			i++;
		}

		//	check if the transformer applies to @this
		//	do this after applying to the children, so it's bottom-up
		if (symbol == null || this.parserSymbol.equals(symbol)) {
			// if (debug) System.out.println("TRANSFORM: tree matches given symbol: " + symbol + "\n" + this);

			if (subSymbol == null) {
				//	we double-apply becaus sometimes we can match the same subtree again - this should never cause an infinite loop
				TreeNode reduced = transformer.transform(this.parserSymbol, children, null, new ArrayList<TreeNode>());
				if (reduced != null) reduced = reduced.applyTransformer(symbol, subSymbol, matchNonLeafTokens, transformer);
				return reduced;
			} else {
				ArrayList<TreeNode> left = new ArrayList<>();
				ArrayList<TreeNode> right = new ArrayList<>();
				TreeNode subSymbolTree = null;

				//	loop through all the children looking for the given @subSymbol and building the left and right arrays
				for (int i = 0; i < children.size(); i++) {
					TreeNode child = children.get(i);

					//	we haven't found @subSymbol yet...
					if (subSymbolTree == null) {
						if (child.parserSymbol.equals(subSymbol)) {
							//	in some cases, we transform a leaf Token node to be a parent (all binary operators for example)
							//	this is a workaround so that once transformed to be a non-terminal parese tree node, we no longer match it
							if (matchNonLeafTokens || (child.parserSymbol instanceof NonTerminalParserSymbol || child.getChildren().size() == 0)) {
								subSymbolTree = child;
							} else {
								left.add(child);
							}
						} else {
							left.add(child);
						}
					} else {
						right.add(child);
					}
				}

				if (subSymbolTree != null) {
					//	it's a match!  do the transformer and return
					TreeNode reduced = transformer.transform(this.parserSymbol, left, subSymbolTree, right);
					if (reduced != null) reduced = reduced.applyTransformer(symbol, subSymbol, matchNonLeafTokens, transformer);
					return reduced;
				} else {
					//	no match
					return this;
				}
			}
		} else {
			//	didn't match conditions, this node doesn't change
			return this;
		}
	}

	public void reParent() {
		for (TreeNode child : children) {
			child.reParent();
			child.setParent(this);
		}
	}


	public String toString() {
		return toString(0);
		// return "(" + parserSymbol.toString() + children.toString() + ")";
	}
}
