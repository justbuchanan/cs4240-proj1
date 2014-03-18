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

	public String getRoot() {
		String returnString = "";
		ArrayList<String> outputList = new ArrayList<>();
		outputList.add(new String(""));
		boolean continueFlag = true;
		TreeNode curr = root;
		ArrayList<TreeNode> children = curr.getChildren();
		System.out.println(children.size());
		int currentCount = 1;
		System.out.println(curr.getSymbol().toString());
		System.out.println("\n" + curr.getSymbol().toString());
		for (TreeNode treeNode : children) {
			System.out.println(treeNode.getSymbol().toString());
			if (!treeNode.getSymbol().isTerminal()) {				
				System.out.println("\n" + treeNode.getSymbol().toString());
				helper(treeNode);
			}
		}


		return returnString;
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
}
