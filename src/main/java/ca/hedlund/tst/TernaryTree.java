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
		final TernaryTreeNode<V> node = findNode(key.toString(), false);
		return (node != null);
	}

	@Override
	public boolean containsValue(Object value) {
		return values().contains(value);
	}

	@Override
	public V get(Object key) {
		final TernaryTreeNode<V> node = findNode(key.toString(), false);
		return (node == null ? null : node.getValue());
	}

	@Override
	public V put(String key, V value) {
		final TernaryTreeNode<V> node = findNode(key, true);
		return node.setValue(value);
	}

	@Override
	public V remove(Object key) {
		final TernaryTreeNode<V> node = findNode(key.toString(), false);
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
		final KeyVisitor visitor = new KeyVisitor();
		lock.lock();
		final TernaryTreeNode<V> root = getRoot();
		if(root != null)
			root.acceptVisitLast(visitor);
		lock.unlock();
		return visitor.keySet;
	}

	@Override
	public Collection<V> values() {
		final ValuesVisitor visitor = new ValuesVisitor();
		lock.lock();
		final TernaryTreeNode<V> root = getRoot();
		if(root != null)
			root.acceptVisitLast(visitor);
		lock.unlock();
		return visitor.values;
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet() {
		final EntryVisitor visitor = new EntryVisitor();
		lock.lock();
		final TernaryTreeNode<V> root = getRoot();
		if(root != null)
			root.acceptVisitLast(visitor);
		lock.unlock();
		return visitor.values;
	}
	
	public Set<String> keysWithPrefix(String prefix) {
		final KeyVisitor visitor = new KeyVisitor();
		final TernaryTreeNode<V> node = findNode(prefix, false);
		lock.lock();
		if(node != null ) {
			if(node.isTerminated())
				visitor.keySet.add(node.getPrefix());
			if(node.getCenter() != null)
				node.getCenter().acceptVisitLast(visitor);
		}
		lock.unlock();
		return visitor.keySet;
	}
	
	public Collection<V> valuesWithPrefix(String prefix) {
		final ValuesVisitor visitor = new ValuesVisitor();
		final TernaryTreeNode<V> node = findNode(prefix, false);
		lock.lock();
		if(node != null) {
			if(node.isTerminated()) {
				visitor.values.add(node.getValue());
			}
			if(node.getCenter() != null) {
				node.getCenter().acceptVisitLast(visitor);
			}
		}
		lock.unlock();
		return visitor.values;
	}
	
	public Set<java.util.Map.Entry<String, V>> entriesWithPrefix(String prefix) {
		final EntryVisitor visitor = new EntryVisitor();
		final TernaryTreeNode<V> node = findNode(prefix, false);
		lock.lock();
		if(node != null) {
			if(node.isTerminated()) {
				visitor.values.add(new Entry(node.getPrefix(), node.getValue()));
			}
			if(node.getCenter() != null) {
				node.getCenter().acceptVisitLast(visitor);
			}
		}
		lock.unlock();
		return visitor.values;
	}
	
	public Set<String> keysContaining(String infix) {
		final KeyContainsVisitor visitor = new KeyContainsVisitor(infix);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitFirst(visitor);
		}
		lock.unlock();
		return visitor.getResult();
	}
	
	public Collection<V> valuesForKeysContaining(String infix) {
		final ValuesForKeyContainsVisitor visitor = new ValuesForKeyContainsVisitor(infix);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitFirst(visitor);
		}
		lock.unlock();
		return visitor.getResult();
	}
	
	public Set<java.util.Map.Entry<String, V>> entriesForKeysContaining(String infix) {
		final EntriesForKeyContainsVisitor visitor = new EntriesForKeyContainsVisitor(infix);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitFirst(visitor);
		}
		lock.unlock();
		return visitor.getResult();
	}
	
	public Set<String> keysEndingWith(String suffix) {
		final KeyEndsWithVisitor visitor = new KeyEndsWithVisitor(suffix);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitLast(visitor);
		}
		lock.unlock();
		return visitor.getResult();
	}
	
	public Collection<V> valuesForKeysEndingWith(String suffix) {
		final ValuesForKeyEndsWithVisitor visitor = new ValuesForKeyEndsWithVisitor(suffix);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitLast(visitor);
		}
		lock.unlock();
		return visitor.getResult();
	}
	
	public Set<Map.Entry<String, V>> entriesForKeysEndingWith(String suffix) {
		final EntriesForKeyEndsWithVisitor visitor = new EntriesForKeyEndsWithVisitor(suffix);
		lock.lock();
		if(getRoot() != null) {
			getRoot().acceptVisitLast(visitor);
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
	TernaryTreeNode<V> findNode(String key, boolean create) {
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
			int cmp = (comparator != null
						? comparator.compare(keyChar, splitChar) 
						: keyChar.compareTo(splitChar));
			
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
		
		public ContainsVisitor(String txt) {
			this.txt = txt;
		}
		
		@Override
		public boolean visit(TernaryTreeNode<V> node) {
			if(txt.length() == 0) return false;
			
			final char ch = txt.charAt(txt.length() - 1);
			if(node.getChar() == ch) {
				final String prefix = node.getPrefix();
				if(prefix.endsWith(txt)) {
					accept(node);
					return true;
				}
			}
			return false;
		}
		
		
		public abstract T getResult();
	
		public abstract void accept(TernaryTreeNode<V> node);
		
	}
	
	private class KeyContainsVisitor extends ContainsVisitor<Set<String>> {

		final Set<String> keySet = new LinkedHashSet<String>();
		
		public KeyContainsVisitor(String txt) {
			super(txt);
		}

		@Override
		public Set<String> getResult() {
			return keySet;
		}

		@Override
		public void accept(TernaryTreeNode<V> node) {
			final KeyVisitor visitor = new KeyVisitor();
			node.acceptVisitLast(visitor);
			keySet.addAll(visitor.keySet);
		}
		
	}
	
	private class ValuesForKeyContainsVisitor extends ContainsVisitor<Collection<V>> {

		public ValuesForKeyContainsVisitor(String txt) {
			super(txt);
		}

		private final Collection<V> values = new ArrayList<V>();
		
		@Override
		public Collection<V> getResult() {
			return values;
		}

		@Override
		public void accept(TernaryTreeNode<V> node) {
			final ValuesVisitor visitor = new ValuesVisitor();
			node.acceptVisitLast(visitor);
			values.addAll(visitor.values);
		}
		
	}
	
	private class EntriesForKeyContainsVisitor extends ContainsVisitor<Set<Map.Entry<String, V>>> {
		
		private Set<Map.Entry<String, V>> entrySet = new LinkedHashSet<Map.Entry<String,V>>();
		
		public EntriesForKeyContainsVisitor(String txt) {
			super(txt);
		}

		@Override
		public Set<java.util.Map.Entry<String, V>> getResult() {
			return entrySet;
		}

		@Override
		public void accept(TernaryTreeNode<V> node) {
			final EntryVisitor visitor = new EntryVisitor();
			node.acceptVisitLast(visitor);
			entrySet.addAll(visitor.values);
		}
		
	}
	
	private abstract class EndsWithVisitor<T> implements TernaryTreeNodeVisitor<V> {
		
		private String txt;
		
		public EndsWithVisitor(String txt) {
			this.txt = txt;
		}
		
		public abstract T getResult();
		
		public abstract void accept(TernaryTreeNode<V> node);
		
		@Override
		public boolean visit(TernaryTreeNode<V> node) {
			if(txt.length() == 0) return false;
			
			final char ch = txt.charAt(txt.length() - 1);
			if(node.getChar() == ch) {
				final String prefix = node.getPrefix();
				if(prefix.endsWith(txt) && node.isTerminated()) {
					accept(node);
					return true;
				}
			}
			return false;
		}
	}
	
	
	private class KeyEndsWithVisitor extends EndsWithVisitor<Set<String>> {

		final Set<String> keySet = new LinkedHashSet<String>();
		
		public KeyEndsWithVisitor(String txt) {
			super(txt);
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
		
		public ValuesForKeyEndsWithVisitor(String txt) {
			super(txt);
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
		
		public EntriesForKeyEndsWithVisitor(String txt) {
			super(txt);
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
	
	private class KeyVisitor implements TernaryTreeNodeVisitor<V> {
		final Set<String> keySet = new LinkedHashSet<String>();

		@Override
		public boolean visit(TernaryTreeNode<V> node) {
			if(node.isTerminated()) {
				keySet.add(node.getPrefix());
			}
			return false;
		}
		
	}
	
	private class ValuesVisitor implements TernaryTreeNodeVisitor<V> {
		final Collection<V> values = new ArrayList<V>();

		@Override
		public boolean visit(TernaryTreeNode<V> node) {
			if(node.isTerminated())
				values.add(node.getValue());
			return false;
		}
	}
	
	private class EntryVisitor implements TernaryTreeNodeVisitor<V> {
		final Set<Map.Entry<String, V>> values = new LinkedHashSet<Map.Entry<String, V>>();

		@Override
		public boolean visit(TernaryTreeNode<V> node) {
			if(node.isTerminated()) {
				final Entry entry = new Entry(node.getPrefix(), node.getValue());
				values.add(entry);
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
