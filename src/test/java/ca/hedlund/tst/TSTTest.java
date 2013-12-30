package ca.hedlund.tst;

import java.util.Map;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TSTTest {

	@Test
	public void test() {
		final TernaryTree<Boolean> tree = new TernaryTree<Boolean>();
		tree.put("hello", true);
		
		tree.put("-ostpy", true);
		tree.put("jelollo", true);
		tree.put("jeflol", true);
		tree.put("tgest", true);
		tree.put("asdfdsafs", true);
		tree.put("back", true);
		tree.put("bacteria", true);
		
		for(Map.Entry<String, Boolean> entry:tree.entriesForKeysEndingWith("lo")) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
	}
	
}
