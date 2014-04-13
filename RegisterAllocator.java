import java.util.ArrayList;

public interface RegisterAllocator{
	public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> origMips);
	public void printCode();
}