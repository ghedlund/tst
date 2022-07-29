package ca.hedlund.tst;

import java.util.*;

public class AccumulatingNodeVisitor<V> implements TernaryTreeNodeVisitor<V> {

	final Set<TernaryTreeNode<V>> nodeSet = new LinkedHashSet<TernaryTreeNode<V>>();

	@Override
	public boolean visit(TernaryTreeNode<V> node) {
		if(node.isTerminated()) {
			nodeSet.add(node);
		}
		return false;
	}

}
