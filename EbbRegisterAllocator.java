
/**
 * A register-allocator based on extended basic blocks
 */
public class EbbRegisterAllocator implements RegisterAllocator {
	Language lang;
	private int registerCount;
	private ArrayList<CodeStatement> finalCode;
	
	public EbbRegisterAllocator(Language lang){
		this.lang = lang;
		this.registerCount = 5;	//	FIXME: set registerCount
	}

	public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> origMips) {
		ControlFlowGraph cfg = new ControlFlowGraph(origMips);

		for (ExtendedBasicBlock ebb : cfg.getExtendedBasicBlocks()) {
			for (BasicBlock bb : ebb.getBasicBlocks()) {
				//	TODO
			}
		}

		//	TODO
	}

	public void printCode() {
		for(CodeStatement stmt : finalCode){
			System.out.println(stmt.toString());
		}
	}
}
