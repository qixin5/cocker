package labrador.driver.server.query;

import pandorasbox.simpleclientserver.client.ClientConnectionInformation;
import pandorasbox.simpleclientserver.messaging.Message;

public class StandardErrorIgnoreQueryErrorResponseCallback implements
		QueryErrorResponseCallback {

	public boolean errorResponseReceived(Message errorResponse,
			ClientConnectionInformation clientInformation) {
		System.err.println("Error performing query through server"
				+ clientInformation + ". Invalid response: "
				+ errorResponse.getMessageText());
		return true;
	}

}