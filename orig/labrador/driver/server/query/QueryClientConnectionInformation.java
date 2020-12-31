package labrador.driver.server.query;

import labrador.search.SearchProvider;
import pandorasbox.simpleclientserver.client.Client;
import pandorasbox.simpleclientserver.client.ClientConnectionInformationImpl;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This class contains connection information for {@link Client}s that query
 * {@link Server}s. It extends {@link ClientConnectionInformationImpl} and adds
 * an additional field for specifing the maximum results a search should return.
 * 
 * @author jtwebb
 * 
 */
public class QueryClientConnectionInformation extends
		ClientConnectionInformationImpl {

	private int max_aggregate_results;

	/**
	 * Creates a new {@link QueryClientConnectionInformation} with default
	 * values for hostname, port, timeout, and number of results to aggregate.
	 */
	public QueryClientConnectionInformation() {
		this(Client.DEFAULT_HOSTNAME, Server.DEFUALT_PORT,
				Client.DEFAULT_TIMEOUT,
				SearchProvider.MAX_AGGREGATE_RESULTS_ALL);
	}

	/**
	 * Creates a new {@link QueryClientConnectionInformation} with default
	 * values for hostname, timeout, and number of results to aggregate as well
	 * as the specified value for port.
	 * 
	 * @param port
	 *            The port being used to connect to the {@link Server}.
	 */
	public QueryClientConnectionInformation(int port) {
		this(Client.DEFAULT_HOSTNAME, port, Client.DEFAULT_TIMEOUT,
				SearchProvider.MAX_AGGREGATE_RESULTS_ALL);
	}

	/**
	 * Creates a new {@link QueryClientConnectionInformation} with default
	 * values for hostname and number of results to aggregate as well as the
	 * specified values for port and timeout.
	 * 
	 * @param port
	 *            The port being used to connect to the {@link Server}.
	 * @param timeout
	 *            The amount of time in milliseconds that the client is willing
	 *            to wait for a response from the server. A value of 0 is
	 *            considered infinite.
	 */
	public QueryClientConnectionInformation(int port, int timeout) {
		this(Client.DEFAULT_HOSTNAME, port, timeout,
				SearchProvider.MAX_AGGREGATE_RESULTS_ALL);
	}

	/**
	 * Creates a new {@link QueryClientConnectionInformation} with default
	 * values for port and number of results to aggregate as well as the
	 * specified values for hostname and timeout.
	 * 
	 * @param timeout
	 *            The amount of time in milliseconds that the client is willing
	 *            to wait for a response from the server. A value of 0 is
	 *            considered infinite.
	 * @param hostname
	 *            The {@link Server}'s hostname.
	 */
	public QueryClientConnectionInformation(int timeout, String hostname) {
		this(hostname, Server.DEFUALT_PORT, timeout,
				SearchProvider.MAX_AGGREGATE_RESULTS_ALL);
	}

	/**
	 * Creates a new {@link QueryClientConnectionInformation} with default
	 * values for port, and number of results to aggregate as well as the
	 * specified values for hostname and timeout.
	 * 
	 * @param timeout
	 *            The amount of time in milliseconds that the client is willing
	 *            to wait for a response from the server. A value of 0 is
	 *            considered infinite.
	 * @param hostname
	 *            The {@link Server}'s hostname.
	 */
	public QueryClientConnectionInformation(int timeout, String hostname,
			int maxAggregateResults) {
		this(hostname, Server.DEFUALT_PORT, timeout, maxAggregateResults);
	}

	/**
	 * Creates a new {@link QueryClientConnectionInformation} with default
	 * values for port, timeout, and number of results to aggregate as well as
	 * the specified value for hostname.
	 * 
	 * @param hostname
	 *            The {@link Server}'s hostname.
	 */
	public QueryClientConnectionInformation(String hostname) {
		this(hostname, Server.DEFUALT_PORT, Client.DEFAULT_TIMEOUT,
				SearchProvider.MAX_AGGREGATE_RESULTS_ALL);
	}

	/**
	 * Creates a new {@link QueryClientConnectionInformation} with default
	 * values for timeout and number of results to aggregate as well as the
	 * specified values for hostname and port.
	 * 
	 * @param hostname
	 *            The {@link Server}'s hostname.
	 * @param port
	 *            The port being used to connect to the {@link Server}.
	 */
	public QueryClientConnectionInformation(String hostname, int port) {
		this(hostname, port, Client.DEFAULT_TIMEOUT,
				SearchProvider.MAX_AGGREGATE_RESULTS_ALL);
	}

	/**
	 * Creates a new {@link QueryClientConnectionInformation} with the specified
	 * values for hostname, port, and timeout as well as the default number of
	 * results to aggregate.
	 * 
	 * @param hostname
	 *            The {@link Server}'s hostname.
	 * @param port
	 *            The port being used to connect to the {@link Server}.
	 * @param timeout
	 *            The amount of time in milliseconds that the client is willing
	 *            to wait for a response from the server. A value of 0 is
	 *            considered infinite.
	 */
	public QueryClientConnectionInformation(String hostname, int port,
			int timeout) {
		this(hostname, port, timeout, SearchProvider.MAX_AGGREGATE_RESULTS_ALL);
	}

	/**
	 * Creates a new {@link QueryClientConnectionInformation} with the specified
	 * values for hostname, port, timeout, and the number of results to
	 * aggregate.
	 * 
	 * @param hostname
	 *            The {@link Server}'s hostname.
	 * @param port
	 *            The port being used to connect to the {@link Server}.
	 * @param timeout
	 *            The amount of time in milliseconds that the client is willing
	 *            to wait for a response from the server. A value of 0 is
	 *            considered infinite.
	 * @param maxAggregateResults
	 *            The maximum number of results that the {@link Server} should
	 *            return. Fewer results may be returned, but no more.
	 */
	public QueryClientConnectionInformation(String hostname, int port,
			int timeout, int maxAggregateResults) {
		super(hostname, port, timeout);
		setMaxAggregateResults(maxAggregateResults);
	}

	/**
	 * Returns <code>true</code> if this
	 * {@link QueryClientConnectionInformation} would connect to the same
	 * {@link Server} and return the same number of results as the specified
	 * {@link Object}; otherwise this will return <code>false</code>.
	 * 
	 * @return <code>true</code> if this
	 *         {@link QueryClientConnectionInformation} would connect to the
	 *         same {@link Server} and return the same number of results as the
	 *         specified {@link Object}; otherwise this will return
	 *         <code>false</code>.
	 */
	@Override
	public boolean equals(Object otherObject) {
		if (otherObject instanceof QueryClientConnectionInformation) {
			QueryClientConnectionInformation otherQueryClientConnectionInformation = (QueryClientConnectionInformation) otherObject;
			return super.equals(otherObject)
					&& this.getMaxAggregateResults() == otherQueryClientConnectionInformation
							.getMaxAggregateResults();
		} else {
			return false;
		}
	}

	/**
	 * Returns the maximum number of results that the {@link Server} should
	 * return. Fewer results may be returned, but no more.
	 * 
	 * @return the maximum number of results that the {@link Server} should
	 *         return. Fewer results may be returned, but no more.
	 */
	public int getMaxAggregateResults() {
		return max_aggregate_results;
	}

	/**
	 * Returns a hash code representation of this object.
	 * 
	 * @return a hash code representation of this object.
	 */
	@Override
	public int hashCode() {
		return super.hashCode()
				+ Integer.toString(getMaxAggregateResults()).hashCode();
	}

	/**
	 * Sets the maximum number of results that the {@link Server} should return.
	 * Fewer results may be returned, but no more.
	 * 
	 * @param maxAggregateResults
	 *            The maximum number of results that the {@link Server} should
	 *            return. Fewer results may be returned, but no more.
	 */
	public void setMaxAggregateResults(int maxAggregateResults) {
		max_aggregate_results = maxAggregateResults;
	}

	/**
	 * Returns a {@link String} representation of the connection information
	 * stored in this object.
	 * 
	 * @return a {@link String} representation of the connection information
	 *         stored in this object.
	 */
	@Override
	public String toString() {
		return super.toString()
				+ " {"
				+ (getMaxAggregateResults() == SearchProvider.MAX_AGGREGATE_RESULTS_ALL ? "All"
						: getMaxAggregateResults()) + "}";
	}
}
