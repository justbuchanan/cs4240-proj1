import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * A register-allocator based on extended basic blocks
 */
public class EbbRegisterAllocator implements RegisterAllocator {
	private int registerCount;
	private ArrayList<CodeStatement> finalCode;
	
	public EbbRegisterAllocator() {
		this.registerCount = 30;	//	FIXME: set registerCount
	}

	public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> irCode) {
		ControlFlowGraph cfg = new ControlFlowGraph(irCode);


		// ArrayList<VarUsageWeb> webs = new ArrayList<>();


		// //	loop over all BasicBlocks
		// //	for each def-ed variable, we add the start of a web to @webs
		// for (ControlFlowGraph.ExtendedBasicBlock ebb : cfg.getExtendedBasicBlocks()) {
		// 	for (ControlFlowGraph.BasicBlock bb : ebb.getBasicBlocks()) {

		// 		for (int i = 0; i < bb.getCode().size(); i++) {
		// 			CodeStatement stmt = bb.getCode().get(i);
		// 			Set<String> defs = new HashSet<>();
		// 			Set<String> uses = new HashSet<>();
		// 			getVariableDefsAndUses(stmt, defs, uses);


		// 			for (String deffedVar : defs) {
		// 				VarUsageWeb webStart = new VarUsageWeb(deffedVar, bb, i);
		// 				webs.add(webStart);
		// 			}



		// 			//	note: only the first basic block in the EBB can have more than one predecessor


		// 			//	FIXME: use the defs and uses
		// 		}

		// 	}
		// }

		//	TODO

		return finalCode;
	}



	public class InterferenceGraph<T> {
		private ArrayList<T> nodes;

		//	an edge is a Set containing two nodes
		//	this allows the undirected-ness to be ignored with .equals()
		private Set<Edge> edges;


		private class Edge {
			public int a, b;

			public Edge(int a , int b) {
				this.a = a;
				this.b = b;
			}

			public boolean equals(Object obj) {
				if (obj == null || !(obj.getClass() != this.getClass())) {
					return false;
				}

				Edge other = (Edge) obj;
				return (other.a == a && other.b == a) || (other.b == a && other.a == b);
			}

			public int hashCode() {
				return a*a + b*b;
			}

			public boolean isConnectedTo(int nodeIdx) {
				return a == nodeIdx || b == nodeIdx;
			}
		}


		InterferenceGraph() {
			this.nodes = new ArrayList<>();
			this.edges = new HashSet<>();
		}

		public Map<T, String> color(Set<String> colors) {
			Stack<T> stack = new Stack<>();
			Set<T> unhandledNodes = new HashSet<>(nodes);
			Map<T, String> assignments = new HashMap<>();

			//	go through all nodes.  if a given node has < colors.size() edges,
			//	remove it from @unhandledNodes and push it onto the stack
			Iterator<T> itr = unhandledNodes.iterator();
			while (itr.hasNext()) {
				T n = itr.next();
				int i = nodes.indexOf(n);
				int count = countEdges(i, stack);

				if (count < colors.size()) {
					stack.push(n);
					itr.remove();
				}
			}

			if (unhandledNodes.size() > 0) {
				//	TODO: implement splitting or spilling here
				throw new RuntimeException("The allocator doesn't yet handle splitting...");
			}

			//	remove nodes from the stack, assigning colors as we go
			while (stack.size() > 0) {
				T n = stack.pop();

				Set<T> coloredNeighbors = getNeighbors(nodes.indexOf(n));
				coloredNeighbors.removeAll(stack);

				Set<String> availableColors = new HashSet<>(colors);
				for (T coloredNeighbor : coloredNeighbors) {
					availableColors.remove(assignments.get(coloredNeighbor));
				}

				if (availableColors.size() == 0) {
					throw new RuntimeException("Oops... there's no available colors");
				}

				//	choose a random color to assign
				String colorToAssign = availableColors.iterator().next();
				assignments.put(n, colorToAssign);
			}

			return assignments;
		}

		public Set<T> getNeighbors(int nodeNum) {
			Set<T> neighbors = new HashSet<>();

			for (Edge e : edges) {
				if (e.a == nodeNum) {
					neighbors.add(nodes.get(e.b));
				} else if (e.b == nodeNum) {
					neighbors.add(nodes.get(e.a));
				}
			}

			return neighbors;
		}

		public int countEdges(int nodeNum, Collection<T> exclude) {
			int count = 0;

			for (Edge e : edges) {
				if ( (e.a == nodeNum && !exclude.contains(nodes.get(e.b)))
					|| (e.b == nodeNum && !exclude.contains(nodes.get(e.a))) ) {
					count++;
				}
			}

			return count;
		}

		public void addNode(T node) {
			nodes.add(node);
		}

		public void addEdge(T from, T to) {
			edges.add(new Edge(nodes.indexOf(from), nodes.indexOf(to)));
		}

		public String toGraphviz() {
			String gv = "graph interferenceGraph {\n";

			for (int i = 0; i < nodes.size(); i++) {
				gv += "\tnode" + i + "[label=\"" + nodes.get(i).toString() + "\"];\n";
			}

			gv += "\n";

			for (Edge e : edges) {
				gv += "\tnode" + e.a + " -> node" + e.b + ";\n";
			}

			gv += "}";

			return gv;
		}
	}


	// private class VarUsageWeb {

	// 	/**
	// 	 * Create a web beginning with a def at the given location
	// 	 */
	// 	public VarUsageWeb(String varName, BasicBlock bb, int lineNumber) {

	// 	}

	// 	public void addUse(BasicBlock bb, int lineNumber) {

	// 	}


	// 	// public VarUsageNode addNode(String varName, int liveRangeIndex) {


	// 	// }

	// 	// public VarUsageEdge connect(VarUsageNode node1, VarUsageNode node2);


	// 	// public class VarUsageEdge {

	// 	// }

	// 	// public class VarUsageNode {
	// 	// 	public int basicBlockIndex;
	// 	// 	public int lineIndex;		//	index into the basic blocks
	// 	// }
	// }






	/**
	 * Examines the given code statement and finds the variables defined and used, returning them through the output variables.
	 */
	public void getVariableDefsAndUses(CodeStatement stmt, Set<String> defOut, Set<String> useOut) {
		if (stmt == null || defOut == null || useOut == null) {
			throw new IllegalArgumentException("Parameters should not be null");
		}


		if (!stmt.isLabel() && !stmt.isEmpty()) {

			ArrayList<String> condBranchOps = new ArrayList<String>(
				Arrays.asList(new String[]{
					"breq",
					"brneq",
					"brlt",
					"brgt",
					"brgeq",
					"brleq"
				})
			);


			String operator = stmt.getOperator();

			if (operator.equals("assign")) {
				if (stmt.getRightOperand().equals("")) {
					//	this is an assignment to a regular variable
					defOut.add(stmt.getOutputRegister());
				} else {
					//	this is an assignment to set the whole contents of an array
					//	FIXME: is this a def?  arrays are different...
				}
			} else if (operator.equals("return")) {
				if (stmt.getOutputRegister().equals("")) {
					//	return operation with no ret val
				} else {
					//	return a value
					useOut.add(stmt.getOutputRegister());
				}
			} else if (operator.equals("call")) {
				useOut.addAll(stmt.getFuncParams());
			} else if (operator.equals("callr")) {
				defOut.add(stmt.getOutputRegister());
				useOut.addAll(stmt.getFuncParams());
			} else if (condBranchOps.contains(stmt.getOperator())) {
				//	these are of the form:
				//	condBranchOp var1, var2, gotoLabel
				//	we add var1 and var2 to the use set

				//	yes, the method names are wierd - they were named for the general case, which isn't this case
				useOut.add(stmt.getOutputRegister());
				useOut.add(stmt.getLeftOperand());
			} else {
				//	general case: "op outVar, inVar1, inVar2"
				defOut.add(stmt.getOutputRegister());
				useOut.add(stmt.getLeftOperand());
				useOut.add(stmt.getRightOperand());
			}


			//	remove any CONST numbers from @useOut
			Iterator<String> itr = useOut.iterator();
			while (itr.hasNext()) {
				String var = itr.next();
				char firstChar = var.charAt(0);
				if ('0' <= firstChar && firstChar <= '9') {
					itr.remove();
				}
			}
		}
	}


	public void printCode() {
		for(CodeStatement stmt : finalCode){
			System.out.println(stmt.toString());
		}
	}
}
