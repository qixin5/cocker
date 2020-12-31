package labrador.application.database.setup.commandline;

import labrador.application.commandline.CommandLineApplication;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SetupCommandLineExecutable {
	
	public static void main(String[] args) {
		args = new String[] { "-s" };
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		CommandLineApplication app = (CommandLineApplication)context.getBean("setupCommandLineApplication");
		app.execute(args);
	}

}
