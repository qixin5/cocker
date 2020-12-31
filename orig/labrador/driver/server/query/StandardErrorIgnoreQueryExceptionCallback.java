package labrador.driver.server.query;

import pandorasbox.simpleclientserver.client.ClientConnectionInformation;

public class StandardErrorIgnoreQueryExceptionCallback implements
		QueryExceptionCallback {

	public boolean caughtException(Exception exception,
			ClientConnectionInformation clientInformation) {
		System.err.println("Error performing query through server"
				+ clientInformation + ". Exception: " + exception.getMessage());
		return true;
	}

}