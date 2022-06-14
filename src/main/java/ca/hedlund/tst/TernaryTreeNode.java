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

import java.io.*;

/**
 * Node for ternary trees.
 * 
 * @param <V>
 */
public class TernaryTreeNode<V> implements Serializable {
	
	public enum Position {
		LOW,
		EQUAL,
		HIGH
	}

	/**
	 * parent reference
	 */
	private transient TernaryTreeNode<V> parent;

	/**
	 * Node char
	 */
	private volatile char ch;

	/**
	 * Node value, a node is 'terminated' if it's
	 * value is non-<code>null</code>.
	 */
	private V value;


	/**
	 * Atomic reference to left child
	 */
	private TernaryTreeNode<V> left;

	/**
	 * Atomic reference to right child
	 */
	private TernaryTreeNode<V> right;
	
	/**
	 * Atomic reference to center child
	 */
	private TernaryTreeNode<V> center;
	
	/**
	 * Constructor
	 */
	public TernaryTreeNode(TernaryTreeNode<V> parent, char ch) {
		this(parent, ch, null);
	}
	
	public TernaryTreeNode(TernaryTreeNode<V> parent, char ch, V value) {
		super();
		setParent(parent);
		setChar(ch);
		setValue(value);
	}
	
	public TernaryTreeNode<V> getParent() {
		return parent;
	}
	
	public void setParent(TernaryTreeNode<V> parent) {
		this.parent = parent;
	}
	
	public char getChar() {
		return this.ch;
	}
	
	public void setChar(char ch) {
		this.ch = ch;
	}
	
	/**
	 * A node is terminated if it has a database entry.
	 * (i.e., if the value is non-<code>null</code>)
	 */
	public boolean isTerminated() {
		return (getValue() != null);
	}
	
	public boolean isRoot() {
		return getParent() == null;
	}

	/**
	 * Get the node's value
	 */
	public V getValue() {
		return value;
	}
	
	/**
	 * Set the node's value
	 */
	public V setValue(V value) {
		final V oldVal = this.value;
		this.value = value;
		return oldVal;
	}

	/**
	 * Get left child
	 */
	public TernaryTreeNode<V> getLeft() {
		return left;
	}
	
	/**
	 * Set left child
	 */
	public void setLeft(TernaryTreeNode<V> left) {
		this.left = left;
	}
	
	public TernaryTreeNode<V> getRight() {
		return right;
	}

	public void setRight(TernaryTreeNode<V> right) {
		this.right = right;
	}
	
	public TernaryTreeNode<V> getCenter() {
		return center;
	}

	public void setCenter(TernaryTreeNode<V> center) {
		this.center = center;
	}
	
	public TernaryTreeNode<V> getChild(Position pos) {
		TernaryTreeNode<V> retVal = null;
		switch(pos) {
		case LOW:
			retVal = getLeft();
			break;
			
		case EQUAL:
			retVal = getCenter();
			break;
			
		case HIGH:
			retVal = getRight();
			break;
		}
		return retVal;
	}
	
	public void setChild(TernaryTreeNode<V> child, Position pos) {
		switch(pos) {
		case LOW:
			setLeft(child);
			break;
			
		case EQUAL:
			setCenter(child);
			break;
			
		case HIGH:
			setRight(child);
			break;
		}
	}
	
	/**
	 * Returns the full string key for this node
	 *
	 * @return node prefix
	 */
	public String getPrefix() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(getChar());
		TernaryTreeNode<V> child = this;
		TernaryTreeNode<V> parent = getParent();
		
		while(parent != null) {
			if(parent.getCenter() == child)
				buffer.append(parent.getChar());
			child = parent;
			parent = parent.getParent();
		}
		
		return buffer.reverse().toString();
	}
	
	/**
	 * Accept a tree node visitor.
	 */
	public void acceptVisitLast(TernaryTreeNodeVisitor<V> visitor) {
		if(getLeft() != null)
			getLeft().acceptVisitLast(visitor);
		if(getCenter() != null)
			getCenter().acceptVisitLast(visitor);
		if(getRight() != null)
			getRight().acceptVisitLast(visitor);
		visitor.visit(this);
	}
	
	public void acceptVisitFirst(TernaryTreeNodeVisitor<V> visitor) {
		if(!visitor.visit(this)) {
			if(getLeft() != null)
				getLeft().acceptVisitFirst(visitor);
			if(getCenter() != null)
				getCenter().acceptVisitFirst(visitor);
			if(getRight() != null)
				getRight().acceptVisitFirst(visitor);
		}
	}
	
	public void acceptVisitMiddle(TernaryTreeNodeVisitor<V> visitor) {
		if(getLeft() != null)
			getLeft().acceptVisitMiddle(visitor);
		visitor.visit(this);
		if(getCenter() != null)
			getCenter().acceptVisitMiddle(visitor);
		if(getRight() != null)
			getRight().acceptVisitMiddle(visitor);
	}
	
	public void acceptVisitOnlyCenter(TernaryTreeNodeVisitor<V> visitor) {
		if(getCenter() != null)
			getCenter().acceptVisitMiddle(visitor);
		visitor.visit(this);
	}

	public TernaryTreeNodePath getPath() {
		TernaryTreeNodePath path = new TernaryTreeNodePath();

		TernaryTreeNode<V> node = this;
		while (node.getParent() != null) {
			if (node.getParent().getLeft() == node) {
				path.pushLeft();
			} else if (node.getParent().getCenter() == node) {
				path.pushCenter();
			} else if (node.getParent().getRight() == node) {
				path.pushRight();
			}
			node = node.getParent();
		}

		return path;
	}

	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		if(getLeft() != null)
			getLeft().setParent(this);
		if(getRight() != null)
			getRight().setParent(this);
		if(getCenter() != null)
			getCenter().setParent(this);
	}

}
