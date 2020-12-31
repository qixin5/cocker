package labrador.driver.server.regulation;

import pandorasbox.simpleclientserver.client.Client;
import pandorasbox.simpleclientserver.messaging.FailureMessage;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This {@link Exception} is thrown when the a {@link Client} recieves a
 * {@link Message} from a {@link Server} that the {@link Client} deems some how
 * in error. Usually this means the {@link Server} replyed with an instance of
 * {@link FailureMessage}.
 * 
 * @author jtwebb
 * 
 */
public class ServerErrorResponseException extends Exception {

	private static final long serialVersionUID = 5803842779123579335L;

	private Message response_message;

	/**
	 * Creates a new {@link ServerErrorResponseException} with the specified
	 * response {@link Message}.
	 * 
	 * @param responseMessage
	 *            The erroneous {@link Message} which the {@link Client}
	 *            received from the {@link Server}.
	 */
	public ServerErrorResponseException(Message responseMessage) {
		setResposneMessage(responseMessage);
	}

	/**
	 * Sets the erroneous {@link Message} which the {@link Client} received from
	 * the {@link Server}.
	 * 
	 * @param responseMessage
	 *            The erroneous {@link Message} which the {@link Client}
	 *            received from the {@link Server}.
	 */
	protected void setResposneMessage(Message responseMessage) {
		response_message = responseMessage;
	}

	/**
	 * Returns the erroneous {@link Message} which the {@link Client} received
	 * from the {@link Server}.
	 * 
	 * @return the erroneous {@link Message} which the {@link Client} received
	 *         from the {@link Server}.
	 */
	public Message getResponseMessage() {
		return response_message;
	}

}
