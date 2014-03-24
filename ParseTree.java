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
		ParseTree parseTree = this;
		removeNonTerminal(null, new NonTerminalParserSymbol(NonTerminals.CONST));

		//	un-needed terminals
		removeTerminal(State.$);
		removeTerminal(State.SEMI);
		removeTerminal(State.COMMA);

		return parseTree;
	}

	public void removeNonTerminal(TreeNode treeNodeVar, NonTerminalParserSymbol symbol) {
		if (treeNodeVar == null) {
			treeNodeVar = root;
		}
	
		for (TreeNode treeNode : treeNodeVar.getChildren()) {
			if(!treeNode.getSymbol().isTerminal() && ((NonTerminalParserSymbol) treeNode.getSymbol()).equals(symbol)) {
				treeNode.getParent().setChildren(treeNode.getChildren());
				removeNonTerminal(treeNode, symbol);
			} else if(!treeNode.getSymbol().isTerminal()) {
				removeNonTerminal(treeNode, symbol);
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

	/**
	 * Removes ALL occurrences of the given Token from the tree
	 */
	public void removeTerminal(State terminal) {
		if (root != null) {
			root.removeTerminal(terminal);
		}
	}
}
