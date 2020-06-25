package ca.hedlund.tst;

import java.io.File;
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
		
//		tree.put("-ostpy", "bals");
//		tree.put("jelollo", "asdfsadsd");
//		tree.put("jeflol","asdfsadsd");
//		tree.put("tgest", "world");
//		tree.put("asdfdsafs", "bals");
//		tree.put("back", "asdfsadsd");
//		tree.put("bacteria", "asdfsasdfadsd");
		
		tree.put("ABCD 1231 This is a test", "1");
		tree.put("ABCD 1232 This is A test", "1");
		tree.put("ABCD 123", "1");
		tree.put("ABCD 1233 This is B test", "1");
		tree.put("ABCD 1234 This is C test", "1");
		tree.put("ABCD 1235 This is D test", "1");
		
//		for(Entry<String, String> entries:tree.entriesWithPrefix("jE", false)) {
//			System.out.println(entries.getKey() + " = " + entries.getValue());
//		}
		for(Entry<String, String> entries:tree.entriesForKeysContaining("ABCD 123", false)) {
			System.out.println(entries.getKey() + " = " + entries.getValue());
		}
		
//		for(Entry<String, String> entries:tree.entriesForKeysEndingWith("py")) {
//			System.out.println(entries.getKey() + " = " + entries.getValue());
//		}
//		for(String key:tree.keySet()) {
//			System.out.println(key + "= " + tree.get(key));
//		}
	}
	
	public void addCompletion(String path, TernaryTree<String> test) {
		if(!test.containsKey(path))
			test.put(path.substring(0, path.lastIndexOf('.')), "");
	}
	
	public void scanFolder(File folder, TernaryTree<String> tree) {
		if(folder.isDirectory()) {
			for(File file:folder.listFiles()) {
				if(file.isDirectory()) 
					scanFolder(file, tree);
				else
					addCompletion(file.getName(), tree);
			}
		} else {
			addCompletion(folder.getName(), tree);
		}
	}
	
	@Test
	public void test2() {
		final TernaryTree<String> tree = new TernaryTree<String>();
		tree.put("hello", "world");
		
		scanFolder(new File("/Volumes/Samsung_T5/Movies"), tree);
		for(Entry<String, String> entries:tree.entriesForKeysContaining("The Golden Girls S01E07", false)) {
			System.out.println(entries.getKey() + " = " + entries.getValue());
		}
		
//		for(Entry<String, String> entries:tree.entriesForKeysEndingWith("py")) {
//			System.out.println(entries.getKey() + " = " + entries.getValue());
//		}
//		for(String key:tree.keySet()) {
//			System.out.println(key + "= " + tree.get(key));
//		}
	}
	
}
