package edu.lclark.orego.mcts;

import java.io.*;
import java.util.*;

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
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			@SuppressWarnings("resource")
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			oos.flush();
			ByteArrayInputStream bin = new ByteArrayInputStream(
					bos.toByteArray());
			@SuppressWarnings("resource")
			ObjectInputStream ois = new ObjectInputStream(bin);
			CopiableStructure result = (CopiableStructure) ois.readObject();
			oos.close();
			ois.close();
			return result;
		} catch (Exception e) {
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
		for (Serializable obj : contents) {
			if (c.isInstance(obj)) {
				return (T)obj;
			}
		}
		// There is no object of that class in this CopiableStructure
		throw new IllegalArgumentException();
	}

}
