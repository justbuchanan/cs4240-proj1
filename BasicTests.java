import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import org.junit.Test;

public class BasicTests {
	@Test
	public void testDefUseExtraction() {
		EbbRegisterAllocator ebbAlloc = new EbbRegisterAllocator();

		Set<String> def = new HashSet<>();
		Set<String> use = new HashSet<>();
		CodeStatement stmt = null;


		stmt = new CodeStatement("add", "outReg", "var1", "var2");
		ebbAlloc.getVariableDefsAndUses(stmt, def, use);
		assertThat(use, hasItem("var1"));
		assertThat(use, hasItem("var2"));
		assertThat(def, hasItem("outReg"));
	}
}
