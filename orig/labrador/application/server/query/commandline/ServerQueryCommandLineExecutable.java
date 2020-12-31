package labrador.application.server.query.commandline;

import pandorasbox.simpleclientserver.client.SimpleClient;
import labrador.application.commandline.CommandLineApplication;
import labrador.driver.server.query.ServerQueryDriverImpl;

public class ServerQueryCommandLineExecutable {

	public static void main(String[] args) {
		//We don't use the Spring Framework here because it takes time to start up
		//and we want queries to be as fast as possible
		CommandLineApplication app = new ServerQueryCommandLineApplicationImpl(
				new ServerQueryDriverImpl(new SimpleClient()));
		app.execute(args);
	}

}
