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
		tree.put("stick", "world");
		tree.put("bat", "1");
		tree.put("batter", "1");
		tree.put("art", "1");
		tree.put("orb", "1");
		tree.put("artist", "1");
		tree.put("artup", "1");
		tree.put("arbitrary", "1");

		return tree;
	}

	@Test
	public void testTerminatedNodeIterator() {
		final TernaryTree<String> tree = createTestTree();

		final TerminatedNodeIterator<String> itr = new TerminatedNodeIterator<>(tree);
		Assert.assertTrue(itr.hasNext());
		Assert.assertEquals("empty", itr.next().getValue());
		itr.reset();
		int cnt = 0;
		while(itr.hasNext()) {
			System.out.println(itr.next().getPrefix());
			++cnt;
		}
		Assert.assertEquals(tree.size(), cnt);
	}

	@Test
	public void testTerminatedNodeIteratorWithFilter() {
		final TernaryTree<String> tree = createTestTree();

		final TerminatedNodeIterator<String> itr = new TerminatedNodeIterator<>(tree, (node) -> tree.get(node.getPrefix()).matches("[0-9]+"));
		while(itr.hasNext()) {
			System.out.println(itr.next().getPrefix());
		}
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
