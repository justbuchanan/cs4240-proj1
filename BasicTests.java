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

		Set<String> def = new HashSet<>();
		Set<String> use = new HashSet<>();
		CodeStatement stmt = null;


		stmt = new CodeStatement("add", "outReg", "var1", "var2");
		stmt.getVariableDefsAndUses(def, use);
		assertThat(use, hasItem("var1"));
		assertThat(use, hasItem("var2"));
		assertThat(def, hasItem("outReg"));
	}
}
