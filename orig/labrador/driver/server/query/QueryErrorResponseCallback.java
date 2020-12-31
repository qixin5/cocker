package labrador.driver.server.query;

import pandorasbox.simpleclientserver.client.ClientConnectionInformation;
import pandorasbox.simpleclientserver.messaging.Message;

public interface QueryErrorResponseCallback {

	public abstract boolean errorResponseReceived(Message errorResponse, ClientConnectionInformation clientInformation);

}
