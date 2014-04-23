import java.util.ArrayList;

public interface RegisterAllocator{
	public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> irCode);
	public void printCode();
}