package labrador.driver.server.query;

import pandorasbox.simpleclientserver.client.ClientConnectionInformation;

public interface QueryExceptionCallback {

	public abstract boolean caughtException(Exception exception,
			ClientConnectionInformation clientInformation);

}
