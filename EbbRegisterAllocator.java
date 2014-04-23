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
		this.finalCode = new ArrayList<>();
	}

	public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> irCode) {
		boolean debug = true;

		ControlFlowGraph cfg = new ControlFlowGraph(irCode);

		//	defs and uses indexed by BB index
		ArrayList<Set<String>> bbDefs = new ArrayList<>();
		ArrayList<Set<String>> bbUses = new ArrayList<>();

		//	find defs and uses for all the basic blocks
		for (int i = 0; i < cfg.getBasicBlocks().size(); i++) {
			Set<String> defs = new HashSet<>();
			Set<String> uses = new HashSet<>();

			BasicBlock bb = cfg.getBasicBlocks().get(i);

			for (CodeStatement stmt : bb.getCode()) {
				stmt.getVariableDefsAndUses(defs, uses);
			}

			bbDefs.add(defs);
			bbUses.add(uses);
		}


		//	debug print defs and uses
		if (debug) {
			for (int i = 0; i < bbDefs.size(); i++) {
				System.out.println("BasicBlock" + i + ": defs=" + bbDefs.get(i) + "; uses=" + bbUses.get(i));
			}
			System.out.println();
		}




		return finalCode;
	}


	public void printCode() {
		for(CodeStatement stmt : finalCode){
			System.out.println(stmt.toString());
		}
	}
}
