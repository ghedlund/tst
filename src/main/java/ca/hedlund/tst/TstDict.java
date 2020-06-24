package ca.hedlund.tst;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * <p>Special use-case for {@link TernaryTree}s where values are
 * {@link String}s.  The strings are stored in a second TernaryTree
 * for memory conservation.  This is especially useful for large
 * dictionaries where values are repeated, share common prefixes are are inserted
 * in <i>random</i> order.</p>
 *
 */
public class TstDict implements Map<String, String> {
	
	/**
	 * key tree
	 */
	private final TernaryTree<TernaryTreeNode<Integer>> keyTree = 
			new TernaryTree<TernaryTreeNode<Integer>>();

	/**
	 * value tree
	 */
	private final TernaryTree<Integer> valueTree =
			new TernaryTree<Integer>();
	
	@Override
	public void clear() {
		keyTree.clear();
		valueTree.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		final TernaryTreeNode<Integer> valueNode = keyTree.get(key);
		return valueNode != null;
	}

	@Override
	public boolean containsValue(Object value) {
		final Integer numRefs = valueTree.get(value);
		return (numRefs != null && numRefs > 0);
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get(Object key) {
		final TernaryTreeNode<Integer> node = keyTree.get(key);
		if(node != null) {
			return node.getPrefix();
		} else {
			return null;
		}
	}

	@Override
	public boolean isEmpty() {
		return keyTree.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return keyTree.keySet();
	}

	@Override
	public String put(String key, String value) {
		String retVal = null;
		final TernaryTreeNode<Integer> oldValueNode = keyTree.get(key);
		if(oldValueNode != null) {
			retVal = oldValueNode.getPrefix();
			// decrement references to values
			oldValueNode.setValue(oldValueNode.getValue()-1);
		}
		
		// increment number of references to value
		Integer numRefs = valueTree.get(value);
		if(numRefs == null) {
			numRefs = 0;
		}
		++numRefs;
		valueTree.put(value, numRefs);
		
		final TernaryTreeNode<Integer> valueNode = valueTree.findNode(value, true, false);
		keyTree.put(key, valueNode);
		
		return retVal;
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		for(String key:m.keySet()) {
			final String v = m.get(key);
			put(key, v);
		}
	}

	@Override
	public String remove(Object key) {
		String retVal = null;
		final TernaryTreeNode<Integer> oldValueNode = keyTree.get(key);
		if(oldValueNode != null) {
			retVal = oldValueNode.getPrefix();
			// decrement references to values
			oldValueNode.setValue(oldValueNode.getValue()-1);
		}
		
		final TernaryTreeNode<TernaryTreeNode<Integer>> keyNode = keyTree.findNode(key.toString(), true, false);
		keyNode.setValue(null);
		
		return retVal;
	}

	@Override
	public int size() {
		return keyTree.size();
	}

	@Override
	public Collection<String> values() {
		return valueTree.keySet();
	}

}
