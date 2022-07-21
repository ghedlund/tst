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

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TSTTest {

	public TernaryTree<String> createTestTree() {
		final TernaryTree<String> tree = new TernaryTree<String>();

		tree.put("", "empty");
		tree.put("hello", "world");
		tree.put("cute", "1");
		tree.put("acup", "1");
		tree.put("at", "1");
		tree.put("he", "1");
		tree.put("us", "1");
		tree.put("i", "1");

		return tree;
	}

	@Test
	public void testNodePaths() {
		final TernaryTree<String> tree = createTestTree();

		for(Entry<String, String> entry:tree.entrySet()) {
			Optional<TernaryTreeNode<String>> node = tree.findNode(entry.getKey());
			Assert.assertTrue(node.isPresent());
			TernaryTreeNodePath path = node.get().getPath();
			Optional<TernaryTreeNode<String>> followPath = path.followPath(tree.getRoot());
			Assert.assertTrue(followPath.isPresent());
			Assert.assertEquals(node.get(), followPath.get());
		}
	}

	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		final TernaryTree<String> tree = createTestTree();

		// write tree
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);

		oout.writeObject(tree);
		oout.flush();
		oout.close();

		// read tree
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream oin = new ObjectInputStream(bin);

		final TernaryTree<String> tree2 = (TernaryTree<String>) oin.readObject();

		for(Entry<String, String> entry:tree.entrySet()) {
			Optional<TernaryTreeNode<String>> node = tree.findNode(entry.getKey());
			Assert.assertTrue(node.isPresent());
			Assert.assertTrue(node.get().isTerminated());
			TernaryTreeNodePath nodePath = node.get().getPath();

			Optional<TernaryTreeNode<String>> node2 = tree2.findNode(entry.getKey());
			Assert.assertTrue(node2.isPresent());
			Assert.assertTrue(node2.get().isTerminated());
			Assert.assertEquals(entry.getValue(), node2.get().getValue());
			TernaryTreeNodePath node2Path = node2.get().getPath();

			Assert.assertArrayEquals(nodePath.toByteArray(), node2Path.toByteArray());
		}
	}

}
