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
	};

	private final TernaryTree<V> tree;

	private TernaryTreeNode<V> startNode;

	private boolean prefixSearch = false;

	private TernaryTreeNode<V> currentNode;

	private Branch currentBranch = Branch.Left;

	private TernaryTreeNode<V> nextNode;

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
		if(prefixSearch)
			this.currentBranch = Branch.Center;
	}

	private Branch getBranch(TernaryTreeNode<V> parent, TernaryTreeNode<V> child) {
		if(parent.getLeft() == child)
			return Branch.Left;
		if(parent.getCenter() == child)
			return Branch.Center;
		if(parent.getRight() == child)
			return Branch.Right;
		return null;
	}

	public void setStartNode(TernaryTreeNode<V> startNode) {
		this.startNode = startNode;
	}

	public void setCurrentNode(TernaryTreeNode<V> currentNode) {
		this.currentNode = currentNode;
		this.currentBranch = Branch.Center;
	}

	public void setPrefixSearch(boolean prefixSearch) {
		this.prefixSearch = prefixSearch;
	}

	public void reset() {
		this.currentNode = null;
		this.nextNode = null;
		this.currentBranch = (this.prefixSearch ? Branch.Center : Branch.Left);
	}

	private TernaryTreeNode<V> nextNode() {
		TernaryTreeNode<V> node = this.currentNode != null ? this.currentNode : this.startNode;
		Branch branch = this.currentBranch;

		boolean loopCnd = true;
		do {
			switch(branch) {
				case Left:
					if(node.getLeft() != null) {
						node = node.getLeft();
						continue;
					} else if(node.isTerminated() && filter.test(node)) {
						this.currentBranch = branch.nextBranch();
						return node;
					}

				case Center:
					if(node.getCenter() != null) {
						branch = Branch.Left;
						node = node.getCenter();
						continue;
					}

				case Right:
					if(node.getRight() != null) {
						branch = Branch.Left;
						node = node.getRight();
						continue;
					}
			}
			Branch childBranch = getBranch(node.getParent(), node);
			if(childBranch == Branch.Left && node.getParent().isTerminated() && filter.test(node.getParent())) {
				this.currentBranch = childBranch.nextBranch();
				return node.getParent();
			}
			node = node.getParent();
			branch = childBranch.nextBranch();

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
		this.nextNode = nextNode();
		return this.nextNode != null;
	}

	@Override
	public TernaryTreeNode<V> next() {
		if(this.nextNode == null)
			this.nextNode = nextNode();
		this.currentNode = this.nextNode;
		this.nextNode = null;
		return this.currentNode;
	}

}
