package labrador.application.commandline;


/**
 * This represents the minimum necessary functionality for a command line
 * executable.
 * 
 * @author jtwebb
 * 
 */
public interface CommandLineApplication {

	/**
	 * Perform operations based on the specified command line arguments.
	 * 
	 * @param args
	 *            The command line arguments to execute.
	 */
	public abstract void execute(String[] args);

	/**
	 * This will write usage or help statements to standard out.
	 */
	public abstract void printUsage();

}