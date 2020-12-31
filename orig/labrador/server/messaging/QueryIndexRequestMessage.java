package labrador.server.messaging;

import java.io.Serializable;

import labrador.engine.Engine;
import labrador.search.SearchProvider;

import pandorasbox.simpleclientserver.messaging.BasicMessage;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This message will run a query using the {@link Server}'s {@link Engine} and
 * request that results be sent in response.
 * 
 * @author jtwebb
 * 
 */
public class QueryIndexRequestMessage extends BasicMessage implements
		LabradorMessage, Serializable {
	private static final long serialVersionUID = -6027027358242867117L;

	private String search_string;
	private int max_aggregate_results;

	/**
	 * Creates a new {@link QueueIndexOptimizationMessage} with no specified search
	 * string and the default maximum number of aggregate results.
	 */
	public QueryIndexRequestMessage() {
		this(SearchProvider.MAX_AGGREGATE_RESULTS_ALL);
	}

	/**
	 * Creates a new {@link QueueIndexOptimizationMessage} with no specified search
	 * string and the specified number of maximum number of aggregate results.
	 * 
	 * @param maxAggregateResults
	 *            The maximum number of results to return. Fewer results can be
	 *            returned but not more.
	 */
	public QueryIndexRequestMessage(int maxAggregateResults) {
		this(null, maxAggregateResults);
	}

	/**
	 * Creates a new {@link QueueIndexOptimizationMessage} with the specified search
	 * string and the default maximum number of aggregate results..
	 * 
	 * @param searchString
	 *            The string to query with.
	 */
	public QueryIndexRequestMessage(String searchString) {
		this(searchString, SearchProvider.MAX_AGGREGATE_RESULTS_ALL);
	}

	/**
	 * Creates a new {@link QueueIndexOptimizationMessage} with the specified search
	 * string and the default maximum number of aggregate results..
	 * 
	 * @param searchString
	 *            The string to query with.
	 * @param maxAggregateResults
	 *            The maximum number of results to return. Fewer results can be
	 *            returned but not more.
	 */
	public QueryIndexRequestMessage(String searchString, int maxAggregateResults) {
		super("");
		setSearchString(searchString);
		setMaxAggregateResults(maxAggregateResults);
	}

	/**
	 * Sets the string to query with.
	 * 
	 * @param searchString
	 *            The string to query with.
	 */
	public void setSearchString(String searchString) {
		search_string = searchString;
		setMessageText("Query the index for: "
				+ (searchString == null ? "No query" : searchString));
	}

	/**
	 * Returns the string to query with.
	 * 
	 * @return the string to query with.
	 */
	public String getSearchString() {
		return search_string;
	}

	/**
	 * Sets the maximum number of results to return. Fewer results can be
	 * returned but not more.
	 * 
	 * @param maxAggregateResults
	 *            The maximum number of results to return. Fewer results can be
	 *            returned but not more.
	 */
	public void setMaxAggregateResults(int maxAggregateResults) {
		max_aggregate_results = maxAggregateResults;
	}

	/**
	 * Returns the maximum number of results to return. Fewer results can be
	 * returned but not more.
	 * 
	 * @return the maximum number of results to return. Fewer results can be
	 *         returned but not more.
	 */
	public int getMaxAggregateResults() {
		return max_aggregate_results;
	}

}
