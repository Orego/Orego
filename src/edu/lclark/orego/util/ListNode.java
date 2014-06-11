package edu.lclark.orego.util;

/**
 * Linked list node. T is the type of the key in the node. This is a simple
 * container class with two fields, key and next.
 * <p>
 * It may be tempting to optimize this class by making the fields public. In
 * addition to being bad style, his would cause problems with implementing
 * Poolable.
 */
public final class ListNode<T> implements Poolable<ListNode<T>> {

	/** The key stored in this node. */
	private T key;

	/** The next list node. */
	private ListNode<T> next;

	/** Returns the key stored in this list node. */
	public T getKey() {
		return key;
	}

	/** Returns the next list node. */
	public ListNode<T> getNext() {
		return next;
	}

	/** Sets the key stored in this list node. */
	public void setKey(T key) {
		this.key = key;
	}

	/** Sets the next list node. */
	public void setNext(ListNode<T> next) {
		this.next = next;
	}

}
