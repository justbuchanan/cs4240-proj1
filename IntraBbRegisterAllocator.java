import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class IntraBbRegisterAllocator implements RegisterAllocator {
	private int registerCount;
	private ArrayList<CodeStatement> finalCode;

	public IntraBbRegisterAllocator() {
		this.registerCount = 30;
		this.finalCode = new ArrayList<>();
	}

	public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> irCode) {
		boolean debug = true;

		ArrayList<ArrayList<Set<String>>> allLiveIns = new ArrayList<>();
		ArrayList<ArrayList<Set<String>>> allLiveOuts = new ArrayList<>();

		ControlFlowGraph cfg = new ControlFlowGraph(irCode);
		for (BasicBlock bb : cfg.getBasicBlocks()) {

			//	live in/out sets by line index
			ArrayList<Set<String>> liveInsForBlock = new ArrayList<>();
			ArrayList<Set<String>> liveOutsForBlock = new ArrayList<>();
			allLiveIns.add(liveInsForBlock);
			allLiveOuts.add(liveOutsForBlock);

			ArrayList<Set<String>> defsForBlock = new ArrayList<>();
			ArrayList<Set<String>> usesForBlock = new ArrayList<>();

			for (CodeStatement stmt : bb.getCode()) {

				Set<String> liveIn = new HashSet<>();
				Set<String> liveOut = new HashSet<>();
				liveInsForBlock.add(liveIn);
				liveOutsForBlock.add(liveOut);

				Set<String> defs = new HashSet<>();
				Set<String> uses = new HashSet<>();
				stmt.getVariableDefsAndUses(defs, uses);

				defsForBlock.add(defs);
				usesForBlock.add(uses);

				//	vars used in this line are live-in here
				liveIn.addAll(uses);
			}

			int prevCount = deepCount(liveInsForBlock) + deepCount(liveOutsForBlock);
			while (true) {
				for (int i = bb.getCode().size() - 2; i >= 0; i--) {
					Set<String> out = liveOutsForBlock.get(i);
					Set<String> nextIn = liveInsForBlock.get(i+1);
					out.addAll(nextIn);

					Set<String> in = liveInsForBlock.get(i);
					in.addAll(out);
					in.removeAll(defsForBlock.get(i));
					in.addAll(usesForBlock.get(i));
				}

				int newCount = deepCount(liveInsForBlock) + deepCount(liveOutsForBlock);
				if (newCount == prevCount) {
					break;
				}
				prevCount = newCount;
			}
		}


		//	print live in/out sets by block and line
		if (debug) {
			for (int i = 0; i < cfg.getBasicBlocks().size(); i++) {
				System.out.println("BasicBlock" + i);

				BasicBlock bb = cfg.getBasicBlocks().get(i);
				for (int j = 0; j < bb.getCode().size(); j++) {
					System.out.println("\t" + bb.getCode().get(j) + ";    in=" + allLiveIns.get(i).get(j) + "; out=" + allLiveOuts.get(i).get(j) + ";");
				}

				System.out.println();
			}
		}




		//	TODO




		//	squash the blocks back together into linear code
		for (BasicBlock bb : cfg.getBasicBlocks()) {
			finalCode.addAll(bb.getCode());
		}

		return finalCode;
	}


	public int deepCount(ArrayList<Set<String>> arr) {
		int count = 0;
		for (Set<String> set : arr) {
			count += set.size();
		}
		return count;
	}



	public void printCode() {
		for(CodeStatement stmt : finalCode){
			System.out.println(stmt.toString());
		}
	}
}
