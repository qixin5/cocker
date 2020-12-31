package labrador.driver.server.query;

import pandorasbox.simpleclientserver.client.ClientConnectionInformation;

public class SilentIgnoreQueryExceptionCallback implements
		QueryExceptionCallback {

	public boolean caughtException(Exception exception,
			ClientConnectionInformation clientInformation) {
		return true;
	}

}
