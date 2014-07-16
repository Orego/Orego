package edu.lclark.orego.util;

/**
 * A pool of objects of type T. This allows for manual memory management of, for
 * example, tree nodes.
 * <p>
 * To fill the pool with objects, create them and pass them to free.
 */
public final class Pool<T extends Poolable<T>> {

	/** Linked list of available objects. */
	private T free;

	/**
	 * Returns the next available object in this pool, or null if none are
	 * available. It is synchronized to avoid the danger of two threads pulling
	 * the same object out of the pool.
	 */
	public synchronized T allocate() {
		if (free == null) {
			return null;
		}
		final T result = free;
		free = free.getNext();
		return result;
	}

	/**
	 * Adds element to those available in this pool. This works even if element
	 * was not previously in this pool. In fact, this is how elements are added
	 * to a pool in the first place.
	 *
	 * @return the T to which element's next pointer used to point.
	 */
	public T free(T element) {
		final T result = element.getNext();
		element.setNext(free);
		free = element;
		return result;
	}

	/**
	 * Returns true if there are no elements left in this pool.
	 */
	public boolean isEmpty() {
		return free == null;
	}

	/**
	 * Returns the number of elements in the pool. This take time linear in the
	 * size of the pool.
	 */
	public int size() {
		int count = 0;
		T node = free;
		while (node != null) {
			count++;
			node = node.getNext();
		}
		return count;
	}

}
