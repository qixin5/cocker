package labrador.server;

import java.io.Serializable;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import pandorasbox.simpleclientserver.client.Client;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This class takes care of the details of creating a {@link JobDetail} for a
 * {@link SynchronizeIndexJob}. It is a helper class that fills in default
 * values.
 * 
 * @author jtwebb
 * 
 */
public class SynchronizeIndexJobDetail extends JobDetail implements
		Serializable {
	private static final long serialVersionUID = 5791114085905240757L;

	/**
	 * The key that maps to the {@link Client} that the
	 * {@link SynchronizeIndexJob} will use to connect to the {@link Server}
	 * (client).
	 */
	public static final String CLIENT_DATA_MAP_KEY = "client";
	/**
	 * The default name for this {@link Job} (synchronize index).
	 */
	public static final String DEFAULT_JOB_NAME = "synchronize index";
	/**
	 * The default group for this job ({@link Scheduler#DEFAULT_GROUP})
	 */
	public static final String DEFAULT_GROUP_NAME = Scheduler.DEFAULT_GROUP;

	/**
	 * Creates a new {@link SynchronizeIndexJobDetail} with the specified
	 * {@link SynchronizeIndexJob} and {@link Client}.
	 * 
	 * @param synchronizeJob
	 *            The {@link Job} for which to create details.
	 * @param client
	 *            The {@link Client} that will be used by the
	 *            {@link SynchronizeIndexJob} to connect to the {@link Server}.
	 */
	public SynchronizeIndexJobDetail(Job synchronizeJob, Client client) {
		super(DEFAULT_JOB_NAME, DEFAULT_GROUP_NAME, synchronizeJob.getClass());

		getJobDataMap().put(CLIENT_DATA_MAP_KEY, client);
	}
}
