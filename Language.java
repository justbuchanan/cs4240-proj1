import java.util.Set;


public interface Language {
	public Set<String> getThreeRegInstr();
	public Set<String>getTwoRegInstr();
	public int numRegInstr(String instr);
	public CodeStatement load(String dr, String addr);
	public CodeStatement store(String sr, String addr);
	public boolean resultStored(String instr);
	public boolean storesToLo(String instr);
}
