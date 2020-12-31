package labrador.driver.server.query;

import pandorasbox.simpleclientserver.client.ClientConnectionInformation;
import pandorasbox.simpleclientserver.messaging.Message;

public class SilentIgnoreQueryErrorResponseCallback implements
		QueryErrorResponseCallback {

	public boolean errorResponseReceived(Message errorResponse,
			ClientConnectionInformation clientInformation) {
		return true;
	}

}
