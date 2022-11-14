package ca.hedlund.tst;

import java.util.Iterator;
import java.util.function.*;

/**
 * Iterate terminated tree nodes with optional filter
 *
 * @param <V>
 */
public class TerminatedNodeIterator<V> implements Iterator<TernaryTreeNode<V>> {

	private enum Branch {
		Left,
		Center,
		Right,
		Undefined;

		public Branch nextBranch() {
			switch(this) {
				case Left:
					return Center;

				case Center:
					return Right;

				default:
					return Undefined;
			}
		}
	}

	private final TernaryTree<V> tree;

	private TernaryTreeNode<V> startNode;

	private TernaryTreeNode<V> currentNode;

	private Branch currentBranch;

	private boolean prefixSearch;

	private Predicate<TernaryTreeNode<V>> filter;

	public TerminatedNodeIterator(TernaryTree<V> tree) {
		this(tree, (node) -> true);
	}

	public TerminatedNodeIterator(TernaryTree<V> tree, Predicate<TernaryTreeNode<V>> filter) {
		this(tree, tree.getRoot(), filter, false);
	}

	public TerminatedNodeIterator(TernaryTree<V> tree, TernaryTreeNode<V> node) {
		this(tree, node, (n) -> true, false);
	}

	/**
	 *
	 * @param tree
	 * @param node
	 * @param filter
	 * @param prefixSearch if true will search for values where this node is the prefix
	 */
	public TerminatedNodeIterator(TernaryTree<V> tree, TernaryTreeNode<V> node, Predicate<TernaryTreeNode<V>> filter, boolean prefixSearch) {
		super();
		this.tree = tree;
		this.startNode = node;
		this.filter = filter;
		this.prefixSearch = prefixSearch;
		this.currentBranch = Branch.Center;
	}

	private Branch getBranch(TernaryTreeNode<V> parent, TernaryTreeNode<V> child) {
		if(parent == null)
			return Branch.Undefined;
		if(parent.getLeft() == child)
			return Branch.Left;
		if(parent.getCenter() == child)
			return Branch.Center;
		if(parent.getRight() == child)
			return Branch.Right;
		return null;
	}

	public TernaryTreeNode<V> getStartNode() {
		return this.startNode;
	}

	public void setStartNode(TernaryTreeNode<V> startNode) {
		this.startNode = startNode;
	}

	public TernaryTreeNode<V> getCurrentNode() {
		return this.currentNode;
	}

	public void setCurrentNode(TernaryTreeNode<V> currentNode) {
		this.currentNode = currentNode;
		this.currentBranch = Branch.Center;
	}

	public boolean isPrefixSearch() {
		return this.prefixSearch;
	}

	public void setPrefixSearch(boolean prefixSearch) {
		this.prefixSearch = prefixSearch;
	}

	public void reset() {
		this.currentNode = null;
		this.currentBranch = Branch.Center;
	}

	private NextNodeReturn nextNode() {
		TernaryTreeNode<V> node = this.currentNode != null ? this.currentNode : this.startNode;
		Branch branch = this.currentBranch;

		if(this.currentNode == null && node == this.startNode && node.isTerminated()) {
			return new NextNodeReturn(node, branch);
		}
		boolean loopCnd = true;
		do {
			switch(branch) {
				case Left:
					if(node.getLeft() != null) {
						node = node.getLeft();
						continue;
					} else if(node.isTerminated() && filter.test(node)) {
						return new NextNodeReturn(node, branch.nextBranch());
					}

				case Center:
					if(node.getCenter() != null) {
						branch = Branch.Left;
						node = node.getCenter();
						continue;
					}

				case Right:
					if(this.prefixSearch && node == this.startNode) {
						return null;
					}
					if(node.getRight() != null) {
						branch = Branch.Left;
						node = node.getRight();
						continue;
					}
			}
			if(!node.isRoot()) {
				Branch childBranch = getBranch(node.getParent(), node);
				if (childBranch == Branch.Left && node.getParent().isTerminated() && filter.test(node.getParent())) {
					this.currentBranch = childBranch.nextBranch();
					return new NextNodeReturn(node.getParent(), childBranch.nextBranch());
				}
				node = node.getParent();
				branch = childBranch.nextBranch();
			} else {
				branch = Branch.Undefined;
			}

			if(this.prefixSearch) {
				loopCnd = !(node == this.startNode && branch == Branch.Right);
			} else {
				loopCnd = !(node == tree.getRoot() && branch == Branch.Undefined);
			}
		} while(loopCnd);

		return null;
	}

	@Override
	public boolean hasNext() {
		final NextNodeReturn nextNode = nextNode();
		return nextNode != null;
	}

	@Override
	public TernaryTreeNode<V> next() {
		final NextNodeReturn nextNode = nextNode();
		if(nextNode != null) {
			this.currentNode = nextNode.nextNode;
			this.currentBranch = nextNode.nextBranch;
		} else {
			this.currentNode = null;
			this.currentBranch = Branch.Undefined;
		}
		return this.currentNode;
	}

	private class NextNodeReturn {
		TernaryTreeNode<V> nextNode;
		Branch nextBranch;
		public NextNodeReturn(TernaryTreeNode<V> nextNode, Branch nextBranch) {
			this.nextNode = nextNode;
			this.nextBranch = nextBranch;
		}
	}

}
