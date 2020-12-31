package labrador.driver.server.query;

import java.util.List;

import labrador.analysis.Result;
import pandorasbox.simpleclientserver.client.Client;

public interface ServerQueryDriver {

	public static final QueryExceptionCallback DEFAULT_QUERY_EXCEPTION_CALLBACK = new StandardErrorIgnoreQueryExceptionCallback();
	public static final QueryErrorResponseCallback DEFAULT_QUERY_ERROR_RESPONSE_CALLBACK = new StandardErrorIgnoreQueryErrorResponseCallback();

	public abstract Client getQueryClient();

	public abstract List<QueryClientConnectionInformation> getServerQueryClientConnections();

	public abstract List<List<Result>> queryServers(String searchString);

	public abstract void setQueryClient(Client queryClient);

	public abstract void setServerQueryClientConnections(
			List<QueryClientConnectionInformation> serverQueryClientConnections);

	public abstract void setQueryErrorResponseCallback(
			QueryErrorResponseCallback errorResponseCallback);

	public abstract QueryErrorResponseCallback getQueryErrorResponseCallback();

	public abstract void setQueryExceptionCallback(
			QueryExceptionCallback exceptionCallback);

	public abstract QueryExceptionCallback getQueryExceptionCallback();

}
