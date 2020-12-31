package labrador.search;

/**
 * This {@link Exception} is thrown when there is some search related error. It
 * is primarily meant as a wrapper for specific search provider
 * {@link Exception}s such as those thrown by compass or lucene. Once wrapped
 * those {@link Exception}s can be handled in a uniform fashion.
 * 
 * @author jtwebb
 * 
 */
public class SearchProviderException extends Exception {
	private static final long serialVersionUID = 199891205762994032L;

	/**
	 * Constructs a {@link SearchProviderException} with no detail message.
	 */
	public SearchProviderException() {
		super();
	}

	/**
	 * Constructs a {@link SearchProviderException} with the specified detail message.
	 * 
	 * @param message
	 *            The detail message.
	 */
	public SearchProviderException(String message) {
		super(message);
	}

	/**
	 * Constructs a {@link SearchProviderException} with the specified nested exception
	 * that was raised causing this {@link Exception}.
	 * 
	 * @param nestedException
	 *            The nested exception that was raised causing this
	 *            {@link Exception}.
	 */
	public SearchProviderException(Throwable nestedException) {
		super(nestedException);
	}

	/**
	 * Constructs a {@link SearchProviderException} with the specified detail message
	 * and the nested exception that was raised causing this {@link Exception}.
	 * 
	 * @param message
	 *            The detail message.
	 * @param nestedException
	 *            The nested exception that was raised causing this
	 *            {@link Exception}.
	 */
	public SearchProviderException(String message, Throwable nestedException) {
		super(message, nestedException);
	}
}
