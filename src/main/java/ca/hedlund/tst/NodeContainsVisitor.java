package ca.hedlund.tst;

import java.util.*;

public class NodeContainsVisitor<V> extends ContainsVisitor<Set<TernaryTreeNode<V>>, V> {

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
