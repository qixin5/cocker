package labrador.server.messaging;

import java.io.Serializable;

import pandorasbox.simpleclientserver.messaging.BasicMessage;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This {@link Message} tells the {@link Server} to optimize the index at the
 * next opportunity.
 * 
 * @author jtwebb
 * 
 */
public class QueueIndexOptimizationMessage extends BasicMessage implements
		LabradorMessage, Serializable {
	private static final long serialVersionUID = -6027027358242867117L;

	/**
	 * Creates a new {@link QueueIndexOptimizationMessage}.
	 */
	public QueueIndexOptimizationMessage() {
		super("Optimize the index");
	}

}
