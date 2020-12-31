package labrador.server;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import labrador.engine.EngineException;
import labrador.search.SearchProviderException;
import labrador.server.messaging.QueueBlacklistFilesMessage;
import labrador.server.messaging.QueueIndexOptimizationMessage;
import labrador.server.messaging.QueueIndexSynchronizationMessage;
import labrador.server.messaging.QueueMonitorFilesMessage;
import labrador.server.messaging.QueueUnblacklistFilesMessage;
import labrador.server.messaging.QueueUnmonitorFilesMessage;
import labrador.server.operations.FilesOperation;
import pandorasbox.simpleclientserver.server.Server;
import pandorasbox.simpleclientserver.server.operations.MessageOperation;
import pandorasbox.simpleclientserver.server.operations.Operation;
import pandorasbox.simpleclientserver.server.simple.SimpleHandleOperationCallbackImpl;

public class LabradorHandleOperationCallback extends
		SimpleHandleOperationCallbackImpl {

	Log log = LogFactory.getLog(LabradorHandleOperationCallback.class);

	/**
	 * Handle the specified {@link Operation} for the known {@link Server}.
	 * 
	 * @param operation
	 *            The {@link Operation} to perform.
	 * @param owningServer
	 *            The {@link Server} that needs the {@link Operation} performed.
	 */
	@Override
	public void handleOperation(Operation operation, Server owningServer) {
		if (operation instanceof MessageOperation) {
			MessageOperation messageOperation = (MessageOperation) operation;
			if (messageOperation.getMessage() instanceof QueueIndexSynchronizationMessage) {
				handleSynchronizeOperation(owningServer);
			} else if (messageOperation.getMessage() instanceof QueueIndexOptimizationMessage) {
				handleOptimizeOperation(owningServer);
			} else if (messageOperation.getMessage() instanceof QueueMonitorFilesMessage) {
				handleMonitorFilesOperation(owningServer, operation);
			} else if (messageOperation.getMessage() instanceof QueueUnmonitorFilesMessage) {
				handleUnmonitorFilesOperation(owningServer, operation);
			} else if (messageOperation.getMessage() instanceof QueueBlacklistFilesMessage) {
				handleBlacklistFilesOperation(owningServer, operation);
			} else if (messageOperation.getMessage() instanceof QueueUnblacklistFilesMessage) {
				handleUnblacklistFilesOperation(owningServer, operation);
			} else {
				super.handleOperation(operation, owningServer);
			}
		} else {
			super.handleOperation(operation, owningServer);
		}
	}

	protected void handleSynchronizeOperation(Server owningServer) {
		if (owningServer instanceof LabradorServer) {
			LabradorServer labradorServer = (LabradorServer) owningServer;
			try {
				labradorServer.getEngine().synchronizeIndex();
			} catch (EngineException ee) {
				if (log.isErrorEnabled())
					log
							.error("Could not perform synchronize server operation: "
									+ ee);
			} catch (IOException ioe) {
				if (log.isErrorEnabled())
					log
							.error("Could not perform synchronize server operation: "
									+ ioe);
			}
		} else {
			if (log.isErrorEnabled())
				log
						.error("Could not perform synchronize server operation. Server was not of suitable type.");
		}
	}

	protected void handleOptimizeOperation(Server owningServer) {
		if (owningServer instanceof LabradorServer) {
			LabradorServer labradorServer = (LabradorServer) owningServer;
			try {
				labradorServer.getEngine().optimizeIndex();
			} catch (IOException ioe) {
				if (log.isErrorEnabled())
					log.error("Could not perform optimize server operation: "
							+ ioe);
			} catch (SearchProviderException ee) {
				if (log.isErrorEnabled())
					log.error("Could not perform optimize server operation: "
							+ ee);
			}
		} else {
			if (log.isErrorEnabled())
				log
						.error("Could not perform optimize server operation. Server was not of suitable type.");
		}
	}

	protected void handleMonitorFilesOperation(Server owningServer,
			Operation operation) {
		if (!(owningServer instanceof LabradorServer)) {
			if (log.isErrorEnabled())
				log
						.error("Could not perform monitor files operation. Server was not of suitable type.");
		} else if (!(operation instanceof FilesOperation)) {
			if (log.isErrorEnabled())
				log
						.error("Could not perform monitor files operation. Operation was not of suitable type.");
		} else {
			List<String> filenames = ((FilesOperation)operation).getFiles();
			LabradorServer labradorServer = (LabradorServer) owningServer;
			if (filenames != null) {
				for (String filename : filenames) {
					try {
						labradorServer.getEngine().monitorFile(
								new File(filename));
					} catch (IOException ioe) {
						if (log.isErrorEnabled())
							log
									.error("Could not perform monitor file operation: "
											+ ioe);
					} catch (EngineException ee) {
						if (log.isErrorEnabled())
							log
									.error("Could not perform monitor file operation: "
											+ ee);
					}
				}
			}
		}
	}

	protected void handleBlacklistFilesOperation(Server owningServer,
			Operation operation) {
		if (!(owningServer instanceof LabradorServer)) {
			if (log.isErrorEnabled())
				log
						.error("Could not perform blacklist files operation. Server was not of suitable type.");
		} else if (!(operation instanceof FilesOperation)) {
			if (log.isErrorEnabled())
				log
						.error("Could not perform blacklist files operation. Operation was not of suitable type.");
		} else {
			List<String> filenames = ((FilesOperation)operation).getFiles();
			LabradorServer labradorServer = (LabradorServer) owningServer;
			if (filenames != null) {
				for (String filename : filenames) {
					try {
						labradorServer.getEngine().blacklistFile(
								new File(filename));
					} catch (IOException ioe) {
						if (log.isErrorEnabled())
							log
									.error("Could not perform blacklist file operation: "
											+ ioe);
					} catch (EngineException ee) {
						if (log.isErrorEnabled())
							log
									.error("Could not perform blacklist file operation: "
											+ ee);
					}
				}
			}
		}
	}

	protected void handleUnmonitorFilesOperation(Server owningServer,
			Operation operation) {
		if (!(owningServer instanceof LabradorServer)) {
			if (log.isErrorEnabled())
				log
						.error("Could not perform unmonitor files operation. Server was not of suitable type.");
		} else if (!(operation instanceof FilesOperation)) {
			if (log.isErrorEnabled())
				log
						.error("Could not perform unmonitor files operation. Operation was not of suitable type.");
		} else {
			List<String> filenames = ((FilesOperation)operation).getFiles();
			LabradorServer labradorServer = (LabradorServer) owningServer;
			if (filenames != null) {
				for (String filename : filenames) {
					try {
						labradorServer.getEngine().unmonitorFile(
								new File(filename));
					} catch (IOException ioe) {
						if (log.isErrorEnabled())
							log
									.error("Could not perform unmonitor file operation: "
											+ ioe);
					} catch (EngineException ee) {
						if (log.isErrorEnabled())
							log
									.error("Could not perform unmonitor file operation: "
											+ ee);
					}
				}
			}
		}
	}

	protected void handleUnblacklistFilesOperation(Server owningServer,
			Operation operation) {
		if (!(owningServer instanceof LabradorServer)) {
			if (log.isErrorEnabled())
				log
						.error("Could not perform unblacklist files operation. Server was not of suitable type.");
		} else if (!(operation instanceof FilesOperation)) {
			if (log.isErrorEnabled())
				log
						.error("Could not perform unblacklist files operation. Operation was not of suitable type.");
		} else {
			List<String> filenames = ((FilesOperation)operation).getFiles();
			LabradorServer labradorServer = (LabradorServer) owningServer;
			if (filenames != null) {
				for (String filename : filenames) {
					try {
						labradorServer.getEngine().unblacklistFile(
								new File(filename));
					} catch (IOException ioe) {
						if (log.isErrorEnabled())
							log
									.error("Could not perform unblacklist file operation: "
											+ ioe);
					} catch (EngineException ee) {
						if (log.isErrorEnabled())
							log
									.error("Could not perform unblacklist file operation: "
											+ ee);
					}
				}
			}
		}
	}

}
