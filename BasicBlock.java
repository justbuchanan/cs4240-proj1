import java.util.List;

/**
 * A basic block is a sequence of consecutive statements
 * in which flow of control can only enter at the beginning and leave at the end
 *
 * Only the last statement of a basic block can be a branch statement
 * and only the first statement of a basic block can be a target of a branch.
 */
public class BasicBlock {
	private List<CodeStatement> code;
	private int startLineIndex;

	public BasicBlock(int startLineIndex, List<CodeStatement> code) {
		this.startLineIndex = startLineIndex;
		this.code = code;
	}

	public List<CodeStatement> getCode() {
		return code;
	}

	public void setCode(List<CodeStatement> code) {
		this.code = code;
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
