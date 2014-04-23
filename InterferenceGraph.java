import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Collection;
import java.lang.Exception;

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

	public Map<T, String> color(ArrayList<String> colors) throws TooFewColorsException {
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
			throw new TooFewColorsException("The allocator doesn't yet handle splitting...");
		}

		//	remove nodes from the stack, assigning colors as we go
		while (stack.size() > 0) {
			T n = stack.pop();

			Set<T> coloredNeighbors = getNeighbors(nodes.indexOf(n));
			coloredNeighbors.removeAll(stack);

			ArrayList<String> availableColors = new ArrayList<>(colors);
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

	//	there may be many with the same # of neighbors, this just returns one of them
	public T getNodeWithMostNeighbors() {
		int maxNeighbors = -1;
		int nodeIndex = -1;

		for (int i = 0; i < nodes.size(); i++) {
			int neighborCount = getNeighbors(i).size();
			if (neighborCount > maxNeighbors) {
				maxNeighbors = neighborCount;
				nodeIndex = i;
			}
		}

		return nodeIndex != -1 ? nodes.get(nodeIndex) : null;
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
