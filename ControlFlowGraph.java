import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

public class ControlFlowGraph {
	private static Set<String> branchInstructions;
	private static Set<String> conditionalBranchInstructions;
	private ArrayList<BasicBlock> basicBlocks;
	private ArrayList<ExtendedBasicBlock> extendedBasicBlocks;
	private Set<CFGEdge> basicBlockEdges;


	private class CFGEdge {
		public int from;
		public int to;

		CFGEdge(int from, int to) {
			this.from = from;
			this.to = to;
		}

		public String toString() {
			return from + " <--> " + to;
		}

		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof CFGEdge)) return false;
			CFGEdge other = (CFGEdge) obj;
			return other.from == from && other.to == to;
		}

		public int hashCode() {
			return from + to;
		}
	}

	//	un-conditional branches
	public Set<String> branchInstructions() {
		if (branchInstructions == null) {
			branchInstructions = new HashSet<>();
			branchInstructions.add("goto");
			branchInstructions.add("return");
			branchInstructions.add("call");
			branchInstructions.add("callr");
		}

		return branchInstructions;
	}

	public Set<String> conditionalBranchInstructions() {
		if (conditionalBranchInstructions == null) {
			conditionalBranchInstructions = new HashSet<>();
			conditionalBranchInstructions.add("breq");
			conditionalBranchInstructions.add("brneq");
			conditionalBranchInstructions.add("brlt");
			conditionalBranchInstructions.add("brgeq");
			conditionalBranchInstructions.add("brleq");
		}

		return conditionalBranchInstructions;
	}

	public ControlFlowGraph(ArrayList<CodeStatement> irCode) {
		this.basicBlocks = new ArrayList<>();
		this.basicBlockEdges = new HashSet<>();
		this.extendedBasicBlocks = new ArrayList<>();

		//	contaions both conditional and non-conditional branches
		Map<Integer, String> branches = new HashMap<>();

		Set<Integer> conditionalBranches = new HashSet<>();
		Map<String, Integer> labelLocations = new HashMap<>();

		//	first pass over the code:
		//	* find where all the labels are
		//	* find where all the branching statements are
		for (int i = 0; i < irCode.size(); i++) {
			CodeStatement stmt = irCode.get(i);

			if (stmt.isLabel()) {
				labelLocations.put(stmt.getLabelName(), new Integer(i));
			} else if (!stmt.isEmpty()) {
				//	see if it's a branch statement
				if (branchInstructions().contains(stmt.getOperator())) {
					String targetLabel = targetLabelForBranchStatement(stmt);
					branches.put(new Integer(i), targetLabel);
				} else if (conditionalBranchInstructions().contains(stmt.getOperator())) {
					String targetLabel = targetLabelForBranchStatement(stmt);
					branches.put(new Integer(i), targetLabel);
					conditionalBranches.add(new Integer(i));
				}
			}
		}

		//	build the set of @branchTargets
		Set<Integer> branchTargets = new HashSet<Integer>();
		for (Iterator it = branches.entrySet().iterator(); it.hasNext();) {
			Map.Entry pair = (Map.Entry)it.next();

			String label = (String) pair.getValue();
			if (label != null) {
				Integer lineNumber = labelLocations.get(label);
				if (lineNumber == null) {
					throw new RuntimeException("There's a break to label '" + label + "' that doesn't exist!");
				}

				branchTargets.add(lineNumber);
			}
		}

		//	ir code pass 2:
		//	* based on branchTargets and branchInstructions,
		//	  build a list of basic blocks
		int blockStartIndex = 0;
		for (int i = 0; i < irCode.size(); i++) {
			if (branchTargets.contains(new Integer(i))) {
				if (blockStartIndex != i) {
					//	make a block
					List<CodeStatement> code = irCode.subList(blockStartIndex, i);
					BasicBlock block = new BasicBlock(blockStartIndex, code);
					this.basicBlocks.add(block);

					blockStartIndex = i;
				}
			} else if (branches.containsKey(new Integer(i))) {
				//	make a block
				List<CodeStatement> code = irCode.subList(blockStartIndex, i + 1);
				BasicBlock block = new BasicBlock(blockStartIndex, code);
				this.basicBlocks.add(block);

				blockStartIndex = i + 1;
			}
		}

		//	create the edges!
		//	every branch corresponds to 0-2 edges
		for (Integer lineIndex : branches.keySet()) {
			CodeStatement stmt = irCode.get(lineIndex);
			String targetLabel = targetLabelForBranchStatement(stmt);

			//	add an edge to where the branch goes to
			if (targetLabel != null) {
				Integer targetLineIndex = labelLocations.get(targetLabel);

				basicBlockEdges.add(new CFGEdge(
					basicBlockIndexForLineIndex(lineIndex),
					basicBlockIndexForLineIndex(targetLineIndex)));
			}

			//	if it's a conditional branch, draw an edge to the next block in sequence
			if (conditionalBranches.contains(lineIndex)) {
				basicBlockEdges.add(new CFGEdge(
					basicBlockIndexForLineIndex(lineIndex),
					basicBlockIndexForLineIndex(lineIndex + 1)));
			}
		}

		//	add edges between chunks in sequence
		for (int i = 0; i < basicBlocks.size() - 1; i++) {
			List<CodeStatement> codeBlock = basicBlocks.get(i).getCode();
			CodeStatement lastStmt = codeBlock.get(codeBlock.size() - 1);
			if (!branchInstructions().contains(lastStmt) && !conditionalBranchInstructions().contains(lastStmt)) {
				basicBlockEdges.add(new CFGEdge(i, i + 1));
			}
		}


		//	Build extended basic blocks

		//	if it's got more than one edge leading into it, it has to be an ebb root
		Set<BasicBlock> unclaimedBlocks = new HashSet<>();
		unclaimedBlocks.addAll(this.basicBlocks);
		// ArrayList<BasicBlock> ebbRoots = new ArrayList<>();
		
		for (BasicBlock bb : basicBlocks) {
			int count = entryEdgeCount(bb);
			if ((count > 1 || count == 0) && unclaimedBlocks.contains(bb)) {
				//	the leading block or a block with multiple inputs
				unclaimedBlocks.remove(bb);
				buildEbb(bb, unclaimedBlocks);
			}
		}
	}

	//	count the number of edges that point into @bb
	int entryEdgeCount(BasicBlock bb) {
		int count = 0;
		for (CFGEdge edge : basicBlockEdges) {
			if (basicBlocks.get(edge.to) == bb) {
				count++;
			}
		}
		return count;
	}

	//	builds an extended basic block from the given root block
	//	adds the final EBB to the extendedBasicBlocks list
	//	removes any blocks  it uses form the unclaimed blocks set
	private ExtendedBasicBlock buildEbb(BasicBlock root, Set<BasicBlock> unclaimedBlocks) {
		ArrayList<BasicBlock> bbs = new ArrayList<>();
		bbs.add(root);

		for (CFGEdge edge : basicBlockEdges) {
			BasicBlock bb = basicBlocks.get(edge.to);
			if (bbs.contains(basicBlocks.get(edge.from)) && unclaimedBlocks.contains(bb)) {
				if (entryEdgeCount(bb) == 1) {
					bbs.add(bb);
					unclaimedBlocks.remove(bb);
				}
			}
		}

		ExtendedBasicBlock ebb = new ExtendedBasicBlock(bbs);
		extendedBasicBlocks.add(ebb);
		return ebb;
	}

	private String targetLabelForBranchStatement(CodeStatement stmt) {
		if (stmt.getOperator().equals("goto")) {
			return stmt.getOutputRegister();
		} else if (stmt.getOperator().equals("return")) {
			return null;
		} else if (stmt.getOperator().equals("call") || stmt.getOperator().equals("callr")) {
			//	FIXME: this probably isn't right
			return null;
		} else {
			return stmt.getRightOperand();
		}
	}

	private int basicBlockIndexForLineIndex(int lineIndex) {
		for (int i = 0; i < basicBlocks.size(); i++) {
			if (basicBlocks.get(i).getStartLineIndex() > lineIndex) {
				return i - 1;
			}
		}

		return basicBlocks.size() - 1;
	}

	public ArrayList<ExtendedBasicBlock> getExtendedBasicBlocks() {
		return extendedBasicBlocks;
	}

	public Set<CFGEdge> getEdges() {
		return basicBlockEdges;
	}


	public String toGraphviz() {
		String gv = "digraph ControlFlow {\n";

		// gv += "\tlabel=\"Control Flow Graph\";\n";

		int clusterNumber = 0;
		for (ExtendedBasicBlock ebb : extendedBasicBlocks) {
			gv += "\tsubgraph cluster" + (clusterNumber++) + "{\n";
			gv += "\tstyle=dashed;\n";

			for (BasicBlock bb : ebb.getBasicBlocks()) {
				gv += "\t\t " + bb.toGraphviz() + "\n";
			}

			gv += "\t}\n";
		}


		//	nodes
		for (BasicBlock block : basicBlocks) {
			gv += "\t" + block.toGraphviz() + "\n";
		}

		gv += "\n";

		//	edges
		for (CFGEdge edge : basicBlockEdges) {
			gv += "\t" + basicBlocks.get(edge.from).graphvizNodeName() + " -> " + basicBlocks.get(edge.to).graphvizNodeName() + "\n";
		}

		gv += "}\n";

		return gv;
	}

	/**
	 * A basic block is a sequence of consecutive statements
	 * in which flow of control can only enter at the beginning and leave at the end
	 *
	 * Only the last statement of a basic block can be a branch statement
	 * and only the first statement of a basic block can be a target of a branch.
	 */
	private class BasicBlock {
		private List<CodeStatement> code;
		private int startLineIndex;

		public BasicBlock(int startLineIndex, List<CodeStatement> code) {
			this.startLineIndex = startLineIndex;
			this.code = code;
		}

		public List<CodeStatement> getCode() {
			return code;
		}

		public int getStartLineIndex() {
			return startLineIndex;
		}

		//	outputs a node (note: doesn't indent at all)
		public String toGraphviz() {
			String gv = graphvizNodeName();

			gv += " [shape=record label=\"";

			for (CodeStatement stmt : code) {
				gv += stmt + "\\n";
			}

			gv += "\"];";

			return gv;
		}

		public String graphvizNodeName() {
			return "block" + getStartLineIndex();
		}
	}

	private class ExtendedBasicBlock {
		private List<BasicBlock> basicBlocks;

		ExtendedBasicBlock(List<BasicBlock> basicBlocks) {
			this.basicBlocks = basicBlocks;
		}

		ExtendedBasicBlock() {
			this(new ArrayList<BasicBlock>());
		}

		public List<BasicBlock> getBasicBlocks() {
			return basicBlocks;
		}
	}
}
