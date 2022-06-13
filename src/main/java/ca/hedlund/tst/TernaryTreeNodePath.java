package ca.hedlund.tst;

import java.util.*;

/**
 * A path within a ternary tree.
 *
 */
public class TernaryTreeNodePath {

	private final static byte END_PATH = 0x00;

	private final static byte LEFT_CHILD = 0x01;

	private final static byte RIGHT_CHILD = 0x02;

	private final static byte CENTER_CHILD = 0x03;

	private final static long NODE_PATH_MASK = 0x0FFFFFFFFFFFFFFFL;

	private byte path[];

	public TernaryTreeNodePath() {
		this(new byte[] { 0x00, END_PATH });
	}

	public TernaryTreeNodePath(byte[] path) {
		this.path = path;
	}

	/**
	 * The number of left, right, center movements in the tree for this path
	 *
	 * @return
	 */
	public int pathLength() {
		if(this.path.length == 0) return 0;

		long lp = this.path[this.path.length-1];
		int zeros = Long.numberOfLeadingZeros(lp);
		// we might have a zero in the  left-most position of the last movement
		if(zeros % 2 == 1) --zeros;

		int pathLength = (64 - zeros) / 2;
		pathLength += (this.path.length - 1) * (62/2);

		return pathLength;
	}

	private void rollover() {
		for(int i = path.length - 1; i > 0; i--) {
			byte b = path[i];
			byte prevB = path[i-1];
			byte lastMovement = (byte)((prevB >> 6) & 0x03);
			b = (byte)(((int)b << 2) & 0xFF);
			b = (byte)((b & 0xFC) | lastMovement);
			path[i] = b;
		}
		if(path[path.length-1] > 0) {
			this.path = Arrays.copyOf(this.path, this.path.length+1);
		}
	}

	private void push(byte value) {
		if((path[0] & 0xC0) > 0) {
			rollover();
		}
		path[0] = (byte)(((int)path[0] << 2) & 0xFF);
		path[0] = (byte)((path[0] & 0xFC) | (value & 0x03));
	}

	public void pushLeft() {
		push(LEFT_CHILD);
	}

	public void pushCenter() {
		push(CENTER_CHILD);
	}

	public void pushRight() {
		push(RIGHT_CHILD);
	}

	public byte[] toByteArray() {
		return this.path;
	}

	/**
	 * Follow path from given tree root and return the node (if found)
	 *
	 * @param root
	 */
	public <T> TernaryTreeNode<T> followPath(TernaryTreeNode<T> root) {
		TernaryTreeNode<T> retVal = root;

		for(int i = 0; i < this.path.length - 1; i++) {
			byte b = this.path[i];
			for(int shift = 0; shift < 8; shift += 2) {
				byte movement = (byte)((b >> shift) & 0x03);
				switch(movement) {
					case LEFT_CHILD:
						retVal = retVal.getLeft();
						break;

					case RIGHT_CHILD:
						retVal = retVal.getRight();
						break;

					case CENTER_CHILD:
						retVal = retVal.getCenter();
						break;

					default:
						break;
				}
				if(retVal == null) break;
			}
			if(retVal == null) break;
		}

		return retVal;
	}

}
