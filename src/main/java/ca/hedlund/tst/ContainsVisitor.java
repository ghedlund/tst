package ca.hedlund.tst;

public abstract class ContainsVisitor<T, V> implements TernaryTreeNodeVisitor<V> {

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
