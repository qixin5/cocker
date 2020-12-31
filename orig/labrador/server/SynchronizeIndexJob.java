package labrador.server;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import pandorasbox.simpleclientserver.client.Client;
import pandorasbox.simpleclientserver.client.Session;
import pandorasbox.simpleclientserver.messaging.MalformedMessageException;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.messaging.SuccessMessage;
import pandorasbox.simpleclientserver.server.Server;

import labrador.server.messaging.QueueIndexOptimizationMessage;
import labrador.server.messaging.QueueIndexSynchronizationMessage;

/**
 * This {@link Job} takes care of synchronizing and optimizing the index by
 * opening a session to the {@link Server} via a {@link Client} in the data map
 * and sending first a {@link QueueIndexSynchronizationMessage} and then if that
 * succeeds a {@link QueueIndexOptimizationMessage}.
 * 
 * @author jtwebb
 * 
 */
public class SynchronizeIndexJob implements Job {

	private static Log log = LogFactory.getLog(SynchronizeIndexJob.class);

	/**
	 * Executes the task of telling the {@link Server} via a {@link Client} in
	 * the data map to synchronize and then optimize the index.
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		Client client = (Client) context.getMergedJobDataMap().get(
				SynchronizeIndexJobDetail.CLIENT_DATA_MAP_KEY);

		Session session = null;

		try {
			session = client.createSession();

			Message response = session
					.sendRequest(new QueueIndexSynchronizationMessage());
			if (response instanceof SuccessMessage) {
				if (log.isInfoEnabled())
					log.info("Index synchronization successfully queued");
				response = session.sendRequest(new QueueIndexOptimizationMessage());
				if (response instanceof SuccessMessage) {
					if (log.isInfoEnabled())
						log.info("Index optimization successfully queued");
				} else {
					if (log.isFatalEnabled())
						log.fatal("Could not queue index optimization: "
								+ response);
				}
			} else {
				if (log.isFatalEnabled())
					log.fatal("Could not queue index synchronization: "
							+ response);
			}

		} catch (IOException ioe) {// is this me or eclipse?
			if (log.isFatalEnabled())
				log.fatal("Problem with job execution: " + ioe);
		} catch (MalformedMessageException mme) {
			if (log.isFatalEnabled())
				log.fatal("Problem with job execution: " + mme);
		} finally {
			if (session != null)
				session.close();
		}
	}

}
