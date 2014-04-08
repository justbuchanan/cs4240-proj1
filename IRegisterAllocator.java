import java.util.ArrayList;

public interface IRegisterAllocator{
	public ArrayList<CodeStatement> allocRegisters(ArrayList<CodeStatement> origMips);
}