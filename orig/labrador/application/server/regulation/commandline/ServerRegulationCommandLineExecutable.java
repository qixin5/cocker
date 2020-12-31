package labrador.application.server.regulation.commandline;

import labrador.application.commandline.CommandLineApplication;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerRegulationCommandLineExecutable {
	
	public static void main(String[] args) {
//		args = new String[] { "-a", "std sync", "0 4 23 * * ? *" };
//		args = new String[] { "-r", "std sync" };
//		args = new String[] { "-s" };
//		args = new String[] { "-k" };
//		args = new String[] { "-l" };
//		args = new String[] { "-i" };
		// TODO take out post debug
		
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		CommandLineApplication app = (CommandLineApplication)context.getBean("serverCommandLineApplication");
		app.execute(args);
	}

}
