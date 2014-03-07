package ca.hedlund.tst;

import java.util.Map;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TSTTest {

	@Test
	public void test() {
		final TstDict tree = new TstDict();
		tree.put("hello", "world");
		
		tree.put("-ostpy", "bals");
		tree.put("jelollo", "asdfsadsd");
		tree.put("jeflol","asdfsadsd");
		tree.put("tgest", "world");
		tree.put("asdfdsafs", "bals");
		tree.put("back", "asdfsadsd");
		tree.put("bacteria", "asdfsasdfadsd");
		
		for(String key:tree.keySet()) {
			System.out.println("key = " + tree.get(key));
		}
	}
	
}
