import java.util.Set;


public interface Language {
	public Set<String> getAllInstr();
	public Set<String> getThreeRegInstr();
	public Set<String>getTwoRegInstr();
	public int numRegInstr(String instr);
}
