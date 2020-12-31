package labrador.server.messaging;

import labrador.server.SynchronizeIndexJobDetail;

import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import pandorasbox.scheduling.server.messaging.RemoveTriggerMessage;

/**
 * This class extends the normal {@link RemoveTriggerMessage} and fills in some
 * blanks to facilitate easier scheduling within labrador.
 * 
 * @author jtwebb
 * 
 */
public class SynchronizeIndexRemoveTriggerMessage extends RemoveTriggerMessage
		implements LabradorMessage {
	private static final long serialVersionUID = -5534099582997579452L;

	/**
	 * Creates a new {@link SynchronizeIndexRemoveTriggerMessage} with no
	 * specified {@link Trigger}.
	 */
	public SynchronizeIndexRemoveTriggerMessage() {
		super();
	}

	/**
	 * Creates a new {@link SynchronizeIndexRemoveTriggerMessage} with a
	 * {@link Trigger} built from the specified trigger name.
	 * 
	 * @param triggerName
	 *            The name to use when constructing the {@link Trigger}.
	 */
	public SynchronizeIndexRemoveTriggerMessage(String triggerName) {
		super();
		setTriggerName(triggerName);
	}

	/**
	 * Sets the name to use when constructing the {@link Trigger}.
	 * 
	 * @param triggerName
	 *            The name to use when constructing the {@link Trigger}.
	 */
	public void setTriggerName(String triggerName) {
		setTrigger(new SimpleTrigger(triggerName,
				SynchronizeIndexJobDetail.DEFAULT_GROUP_NAME));
	}

}
