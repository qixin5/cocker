package labrador.server.messaging;

import java.io.Serializable;
import java.util.List;

import labrador.analysis.Result;

import pandorasbox.simpleclientserver.messaging.BasicMessage;
import pandorasbox.simpleclientserver.messaging.Message;

/**
 * This {@link Message} represents a response to
 * {@link QueryIndexRequestMessage}. It contains {@link Result} data.
 * 
 * @author jtwebb
 * 
 */
public class QueryIndexResponseMessage extends BasicMessage implements
		LabradorMessage, Serializable {
	private static final long serialVersionUID = -6027027358242867117L;

	private List<Result> results;

	/**
	 * Creates a new {@link QueueIndexOptimizationMessage} with an empy {@link List}
	 * of {@link Result}s.
	 */
	public QueryIndexResponseMessage() {
		super("Query Results");
	}

	/**
	 * Returns the results of the query.
	 * @param results the results of the query.
	 */
	public void setResults(List<Result> results) {
		this.results = results;
	}

	/**
	 * Sets the results of the query.
	 * @return The results of the query.
	 */
	public List<Result> getResults() {
		return results;
	}

}
