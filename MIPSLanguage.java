import java.util.HashSet;
import java.util.Set;


public class MIPSLanguage implements Language{
	private Set<String> allInstr;
	private Set<String> threeRegInstr;
	private Set<String> twoRegInstr;
	private Set<String> oneRegInstr;
	
	public MIPSLanguage(){
		generateAllInstr();
		generateThreeRegInstr();
		generateTwoRegInstr();
		
	}
	
	@Override
	public Set<String> getAllInstr() {
		return allInstr;
	}

	@Override
	public Set<String> getThreeRegInstr() {
		return threeRegInstr;
	}

	@Override
	public Set<String> getTwoRegInstr() {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public int numRegInstr(String instr) {
		if(threeRegInstr.contains(instr)) return 3;
		//else if(twoRegInstr.contains(instr)) return 2;
		//else if(oneRegInstr.contains(instr)) return 1;
		else return 0;
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
	}
	
	private void generateTwoRegInstr(){
		
	}
	
	private void generateAllInstr(){
		
	}
}
