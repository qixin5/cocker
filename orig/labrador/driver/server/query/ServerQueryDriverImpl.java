package labrador.driver.server.query;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import labrador.analysis.Result;
import labrador.server.messaging.QueryIndexRequestMessage;
import labrador.server.messaging.QueryIndexResponseMessage;
import pandorasbox.simpleclientserver.client.Client;
import pandorasbox.simpleclientserver.messaging.MalformedMessageException;
import pandorasbox.simpleclientserver.messaging.Message;

public class ServerQueryDriverImpl implements ServerQueryDriver {

	private List<QueryClientConnectionInformation> server_query_client_connections;
	private Client query_client;
	private QueryErrorResponseCallback error_response_callback;
	private QueryExceptionCallback exception_callback;

	public ServerQueryDriverImpl() {
		this(null);
	}

	public ServerQueryDriverImpl(Client queryClient) {
		setServerQueryClientConnections(new LinkedList<QueryClientConnectionInformation>());
		setQueryClient(queryClient);
		setQueryExceptionCallback(DEFAULT_QUERY_EXCEPTION_CALLBACK);
		setQueryErrorResponseCallback(DEFAULT_QUERY_ERROR_RESPONSE_CALLBACK);
	}

	public List<QueryClientConnectionInformation> getServerQueryClientConnections() {
		return server_query_client_connections;
	}

	public List<List<Result>> queryServers(String searchString) {
		List<List<Result>> results = new LinkedList<List<Result>>();

		for (QueryClientConnectionInformation connectionInformation : getServerQueryClientConnections()) {
			getQueryClient().setConnectionInformation(connectionInformation);
			try {
				Message response = getQueryClient()
						.sendSingleMessage(
								new QueryIndexRequestMessage(searchString,
										connectionInformation
												.getMaxAggregateResults()));

				if (response instanceof QueryIndexResponseMessage) {
					results.add(((QueryIndexResponseMessage) response)
							.getResults());
				} else {
					if (!getQueryErrorResponseCallback().errorResponseReceived(
							response, connectionInformation))
						return results;
				}
			} catch (IOException ioe) {
				if (!getQueryExceptionCallback().caughtException(ioe,
						connectionInformation))
					return results;
			} catch (MalformedMessageException mme) {
				if (!getQueryExceptionCallback().caughtException(mme,
						connectionInformation))
					return results;
			}
		}

		return results;
	}

	public void setServerQueryClientConnections(
			List<QueryClientConnectionInformation> serverQueryClientConnections) {
		server_query_client_connections = serverQueryClientConnections;
	}

	public void setQueryClient(Client queryClient) {
		query_client = queryClient;
	}

	public Client getQueryClient() {
		return query_client;
	}

	public void setQueryErrorResponseCallback(
			QueryErrorResponseCallback errorResponseCallback) {
		error_response_callback = errorResponseCallback;
	}

	public QueryErrorResponseCallback getQueryErrorResponseCallback() {
		return error_response_callback;
	}

	public void setQueryExceptionCallback(
			QueryExceptionCallback exceptionCallback) {
		this.exception_callback = exceptionCallback;
	}

	public QueryExceptionCallback getQueryExceptionCallback() {
		return exception_callback;
	}

}
