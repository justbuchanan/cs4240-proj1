import java.util.ArrayList;

public class ParseTree {
	private TreeNode root = null;
	private TreeNode current = null;
	private int size;

	public ParseTree() {
		size = 0;
	}

	public void newLevel(NonTerminalParserSymbol nonTerminal) {
		if(root == null) {
			root = new TreeNode(null, nonTerminal);
			current = root;
		} else {
			boolean continueFlag = true;
			while(continueFlag) {
				ArrayList<TreeNode> children = current.getChildren();
				for (TreeNode treeNode : children) {
					if(treeNode.getSymbol().equals(nonTerminal)) {
						current = treeNode;
						continueFlag = false;
						break;
					}
				}
				if(continueFlag) {
					current = current.getParent();
				}
			}
		}
	}

	public void add(ParserSymbol parserSymbol) {
		size++;
		if(parserSymbol.isTerminal()) {
			current.addChild((Token) parserSymbol);
		} else {
			current.addChild((NonTerminalParserSymbol) parserSymbol);
		}
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
}
