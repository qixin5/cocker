package labrador.server;

import java.io.IOException;

import labrador.search.SearchProviderException;
import labrador.server.messaging.QueueBlacklistFilesMessage;
import labrador.server.messaging.LabradorMessage;
import labrador.server.messaging.QueueMonitorFilesMessage;
import labrador.server.messaging.QueueIndexOptimizationMessage;
import labrador.server.messaging.QueryIndexRequestMessage;
import labrador.server.messaging.QueryIndexResponseMessage;
import labrador.server.messaging.QueueIndexSynchronizationMessage;
import labrador.server.messaging.QueueUnblacklistFilesMessage;
import labrador.server.messaging.QueueUnmonitorFilesMessage;
import labrador.server.operations.FilesOperation;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import pandorasbox.scheduling.server.SchedulingHandleRequestCallback;
import pandorasbox.simpleclientserver.messaging.FailureMessage;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.messaging.SuccessMessage;
import pandorasbox.simpleclientserver.messaging.UnknownMessageFailureMessage;
import pandorasbox.simpleclientserver.server.Server;
import pandorasbox.simpleclientserver.server.operations.MessageOperation;

/**
 * 
 * @author jtwebb
 * 
 */
public class LabradorHandleRequestCallback extends
		SchedulingHandleRequestCallback {

	// private static final Log log = LogFactory
	// .getLog(LabradorHandleRequestCallback.class);

	public Message handleMessage(Message request, Server owningServer) {
		Message response = new UnknownMessageFailureMessage(request);
		if (request instanceof LabradorMessage) {
			if (request instanceof QueueIndexSynchronizationMessage) {
				response = new SuccessMessage(
						"Queued index synchronization. It will begin ASAP.");
				owningServer.getOperationsManager().synchronousOperation(
						new MessageOperation(request));
			} else if (request instanceof QueueIndexOptimizationMessage) {
				response = new SuccessMessage(
						"Queued index optimization. It will begin ASAP.");
				owningServer.getOperationsManager().synchronousOperation(
						new MessageOperation(request));
			} else if (request instanceof QueueMonitorFilesMessage) {
				response = new SuccessMessage(
						"Queued new file monitoring. It will begin ASAP.");
				owningServer.getOperationsManager().synchronousOperation(
						new FilesOperation(((QueueMonitorFilesMessage) request)
								.getFiles()));
			} else if (request instanceof QueueUnmonitorFilesMessage) {
				response = new SuccessMessage(
						"Queued new file unmonitoring. It will begin ASAP.");
				owningServer.getOperationsManager().synchronousOperation(
						new FilesOperation(((QueueMonitorFilesMessage) request)
								.getFiles()));
			} else if (request instanceof QueueBlacklistFilesMessage) {
				response = new SuccessMessage(
						"Queued new file blacklisting. It will begin ASAP.");
				owningServer.getOperationsManager().synchronousOperation(
						new FilesOperation(((QueueMonitorFilesMessage) request)
								.getFiles()));
			} else if (request instanceof QueueUnblacklistFilesMessage) {
				response = new SuccessMessage(
						"Queued new file unblacklisting. It will begin ASAP.");
				owningServer.getOperationsManager().synchronousOperation(
						new FilesOperation(((QueueMonitorFilesMessage) request)
								.getFiles()));
			} else if (request instanceof QueryIndexRequestMessage) {
				response = handleQueryIndexRequestMessage(request, owningServer);
			}
		} else {
			response = super.handleMessage(request, owningServer);
		}
		return response;
	}

	private Message handleQueryIndexRequestMessage(Message request,
			Server owningServer) {
		Message response;
		if (owningServer instanceof LabradorServer) {
			// make some casts
			LabradorServer labradorServer = (LabradorServer) owningServer;
			QueryIndexRequestMessage queryIndexRequestMessage = (QueryIndexRequestMessage) request;
			// set max results
			labradorServer.getEngine().getSearchProvider()
					.setMaxAggregateResults(
							queryIndexRequestMessage.getMaxAggregateResults());
			// prepare the response
			response = new QueryIndexResponseMessage();
			// run the search and store the results
			try {
				((QueryIndexResponseMessage) response)
						.setResults(labradorServer.getEngine().search(
								queryIndexRequestMessage.getSearchString()));
			} catch (IOException ioe) {
			} catch (SearchProviderException e) {
			}
		} else {
			response = new FailureMessage(
					"Server was not of a suitable type to handle a query index message.");
		}
		return response;
	}
}
