package orego.play;

/** Thrown when a command-line option tries to set an unknown property. */
public class UnknownPropertyException extends Exception {

	public UnknownPropertyException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
