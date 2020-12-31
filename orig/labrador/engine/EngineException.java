package labrador.engine;

import labrador.engine.sessions.Session;

/**
 * This {@link Exception} is thrown when the {@link Engine} or a {@link Session}
 * encounters an error which cannot be recovered from and cannot be categorized
 * easily inside existing exception hierarchy.
 * 
 * @author jtwebb
 * 
 */
public class EngineException extends Exception {
	private static final long serialVersionUID = 199891205762994032L;

	/**
	 * Constructs a {@link EngineException} with no detail message.
	 */
	public EngineException() {
		super();
	}

	/**
	 * Constructs a {@link EngineException} with the specified detail message.
	 * 
	 * @param message
	 *            The detail message.
	 */
	public EngineException(String message) {
		super(message);
	}

	/**
	 * Constructs a {@link EngineException} with the specified nested exception
	 * that was raised causing this {@link Exception}.
	 * 
	 * @param nestedException
	 *            The nested exception that was raised causing this
	 *            {@link Exception}.
	 */
	public EngineException(Throwable nestedException) {
		super(nestedException);
	}

	/**
	 * Constructs a {@link EngineException} with the specified detail message
	 * and the nested exception that was raised causing this {@link Exception}.
	 * 
	 * @param message
	 *            The detail message.
	 * @param nestedException
	 *            The nested exception that was raised causing this
	 *            {@link Exception}.
	 */
	public EngineException(String message, Throwable nestedException) {
		super(message, nestedException);
	}

}