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

	/**
	 * Removes ALL occurrences of the given Token from the node and all of it's subnodes
	 */
	public void removeTerminal(State terminal) {
		for (int i = 0; i < children.size();) {
			ParserSymbol childSymbol = children.get(i).getSymbol();
			if (childSymbol instanceof Token) {
				if (((Token)childSymbol).type().equals(terminal)) {
					children.remove(i);	//	delete the token
					continue;	//	reloop and examine the current index again, which will contain a different child node
				}
			} else {
				children.get(i).removeTerminal(terminal);
			}

			//	we only move to the next index if we didn't just delete a token
			i++;
		}
	}
}
