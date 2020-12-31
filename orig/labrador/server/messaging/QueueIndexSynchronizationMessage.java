package labrador.server.messaging;

import java.io.Serializable;

import pandorasbox.simpleclientserver.messaging.BasicMessage;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This {@link Message} tells the {@link Server} to synchronize the index at the
 * next opportunity.
 * 
 * @author jtwebb
 * 
 */
public class QueueIndexSynchronizationMessage extends BasicMessage implements
		LabradorMessage, Serializable {
	private static final long serialVersionUID = 0L;

	/**
	 * Creates a new {@link QueueIndexSynchronizationMessage}.
	 */
	public QueueIndexSynchronizationMessage() {
		super("Synchronize the index");
	}

}
