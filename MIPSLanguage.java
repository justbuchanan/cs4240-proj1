import java.util.HashSet;
import java.util.Set;


public class MIPSLanguage implements Language{
	private Set<String> threeRegInstr;
	private Set<String> twoRegInstr;
	private Set<String> oneRegInstr;
	private Set<String> resultStored;
	private Set<String> storesLo;
	
	public MIPSLanguage(){
		generateThreeRegInstr();
		generateTwoRegInstr();
		generateOneRegInstr();
		generateStoresLoInstr();
		generateResultStored();
	}
	

	@Override
	public Set<String> getThreeRegInstr() {
		return threeRegInstr;
	}

	@Override
	public Set<String> getTwoRegInstr() {
		return twoRegInstr;
	}
	

	@Override
	public int numRegInstr(String instr) {
		if(threeRegInstr.contains(instr)) return 3;
		else if(twoRegInstr.contains(instr)) return 2;
		else if(oneRegInstr.contains(instr)) return 1;
		else return 0;
	}
	
	@Override
	public CodeStatement load(String dr, String addr) {
		return new CodeStatement("lw", dr, addr);
	}

	@Override
	public CodeStatement store(String sr, String addr) {
		return new CodeStatement("sw", sr, addr);
	}
	
	@Override
	public boolean resultStored(String instr){
		return resultStored.contains(instr);
	}
	
	public boolean storesToLo(String instr){
		return storesLo.contains(instr);
	}
	
	private void generateThreeRegInstr(){
		threeRegInstr = new HashSet();
		//div is speciail case?
		threeRegInstr.add("add");
		threeRegInstr.add("addu");
		threeRegInstr.add("and");
		threeRegInstr.add("or");
		threeRegInstr.add("slt");
		threeRegInstr.add("sub");
		threeRegInstr.add("subu");
		threeRegInstr.add("xor");
		threeRegInstr.add("sub");
	}
	
	private void generateTwoRegInstr(){
		twoRegInstr = new HashSet();
		twoRegInstr.add("addi");
		twoRegInstr.add("addiu");
		twoRegInstr.add("andi");
		twoRegInstr.add("beq");
		twoRegInstr.add("bne");
		twoRegInstr.add("div");
		twoRegInstr.add("divu");
		twoRegInstr.add("mult");
		twoRegInstr.add("multu");
		twoRegInstr.add("ori");
		twoRegInstr.add("xori");
	}
	
	private void generateOneRegInstr(){
		oneRegInstr = new HashSet();
		oneRegInstr.add("bgez");
		oneRegInstr.add("bgezal");
		oneRegInstr.add("bgtz");
		oneRegInstr.add("blez");
		oneRegInstr.add("bltz");
		oneRegInstr.add("bltzal");
		oneRegInstr.add("jr");
		oneRegInstr.add("blez");
		oneRegInstr.add("blez");
		oneRegInstr.add("blez");
		oneRegInstr.add("blez");
		oneRegInstr.add("blez");
		oneRegInstr.add("la");
		oneRegInstr.add("mfhi");
		oneRegInstr.add("mflo");
	}
	
	private void generateResultStored(){
		resultStored = new HashSet();
		resultStored.add("add");
		resultStored.add("addi");
		resultStored.add("addiu");
		resultStored.add("addu");
		resultStored.add("and");
		resultStored.add("andi");
		resultStored.add("div");
		resultStored.add("divu");
		resultStored.add("mult");
		resultStored.add("multu");
		resultStored.add("or");
		resultStored.add("ori");
		resultStored.add("sub");
		resultStored.add("subu");
		resultStored.add("xor");
		resultStored.add("xori");
		resultStored.add("la");
		resultStored.add("mfhi");
		resultStored.add("mflo");
	}
	
	private void generateStoresLoInstr(){
		storesLo = new HashSet();
		storesLo.add("div");
		storesLo.add("divu");
		storesLo.add("mult");
		storesLo.add("multu");
	}
	
}
