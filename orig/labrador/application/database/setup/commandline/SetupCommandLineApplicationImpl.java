package labrador.application.database.setup.commandline;

import java.io.IOException;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;

import labrador.application.commandline.CommandLineApplication;
import labrador.driver.database.setup.DatabaseSetupDriver;
import labrador.search.SearchProviderException;

/**
 * <p>
 * Display Help -h<br>
 * <p>
 * Setup User Database -s<br>
 * Create New User Database (no setup) -n<br>
 * Clean User Database (no create) -c<br>
 * 
 * @author jtwebb
 * 
 */
public class SetupCommandLineApplicationImpl implements CommandLineApplication {

	private Option clean_user_database_option;
	private Parser command_parser;
	private Option create_new_user_database_option;
	private Option help_option;
	private Group options_group;
	private DatabaseSetupDriver setup_driver;
	private Option setup_user_database_option;

	public SetupCommandLineApplicationImpl() {
		this(null);
	}

	public SetupCommandLineApplicationImpl(DatabaseSetupDriver setupDriver) {
		setSetupDriver(setupDriver);
		setCommandParser(new Parser());
		buildOptions();
	}

	protected void buildOptions() {
		final DefaultOptionBuilder optionBuilder = new DefaultOptionBuilder();
		// final ArgumentBuilder argumentBuilder = new ArgumentBuilder();
		final GroupBuilder groupBuilder = new GroupBuilder();

		setSetupUserDatabaseOption(optionBuilder
				.withId('s')
				.withShortName("s")
				.withLongName("setup")
				.withRequired(false)
				.withDescription(
						"Creates and cleans a user database so that it is blank and ready for use.")
				.create());

		setCreateNewUserDatabaseOption(optionBuilder
				.withId('n')
				.withShortName("n")
				.withLongName("newdb")
				.withRequired(false)
				.withDescription(
						"Creates a new user database. That database will not be setup for use. It will simply be empty.")
				.create());

		setCleanUserDatabaseOption(optionBuilder
				.withId('c')
				.withShortName("c")
				.withLongName("clean")
				.withRequired(false)
				.withDescription(
						"Cleans an existing database, making it blank and ready for use.")
				.create());

		setHelpOption(optionBuilder.withId('h').withShortName("h")
				.withLongName("help").withRequired(false).withDescription(
						"Displays the help text for this program.").create());

		setOptionsGroup(groupBuilder.withName("Options").withOption(
				getHelpOption()).withOption(getSetupUserDatabaseOption())
				.withOption(getCleanUserDatabaseOption()).withOption(
						getCreateNewUserDatabaseOption()).create());
	}

	public void execute(String[] args) {
		if (getSetupDriver() == null) {
			System.err
					.println("Error, no database setup driver found. Exiting.");
			System.exit(1);
		} else if (getSetupDriver().getDataSource() == null) {
			System.err.println("Error, no datasource found. Exiting.");
			System.exit(1);
		} else {

			CommandLine commands = parse(args);

			if (args.length == 0 || commands.hasOption(help_option)) {
				printUsage();
				System.exit(0);
			}

			perfromSequentialOperations(commands);
		}
	}

	protected Option getCleanUserDatabaseOption() {
		return clean_user_database_option;
	}

	/**
	 * Returns the {@link Parser} this {@link CommandLineApplication} will use
	 * to parse command line arguments.
	 * 
	 * @return the {@link Parser} this {@link CommandLineApplication} will use
	 *         to parse command line arguments.
	 */
	protected Parser getCommandParser() {
		return command_parser;
	}

	protected Option getCreateNewUserDatabaseOption() {
		return create_new_user_database_option;
	}

	protected Option getHelpOption() {
		return help_option;
	}

	protected Group getOptionsGroup() {
		return options_group;
	}

	public DatabaseSetupDriver getSetupDriver() {
		return setup_driver;
	}

	protected Option getSetupUserDatabaseOption() {
		return setup_user_database_option;
	}

	public void printUsage() {
		HelpFormatter usageCreater = new HelpFormatter();
		usageCreater.setShellCommand("LabradorDatabaseSetupApplication");
		usageCreater.setGroup(getOptionsGroup());
		usageCreater.print();
	}

	protected void setCleanUserDatabaseOption(Option cleanUserDatabaseOption) {
		this.clean_user_database_option = cleanUserDatabaseOption;
	}

	/**
	 * Sets the {@link Parser} this {@link CommandLineApplication} will use to
	 * parse command line arguments.
	 * 
	 * @param parser
	 *            The {@link Parser} this {@link CommandLineApplication} will
	 *            use to parse command line arguments.
	 */
	protected void setCommandParser(Parser parser) {
		command_parser = parser;
	}

	protected void setCreateNewUserDatabaseOption(
			Option createNewUserDatabaseOption) {
		create_new_user_database_option = createNewUserDatabaseOption;
	}

	protected void setHelpOption(Option helpOption) {
		this.help_option = helpOption;
	}

	protected void setOptionsGroup(Group optionsGroup) {
		options_group = optionsGroup;
	}

	public void setSetupDriver(DatabaseSetupDriver setupDriver) {
		this.setup_driver = setupDriver;
	}

	protected void setSetupUserDatabaseOption(Option setupUserDatabaseOption) {
		setup_user_database_option = setupUserDatabaseOption;
	}

	protected CommandLine parse(String[] args) {
		CommandLine commands = null;
		getCommandParser().setGroup(getOptionsGroup());
		try {
			commands = getCommandParser().parse(args);
		} catch (OptionException oe) {
			System.err.println("Could not parse commands! " + oe);
			printUsage();
			System.exit(1);
		}
		return commands;
	}

	protected void perfromSequentialOperations(CommandLine commands) {
		for (Object o : commands.getOptions()) {
			Option op = (Option) o;

			System.out.println("Starting: " + op);

			switch ((char) op.getId()) {
			case 's':
				setupDatabase();
				break;
			case 'n':
				createNewDatabase();
				break;
			case 'c':
				cleanDatabase();
				break;
			default:
				System.err.println("Unknown Command: " + op);
				break;
			}

			System.out.println("Completed: " + op);
		}
	}

	protected void cleanDatabase() {
		try {
			getSetupDriver().cleanDatabase();
		} catch (IOException ioe) {
			System.err.println("Could not clean database.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (SearchProviderException spe) {
			System.err.println("Could not clean database.");
			System.err.println("Error: " + spe.getMessage());
		}
	}

	protected void createNewDatabase() {
		getSetupDriver().createUserDatabase();
	}

	protected void setupDatabase() {
		try {
			getSetupDriver().setupUserDatabase();
		} catch (IOException ioe) {
			System.err.println("Could not setup database.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (SearchProviderException spe) {
			System.err.println("Could not setup database.");
			System.err.println("Error: " + spe.getMessage());
		}
	}

}
