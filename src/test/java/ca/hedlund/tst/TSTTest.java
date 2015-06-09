package ca.hedlund.tst;

import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TSTTest {

	@Test
	public void test() {
		final TernaryTree<String> tree = new TernaryTree<String>();
		tree.put("hello", "world");
		
		tree.put("-ostpy", "bals");
		tree.put("jelollo", "asdfsadsd");
		tree.put("jeflol","asdfsadsd");
		tree.put("tgest", "world");
		tree.put("asdfdsafs", "bals");
		tree.put("back", "asdfsadsd");
		tree.put("bacteria", "asdfsasdfadsd");
		
		for(Entry<String, String> entries:tree.entriesWithPrefix("je")) {
			System.out.println(entries.getKey() + " = " + entries.getValue());
		}
		for(Entry<String, String> entries:tree.entriesForKeysEndingWith("py")) {
			System.out.println(entries.getKey() + " = " + entries.getValue());
		}
		for(String key:tree.keySet()) {
			System.out.println(key + "= " + tree.get(key));
		}
	}
	
}
