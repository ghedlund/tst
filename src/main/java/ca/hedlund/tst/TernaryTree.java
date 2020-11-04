/*
 * Copyright (C) 2012-2020 Gregory Hedlund <https://www.phon.ca>
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import ca.hedlund.tst.TernaryTreeNode.Position;

/**
 * <p>Ternary search tree implementation.  This implementation is thread safe, however
 * the default {@link Map} methods are not synchronized.  To obtain a synchronized
 * version of this {@link Map}, use {@link Collections#synchronizedMap(Map)}.</p>
 */
public class TernaryTree<V> implements Map<String, V> {
	
	/**
	 * Root
	 */
	private TernaryTreeNode<V> root = null;
	
	/**
	 * re-entrant lock
	 */
	private final Lock lock = new ReentrantLock();
	
	/**
	 * Collator
	 */
	private final Comparator<Character> comparator;

	public TernaryTree() {
		this(null);
	}
	
	public TernaryTree(Comparator<Character> comparator) {
		super();
		this.comparator = comparator;
	}
	
	public TernaryTreeNode<V> getRoot() {
		return root;
	}
	
	@Override
	public int size() {
		return keySet().size();
	}

	@Override
	public boolean isEmpty() {
		return values().isEmpty();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return containsKey(key, true);
	}

	public boolean containsKey(Object key, boolean caseSensitive) {
		final TernaryTreeNode<V> node = findNode(key.toString(), caseSensitive, false);
		return (node != null);
	}

	@Override
	public boolean containsValue(Object value) {
		return values().contains(value);
	}

	@Override
	public V get(Object key) {
		final TernaryTreeNode<V> node = findNode(key.toString(), true, false);
		return (node == null ? null : node.getValue());
	}

	@Override
	public V put(String key, V value) {
		final TernaryTreeNode<V> node = findNode(key, true, true);
		return node.setValue(value);
	}

	@Override
	public V remove(Object key) {
		final TernaryTreeNode<V> node = findNode(key.toString(), true, false);
		return (node != null ? node.setValue(null) : null);
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> m) {
		for(String key:m.keySet()) {
			put(key, m.get(key));
		}
	}

	@Override
	public void clear() {
		root = null;
	}

	@Override
	public Set<String> keySet() {
		final AccumulatingNodeVisitor visitor = new AccumulatingNodeVisitor();
		lock.lock();
		final TernaryTreeNode<V> root = getRoot();
		if(root != null)
			root.acceptVisitMiddle(visitor);
		lock.unlock();
		
		LinkedHashSet<String> retVal = new LinkedHashSet<>();
		visitor.nodeSet.stream()
					.map( node -> node.getPrefix() )
					.forEach(retVal::add);
		return retVal;
	}

	@Override
	public Collection<V> values() {
		final AccumulatingNodeVisitor visitor = new AccumulatingNodeVisitor();
		lock.lock();
		final TernaryTreeNode<V> root = getRoot();
		if(root != null)
			root.acceptVisitMiddle(visitor);
		lock.unlock();
		
		ArrayList<V> values = new ArrayList<>();
		visitor.nodeSet.stream()
				.map( node -> node.getValue() )
				.forEach(values::add);
		return values;
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet() {
		final AccumulatingNodeVisitor visitor = new AccumulatingNodeVisitor();
		lock.lock();
		final TernaryTreeNode<V> root = getRoot();
		if(root != null)
			root.acceptVisitMiddle(visitor);
		lock.unlock();
		
		LinkedHashSet<java.util.Map.Entry<String, V>> retVal = new LinkedHashSet<>();
		visitor.nodeSet.stream()
				.map( node -> {
					return new Entry(node.getPrefix(), node.getValue());
				})
				.forEach(retVal::add);
		return retVal;
	}
	
	public Set<String> keysWithPrefix(String prefix) {
		return keysWithPrefix(prefix, true);
	}
	
	public Set<String> keysWithPrefix(String prefix, boolean caseSensitive) {
		final AccumulatingNodeVisitor visitor = new AccumulatingNodeVisitor();
		final TernaryTreeNode<V> node = findNode(prefix, caseSensitive, false);
		lock.lock();
		if(node != null ) {
			if(node.isTerminated())
				visitor.nodeSet.add(node);
			if(node.getCenter() != null)
				node.getCenter().acceptVisitMiddle(visitor);
		}
		lock.unlock();
		
		LinkedHashSet<String> retVal = new LinkedHashSet<>();
		visitor.nodeSet.stream()
				.map( n -> n.getPrefix() )
				.forEach(retVal::add);
		return retVal;
	}
	
	public Collection<V> valuesWithPrefix(String prefix) {
		return valuesWithPrefix(prefix, true);
	}
	
	public Collection<V> valuesWithPrefix(String prefix, boolean caseSensitive) {
		final AccumulatingNodeVisitor visitor = new AccumulatingNodeVisitor();
		final TernaryTreeNode<V> node = findNode(prefix, caseSensitive, false);
		lock.lock();
		if(node != null) {
			if(node.isTerminated()) {
				visitor.nodeSet.add(node);
			}
			if(node.getCenter() != null) {
				node.getCenter().acceptVisitMiddle(visitor);
			}
		}
		lock.unlock();
		
		ArrayList<V> values = new ArrayList<>();
		visitor.nodeSet.stream()
				.map( n -> n.getValue() )
				.forEach(values::add);
		return values;
	}
	
	public Set<java.util.Map.Entry<String, V>> entriesWithPrefix(String prefix) {
		return entriesWithPrefix(prefix, true);
	}
	
	public Set<java.util.Map.Entry<String, V>> entriesWithPrefix(String prefix, boolean caseSensitive) {
		final AccumulatingNodeVisitor visitor = new AccumulatingNodeVisitor();
		final TernaryTreeNode<V> node = findNode(prefix, caseSensitive, false);
		lock.lock();
		if(node != null) {
			if(node.isTerminated()) {
				visitor.nodeSet.add(node);
			}
			if(node.getCenter() != null) {
				node.getCenter().acceptVisitMiddle(visitor);
			}
		}
		lock.unlock();
		
		LinkedHashSet<java.util.Map.Entry<String, V>> retVal = new LinkedHashSet<>();
		visitor.nodeSet.stream()
			.map( n -> {
				return new Entry(n.getPrefix(), n.getValue());
			})
			.forEach(retVal::add);
		return retVal;
	}
	
	public Set<String> keysContaining(String infix) {
		return keysContaining(infix, true);
	}
	
	public Set<String> keysContaining(String infix, boolean caseSensitive) {
		final NodeContainsVisitor visitor = new NodeContainsVisitor(infix, caseSensitive);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitMiddle(visitor);
		}
		lock.unlock();
		
		LinkedHashSet<String> retVal = new LinkedHashSet<>();
		visitor.nodeSet.stream()
				.map( n -> n.getPrefix() )
				.forEach(retVal::add);
		return retVal;
	}
	
	public Collection<V> valuesForKeysContaining(String infix) {
		return valuesForKeysContaining(infix, true);
	}
	
	public Collection<V> valuesForKeysContaining(String infix, boolean caseSensitive) {
		final NodeContainsVisitor visitor = new NodeContainsVisitor(infix, caseSensitive);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitMiddle(visitor);
		}
		lock.unlock();
		
		ArrayList<V> values = new ArrayList<>();
		visitor.nodeSet.stream()
				.map( n -> n.getValue() )
				.forEach(values::add);
		return values;
	}
	
	public Set<java.util.Map.Entry<String, V>> entriesForKeysContaining(String infix) {
		return entriesForKeysContaining(infix, true);
	}
	
	public Set<java.util.Map.Entry<String, V>> entriesForKeysContaining(String infix, boolean caseSensitive) {
		final NodeContainsVisitor visitor = new NodeContainsVisitor(infix, caseSensitive);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitMiddle(visitor);
		}
		lock.unlock();
		
		LinkedHashSet<java.util.Map.Entry<String, V>> retVal = new LinkedHashSet<>();
		visitor.nodeSet.stream()
			.map( n -> {
				return new Entry(n.getPrefix(), n.getValue());
			})
			.forEach(retVal::add);
		return retVal;
	}
	
	public Set<String> keysEndingWith(String suffix) {
		return keysEndingWith(suffix, true);
	}
	
	public Set<String> keysEndingWith(String suffix, boolean caseSensitive) {
		final KeyEndsWithVisitor visitor = new KeyEndsWithVisitor(suffix, caseSensitive);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitMiddle(visitor);
		}
		lock.unlock();
		return visitor.getResult();
	}
	
	public Collection<V> valuesForKeysEndingWith(String suffix) {
		return valuesForKeysEndingWith(suffix, true);
	}
	
	public Collection<V> valuesForKeysEndingWith(String suffix, boolean caseSensitive) {
		final ValuesForKeyEndsWithVisitor visitor = new ValuesForKeyEndsWithVisitor(suffix, caseSensitive);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitMiddle(visitor);
		}
		lock.unlock();
		return visitor.getResult();
	}
	
	public Set<Map.Entry<String, V>> entriesForKeysEndingWith(String suffix) {
		return entriesForKeysEndingWith(suffix, true);
	}
	
	public Set<Map.Entry<String, V>> entriesForKeysEndingWith(String suffix, boolean caseSensitive) {
		final EntriesForKeyEndsWithVisitor visitor = new EntriesForKeyEndsWithVisitor(suffix, caseSensitive);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitMiddle(visitor);
		}
		lock.unlock();
		return visitor.getResult();
	}
	
	/**
	 * Find the node for the given key
	 * 
	 * @param key
	 * 
	 * @return the node for the given key or
	 *  <code>null</code> if not found
	 */
	TernaryTreeNode<V> findNode(String key, boolean caseSensitive, boolean create) {
		if(key.length() == 0) return null;
		TernaryTreeNode<V> retVal = null;
		
		lock.lock();
		
		TernaryTreeNode<V> prevNode = null;
		TernaryTreeNode<V> currentNode = getRoot();
		int charIndex = 0;
		Position lastPos = Position.EQUAL;
		while(true) {
			Character keyChar = key.charAt(charIndex);
			if(currentNode == null) {
				if(create) {
					final TernaryTreeNode<V> newNode = new TernaryTreeNode<V>(prevNode, keyChar);
					if(prevNode == null)
						root = newNode;
					else
						prevNode.setChild(newNode, lastPos);
					currentNode = newNode;
				} else {
					break;			
				}
			}
			prevNode = currentNode;
			
			Character splitChar = currentNode.getChar();
			Character c1 = (caseSensitive ? keyChar : Character.toLowerCase(keyChar));
			Character c2 = (caseSensitive ? splitChar : Character.toLowerCase(splitChar));
			int cmp = (comparator != null
						? comparator.compare(c1, c2) 
						: c1.compareTo(c2));
			
			if(cmp == 0) {
				charIndex++;
				if(charIndex == key.length()) {
					retVal = currentNode;
					break;
				}
				currentNode = currentNode.getCenter();
				lastPos = Position.EQUAL;
			} else if(cmp < 0) {
				currentNode = currentNode.getLeft();
				lastPos = Position.LOW;
			} else if(cmp > 0) {
				currentNode = currentNode.getRight();
				lastPos = Position.HIGH;
			}
		}
		
		lock.unlock();
		
		return retVal;
	}
	
	/* Internal Visitors */
	private abstract class ContainsVisitor<T> implements TernaryTreeNodeVisitor<V> {

		private final String txt;
		
		private boolean caseSensitive = true;
		
		public ContainsVisitor(String txt, boolean caseSensitive) {
			this.txt = txt;
			this.caseSensitive = caseSensitive;
		}
		
		@Override
		public boolean visit(TernaryTreeNode<V> node) {
			if(txt.length() == 0) return false;
			
			final char ch = txt.charAt(txt.length() - 1);
			boolean matches = (caseSensitive ? node.getChar() == ch : Character.toLowerCase(node.getChar()) == Character.toLowerCase(ch));
			
			if(matches) {
				final String prefix = node.getPrefix();
				matches = (caseSensitive ? prefix.endsWith(txt) : prefix.toLowerCase().endsWith(txt.toLowerCase()));
				if(matches) {
					accept(node);
					return true;
				}
			}
			return false;
		}
		
		public abstract T getResult();
	
		public abstract void accept(TernaryTreeNode<V> node);
		
	}
	
	private class NodeContainsVisitor extends ContainsVisitor<Set<TernaryTreeNode<V>>> {

		final Set<TernaryTreeNode<V>> nodeSet = new LinkedHashSet<TernaryTreeNode<V>>();
		
		public NodeContainsVisitor(String txt, boolean caseSensitive) {
			super(txt, caseSensitive);
		}

		@Override
		public Set<TernaryTreeNode<V>> getResult() {
			return nodeSet;
		}

		@Override
		public void accept(TernaryTreeNode<V> node) {
			final AccumulatingNodeVisitor visitor = new AccumulatingNodeVisitor();
			node.acceptVisitOnlyCenter(visitor);
			nodeSet.addAll(visitor.nodeSet);
		}
		
	}
	
	private abstract class EndsWithVisitor<T> implements TernaryTreeNodeVisitor<V> {
		
		private String txt;
		
		private boolean caseSensitive = true;
		
		public EndsWithVisitor(String txt, boolean caseSensitive) {
			this.txt = txt;
			this.caseSensitive = caseSensitive;
		}
		
		public abstract T getResult();
		
		public abstract void accept(TernaryTreeNode<V> node);
		
		@Override
		public boolean visit(TernaryTreeNode<V> node) {
			if(txt.length() == 0) return false;
			
			final char ch = txt.charAt(txt.length() - 1);
			boolean matches = (caseSensitive ? node.getChar() == ch : Character.toLowerCase(node.getChar()) == Character.toLowerCase(ch));
			
			if(matches) {
				final String prefix = node.getPrefix();
				matches = (caseSensitive ? prefix.endsWith(txt) : prefix.toLowerCase().endsWith(txt.toLowerCase()));
				if(matches && node.isTerminated()) {
					accept(node);
					return true;
				}
			}
			return false;
		}
	}
	
	
	private class KeyEndsWithVisitor extends EndsWithVisitor<Set<String>> {

		final Set<String> keySet = new LinkedHashSet<String>();
		
		public KeyEndsWithVisitor(String txt, boolean caseSensitive) {
			super(txt, caseSensitive);
		}

		@Override
		public Set<String> getResult() {
			return keySet;
		}

		@Override
		public void accept(TernaryTreeNode<V> node) {
			keySet.add(node.getPrefix());
		}
		
	}

	private class ValuesForKeyEndsWithVisitor extends EndsWithVisitor<Collection<V>> {
		
		public ValuesForKeyEndsWithVisitor(String txt, boolean caseSensitive) {
			super(txt, caseSensitive);
		}
		
		private final Collection<V> values = new ArrayList<V>();
		
		@Override
		public Collection<V> getResult() {
			return values;
		}
		
		@Override
		public void accept(TernaryTreeNode<V> node) {
			values.add(node.getValue());
		}
		
	}
	
	private class EntriesForKeyEndsWithVisitor extends EndsWithVisitor<Set<Map.Entry<String, V>>> {
		
		private Set<Map.Entry<String, V>> entrySet = new LinkedHashSet<Map.Entry<String,V>>();
		
		public EntriesForKeyEndsWithVisitor(String txt, boolean caseSensitive) {
			super(txt, caseSensitive);
		}

		@Override
		public Set<java.util.Map.Entry<String, V>> getResult() {
			return entrySet;
		}

		@Override
		public void accept(TernaryTreeNode<V> node) {
			entrySet.add(new Entry(node.getPrefix(), node.getValue()));
		}
		
	}
	
	private class AccumulatingNodeVisitor implements TernaryTreeNodeVisitor<V> {
		final Set<TernaryTreeNode<V>> nodeSet = new LinkedHashSet<TernaryTreeNode<V>>();

		@Override
		public boolean visit(TernaryTreeNode<V> node) {
			if(node.isTerminated()) {
				nodeSet.add(node);
			}
			return false;
		}
		
	}
	
	private class Entry implements Map.Entry<String, V> {
		
		private final String key;
		
		private V value;

		public Entry(String key, V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public String getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V object) {
			final V oldVal = this.value;
			this.value = object;
			return oldVal;
		}
		
	}
}
