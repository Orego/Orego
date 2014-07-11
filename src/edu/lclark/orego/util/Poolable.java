package edu.lclark.orego.util;

/**
 * Something that can be included in a pool.
 *
 * @see Pool
 */
public interface Poolable<T> {

	/** Returns the next object (e.g., on the pool's free list). */
	public T getNext();

	/** Sets the next object (e.g., on the pool's free list). */
	public void setNext(T next);

}
