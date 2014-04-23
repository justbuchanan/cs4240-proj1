import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class IntraBbRegisterAllocator implements RegisterAllocator {
	private int registerCount;
	private int spillRegister;	//	the register used for spilled variables
	private ArrayList<CodeStatement> finalCode;

	public IntraBbRegisterAllocator() {
		this.registerCount = 28;
		this.spillRegister = 31;
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
					break;	//	when we've gone an iteration w/out changing, we're done
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

		//	["$r1", "$r2"...]
		ArrayList<String> availableRegisters = new ArrayList<>();
		for (int i = 1; i <= registerCount; i++) {
			availableRegisters.add("$r" + i);
		}
 
 		if (debug) {
			System.out.println("Registers: " + availableRegisters);
			System.out.println();
		}


		//	build an interference graph for each block and assign registers
		for (int bbIdx = 0; bbIdx < cfg.getBasicBlocks().size(); bbIdx++) {

			ArrayList<String> spilledVars = new ArrayList();
			Map<String, String> registerAllocations = new HashMap();

			while (true) {
				InterferenceGraph<String> ifg = new InterferenceGraph<>();

				//	build a set of all the variables in use in this block
				Set<String> allBlockVars = new HashSet<>();
				ArrayList<Set<String>> blockLiveIns = allLiveIns.get(bbIdx);
				for (Set<String> vars : blockLiveIns) {
					allBlockVars.addAll(vars);
				}

				//	remove spilled vars from this set
				allBlockVars.removeAll(spilledVars);

				//	add all of these vars to the ifg
				for (String var : allBlockVars) {
					ifg.addNode(var);
				}

				//	add edges between vars that are alive at the same time
				ArrayList<String> allBlockVarsList = new ArrayList<>(allBlockVars);
				for (int i = 0; i < allBlockVarsList.size() - 1; i++) {
					for (int j = i + 1; j < allBlockVarsList.size(); j++) {
						ifg.addEdge(allBlockVarsList.get(i), allBlockVarsList.get(j));
					}
				}

				//	allocate registers
				try {
					registerAllocations = ifg.color(availableRegisters);
					break;
				}
				catch (TooFewColorsException exc) {
					//	spill a variable and run the loop again!
					String varToSpill = ifg.getNodeWithMostNeighbors();
					spilledVars.add(varToSpill);
				}
			}
			
			if (debug) {
				System.out.println("Block" + bbIdx);
				System.out.println("\tspilled vars = " + spilledVars);
				System.out.println("\tregister allocations = " + registerAllocations);
			}

			//	FIXME: handle load / store for spilled variables!!!


			BasicBlock bb = cfg.getBasicBlocks().get(bbIdx);

			//	replace occurrences of variables with their respective registers
			for (int stmtIdx = 0; stmtIdx < bb.getCode().size(); stmtIdx++) {
				CodeStatement stmt = bb.getCode().get(stmtIdx);
				stmt.replaceVariableOccurrences(registerAllocations);
			}

			//	loads and stores for all variables
			ArrayList<CodeStatement> loads = new ArrayList();
			ArrayList<CodeStatement> stores = new ArrayList();
			for (Map.Entry<String, String> pair : registerAllocations.entrySet()) {
				CodeStatement load = new CodeStatement("load", pair.getValue(), pair.getKey());		//	load $reg, var
				loads.add(load);

				CodeStatement store = new CodeStatement("store", pair.getKey(), pair.getValue());	//	store var, $reg
				stores.add(store);
			}

			//	merge the loads, old code, and stores into a new array
			ArrayList<CodeStatement> newCode = new ArrayList();
			newCode.addAll(bb.getCode());

			if (newCode.get(0).isLabel()) {
				newCode.addAll(1, loads);
			} else {
				newCode.addAll(0, loads);
			}

			int lastIdx = bb.getCode().size() - 1;
			if (cfg.statementIsBranch(bb.getCode().get(lastIdx))) {
				newCode.addAll(lastIdx, stores);	//	add @stores right before the branch statement
			} else {
				newCode.addAll(stores);				//	add @stores to the end
			}
			bb.setCode(newCode);

			// System.out.println("LOADS: " + loads.size());
		}

		//	squash the blocks back together into linear code
		for (BasicBlock bb : cfg.getBasicBlocks()) {
			finalCode.addAll(bb.getCode());
		}


		// for (CodeStatement stmt : finalCode) {
		// 	System.out.println("CODE: " + stmt);
		// }


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
