/*
 * Copyright (C) 2012-2020 Gregory Hedlund <https://www.phon.ca>
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		tree.put("cute", "1");
		tree.put("cup", "1");
		tree.put("at", "1");
		tree.put("he", "1");
		tree.put("us", "1");
		tree.put("i", "1");
		
		for(Entry<String, String> entries:tree.entriesForKeysContaining("ABCD 1234", false)) {
			System.out.println(entries.getKey() + " = " + entries.getValue());
		}
		System.out.println(tree.keySet());
	}
	
	public void addCompletion(String path, TernaryTree<String> test) {
		if(!test.containsKey(path) && path.lastIndexOf('.') > 0)
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
	}
	
}
