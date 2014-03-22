import java.util.ArrayList;

public class TreeNode {
	private TreeNode parent;
	private ArrayList<TreeNode> children;
	private ParserSymbol parserSymbol;

	public TreeNode(TreeNode parent, NonTerminalParserSymbol nonTerminal) {
		this.parent = parent;
		parserSymbol = (ParserSymbol) nonTerminal;
		children = new ArrayList<>();
	}

	public TreeNode(TreeNode parent, Token token) {
		this.parent = parent;
		parserSymbol = (ParserSymbol) token;
		children = new ArrayList<>();
	}
	
	public void addChild(NonTerminalParserSymbol nonTerminal) {
		children.add(new TreeNode(this, nonTerminal));
	}
	
	public void addChild(Token token) {
		children.add(new TreeNode(this, token));
	}

	public ArrayList<TreeNode> getChildren() {
		return children;
	}

	public ParserSymbol getSymbol() {
		return parserSymbol;
	}

	public TreeNode getParent() {
		return parent;
	}

	public String toString(int depth) {
		String prefix = "|";
		for (int i = 1; i < depth; i++) {
			prefix += " |";
		}
		
		String desc = prefix;
		desc += children.size() > 0 ? "-+= " : "--= ";
		desc += parserSymbol.toString() + "\n";
		for (TreeNode childNode : children) {
			desc += childNode.toString(depth + 1);
		}

		return desc;
	}
}
