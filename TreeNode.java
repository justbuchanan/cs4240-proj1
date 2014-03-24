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

	public void setChildren(ArrayList<TreeNode> children) {
		this.children = children;
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

	public TreeNode applyTransformer(ParserSymbol symbol, ParserSymbol subSymbol, TreeTransformer transformer) {
		boolean debug = true;

		if (debug) System.out.println("TreeNode.applyTransformer()");

		for (int i = 0; i < children.size();) {
			TreeNode child = children.get(i);
			TreeNode newChild = child.applyTransformer(symbol, subSymbol, transformer);

			//	replace the old subtree with the new
			if (newChild != child) {
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
			if (debug) System.out.println("TRANSFORM: tree matches given symbol: " + symbol + "\n" + this);

			if (subSymbol == null) {
				return transformer.transform(this.parserSymbol, children, null, new ArrayList<TreeNode>());
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
							subSymbolTree = child;
						} else {
							left.add(child);
						}
					} else {
						right.add(child);
					}
				}

				if (subSymbolTree != null) {
					//	it's a match!  do the transformer and return
					return transformer.transform(this.parserSymbol, left, subSymbolTree, right);
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
}
