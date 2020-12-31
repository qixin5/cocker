package labrador.server.messaging;

import java.text.ParseException;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import pandorasbox.scheduling.server.messaging.CronScheduleJobMessage;

/**
 * This class extends the normal {@link CronScheduleJobMessage} and fills in
 * some blanks to facilitate easier scheduling within labrador.
 * 
 * @author jtwebb
 * 
 */
public class SynchronizeIndexAddTriggerMessage extends CronScheduleJobMessage
		implements LabradorMessage {
	private static final long serialVersionUID = -5534099582997579452L;

	/**
	 * Creates a {@link SynchronizeIndexAddTriggerMessage} with no specified
	 * {@link JobDetail} or {@link Trigger}.
	 */
	public SynchronizeIndexAddTriggerMessage() {
	}

	/**
	 * Creates a {@link SynchronizeIndexAddTriggerMessage} with the specified
	 * {@link JobDetail} and constructs a {@link Trigger} from the specified
	 * trigger name and cron expression.
	 * 
	 * @param triggerName
	 *            The name to use when constructing the {@link Trigger}.
	 * @param cronExpression
	 *            The cron expression to use when constructing the
	 *            {@link Trigger}.
	 * @param synchronizeIndexJobDetail
	 *            The {@link JobDetail} that needs to be scheduled.
	 * @throws ParseException
	 *             This {@link Exception} is propagated from the creation of a
	 *             {@link CronTrigger}: "Signals that an error has been reached
	 *             unexpectedly while parsing" the cron expression
	 *             {@link String}.
	 */
	public SynchronizeIndexAddTriggerMessage(String triggerName,
			String cronExpression, JobDetail synchronizeIndexJobDetail)
			throws ParseException {
		setTrigger(triggerName, cronExpression);
		setJobDetail(synchronizeIndexJobDetail);
	}

}
