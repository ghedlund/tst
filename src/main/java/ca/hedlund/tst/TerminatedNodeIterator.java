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

	private TernaryTreeNode<V> currentNode;

	private TernaryTreeNode<V> nextNode;

	private Predicate<TernaryTreeNode<V>> filter;

	public TerminatedNodeIterator(TernaryTree<V> tree) {
		this(tree, (node) -> true);
	}

	public TerminatedNodeIterator(TernaryTree<V> tree, Predicate<TernaryTreeNode<V>> filter) {
		this(tree, tree.getRoot(), filter);
	}

	public TerminatedNodeIterator(TernaryTree<V> tree, TernaryTreeNode<V> node) {
		this(tree, node, (n) -> true);
	}

	public TerminatedNodeIterator(TernaryTree<V> tree, TernaryTreeNode<V> node, Predicate<TernaryTreeNode<V>> filter) {
		super();
		this.tree = tree;
		this.startNode = node;
		this.filter = filter;
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

	public void reset() {
		this.currentNode = null;
		this.nextNode = null;
	}

	private TernaryTreeNode<V> findNextNode() {
		if(currentNode == null) {
			return continueFromNode(this.startNode, Branch.Left);
		} else {
			return continueFromNode(currentNode, Branch.Center);
		}
	}

	private TernaryTreeNode<V> continueFromNode(TernaryTreeNode<V> node, Branch branch) {
		// continue from next branch in parent node
		switch(branch) {
			case Left:
				if(node.getLeft() != null) {
					TernaryTreeNode<V> leftVal = continueFromNode(node.getLeft(), Branch.Left);
					if(leftVal != null)
						return leftVal;
				}
				if(node.isTerminated() && filter.test(node))
					return node;

			case Center:
				if(node.getCenter() != null) {
					TernaryTreeNode<V> centerVal = continueFromNode(node.getCenter(), Branch.Left);
					if(centerVal != null)
						return centerVal;
				}

			case Right:
				if(node.getRight() != null) {
					TernaryTreeNode<V> rightVal = continueFromNode(node.getRight(), Branch.Left);
					if(rightVal != null)
						return rightVal;
				}

			default:
				break;
		}

		if(node.getParent() != null) {
			final Branch childBranch = getBranch(node.getParent(), node);
			if(childBranch == Branch.Left && node.getParent().isTerminated() && filter.test(node.getParent())) {
				return node.getParent();
			}
			return continueFromNode(node.getParent(), getBranch(node.getParent(), node).nextBranch());
		} else
			return null;
	}

	@Override
	public boolean hasNext() {
		this.nextNode = findNextNode();
		return this.nextNode != null;
	}

	@Override
	public TernaryTreeNode<V> next() {
		if(this.nextNode == null)
			this.nextNode = findNextNode();
		this.currentNode = this.nextNode;
		this.nextNode = null;
		return this.currentNode;
	}

}
