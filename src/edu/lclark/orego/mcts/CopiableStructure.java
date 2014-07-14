package edu.lclark.orego.mcts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A complicated structure with many parts. It can copy itself using
 * serialization. This is used mainly to copy the Board and associated
 * BoardObservers, etc. into each McRunnable.
 *
 */
@SuppressWarnings("serial")
public final class CopiableStructure implements Serializable {

	private final List<Serializable> contents;

	public CopiableStructure() {
		this.contents = new ArrayList<>();
	}

	/** Adds item to this CopiableStructure. */
	public CopiableStructure add(Serializable item) {
		contents.add(item);
		return this;
	}

	/**
	 * Returns a deep copy of this CopiableStructure.
	 *
	 * Adapted from
	 * http://www.javaworld.com/article/2077578/learn-java/java-tip-
	 * 76--an-alternative-to-the-deep-copy-technique.html.
	 */
	public CopiableStructure copy() {
		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			@SuppressWarnings("resource")
			final
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			oos.flush();
			final ByteArrayInputStream bin = new ByteArrayInputStream(
					bos.toByteArray());
			@SuppressWarnings("resource")
			final
			ObjectInputStream ois = new ObjectInputStream(bin);
			final CopiableStructure result = (CopiableStructure) ois.readObject();
			oos.close();
			ois.close();
			return result;
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null; // Unreachable
	}

	/**
	 * Returns the object of the specified class in this CopiableStructure. If
	 * it is meant to be a copy, it is vital to call copy() first and then call
	 * get() on the copy.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T get(Class<T> c) {
		for (final Serializable obj : contents) {
			if (c.isInstance(obj)) {
				return (T) obj;
			}
		}
		// There is no object of that class in this CopiableStructure
		throw new IllegalArgumentException();
	}

}
