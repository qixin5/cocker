package labrador.application.server.regulation.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import labrador.application.commandline.CommandLineApplication;
import labrador.application.server.regulation.ServerRegulationApplication;
import labrador.driver.server.regulation.ServerErrorResponseException;
import labrador.driver.server.regulation.ServerRegulationDriver;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;

import pandorasbox.scheduling.server.ScheduleListing;
import pandorasbox.simpleclientserver.messaging.MalformedMessageException;
import pandorasbox.simpleclientserver.server.ServerStatus;

/**
 * <p>
 * Display Help -h<br>
 * <p>
 * Add Synchronization Trigger -a<br>
 * Remove Synchronization Trigger -r<br>
 * Update Synchronization Trigger -u<br>
 * <p>
 * Display Server Status -t<br>
 * Display Server Schedule -l<br>
 * <p>
 * Start Server -s<br>
 * Stop Server -k<br>
 * <p>
 * Use Hostname -n<br>
 * Use Port -p<br>
 * <p>
 * Queue Index Synchronization -i<br>
 * Monitor Files -m<br>
 * Unmonitor Files -o<br>
 * Blacklist Files -b<br>
 * Unblacklist Files -w<br>
 * <p>
 * Implements a command line application for the administration of servers.
 * 
 * @author jtwebb
 * 
 */
public class ServerRegulationCommandLineApplicationImpl implements
		CommandLineApplication, ServerRegulationApplication {

	private Option add_synchronization_trigger_option;
	private Option blacklist_files_option;
	private Parser command_parser;
	private Option display_server_schedule_option;
	private Option display_server_status_option;
	private Option help_option;
	private Option monitor_files_option;
	private Group options_group;
	private Option remove_synchronization_trigger_option;
	private ServerRegulationDriver server_driver;
	private Option start_server_option;
	private Option stop_server_option;
	private Option sychronize_index_option;
	private Option unblacklist_files_option;
	private Option unmonitor_files_option;
	private Option update_synchronization_trigger_option;
	private Option use_hostname_option;
	private Option use_port_option;

	public ServerRegulationCommandLineApplicationImpl() {
		this(null);
	}

	public ServerRegulationCommandLineApplicationImpl(
			ServerRegulationDriver serverDriver) {
		setServerDriver(serverDriver);
		setCommandParser(new Parser());
		buildOptions();
	}

	public void addSynchronizationTrigger(String triggerName,
			String cronExpression) {
		try {
			getServerDriver().addSynchronizationTrigger(triggerName,
					cronExpression);
		} catch (IOException ioe) {
			System.err.println("Could not add synchronization trigger.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not add synchronization trigger.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ParseException pe) {
			System.err.println("Could not add synchronization trigger.");
			System.err.println("Error: " + pe.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not add synchronization trigger.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	protected void buildOptions() {
		final DefaultOptionBuilder optionBuilder = new DefaultOptionBuilder();
		final ArgumentBuilder argumentBuilder = new ArgumentBuilder();
		final GroupBuilder groupBuilder = new GroupBuilder();

		setAddSynchronizationTriggerOption(optionBuilder
				.withId('a')
				.withShortName("a")
				.withLongName("adsyntrig")
				.withArgument(
						argumentBuilder
								.withName("exp")
								.withMinimum(2)
								.withMaximum(2)
								.withDescription(
										"The name for this synchronization trigger. Followed by a cron expression designating when to trigger index synchronizatoin.")
								.create())
				.withRequired(false)
				.withDescription(
						"Tells the server to add the named index synchronization trigger at the interval of the cron expression. (Trigger name followed by cron expression).")
				.create());

		setBlacklistFilesOption(optionBuilder.withId('b').withShortName("b")
				.withLongName("blacklist").withRequired(false).withDescription(
						"Tells the server to blacklist the specified files.")
				.withArgument(
						argumentBuilder.withName("file").withMinimum(1)
								.withDescription(
										"A file for the server to blacklist.")
								.create()).create());

		setDisplayServerScheduleOption(optionBuilder
				.withId('l')
				.withShortName("l")
				.withLongName("listing")
				.withRequired(false)
				.withDescription(
						"Displays the schedule of a server with the known port.")
				.create());

		setDisplayServerStatusOption(optionBuilder.withId('t').withShortName(
				"t").withLongName("status").withRequired(false)
				.withDescription(
						"Displays the status of a server with the known port.")
				.create());

		setHelpOption(optionBuilder.withId('h').withShortName("h")
				.withLongName("help").withRequired(false).withDescription(
						"Displays the help text for this program.").create());

		setMonitorFilesOption(optionBuilder.withId('m').withShortName("m")
				.withLongName("monitor").withRequired(false).withDescription(
						"Tells the server to monitor the specified files.")
				.withArgument(
						argumentBuilder.withName("file").withMinimum(1)
								.withDescription(
										"A file for the server to monitor.")
								.create()).create());

		setRemoveSynchronizationTriggerOption(optionBuilder
				.withId('r')
				.withShortName("r")
				.withLongName("rmsyntrig")
				.withArgument(
						argumentBuilder
								.withName("trigname")
								.withMinimum(1)
								.withMaximum(1)
								.withDescription(
										"The name of synchronization trigger to remove.")
								.create())
				.withRequired(false)
				.withDescription(
						"Tells the server to update the named index synchronization trigger with the new cron expression.")
				.create());

		setStartServerOption(optionBuilder.withId('s').withShortName("s")
				.withLongName("start").withRequired(false).withDescription(
						"Starts a server on the known port.").create());

		setStopServerOption(optionBuilder.withId('k').withShortName("k")
				.withLongName("stop").withRequired(false).withDescription(
						"Stops a server on the known port.").create());

		setSychronizeIndexOption(optionBuilder
				.withId('i')
				.withShortName("i")
				.withLongName("synindex")
				.withRequired(false)
				.withDescription(
						"Tells the server to synchronize its index immediately.")
				.create());

		setUpdateSynchronizationTriggerOption(optionBuilder
				.withId('u')
				.withShortName("u")
				.withLongName("upsyntrig")
				.withArgument(
						argumentBuilder
								.withName("exp")
								.withMinimum(2)
								.withMaximum(2)
								.withDescription(
										"The name of this synchronization trigger. Followed by a cron expression designating when to trigger index synchronizatoin.")
								.create())
				.withRequired(false)
				.withDescription(
						"Tells the server to update the named index synchronization trigger with the new cron expression.(Trigger name followed by cron expresseion).")
				.create());

		setUnblacklistFilesOption(optionBuilder
				.withId('w')
				.withShortName("w")
				.withLongName("unblacklist")
				.withRequired(false)
				.withDescription(
						"Tells the server to unblacklist the specified files.")
				.withArgument(
						argumentBuilder
								.withName("file")
								.withMinimum(1)
								.withDescription(
										"A file for the server to unblacklist.")
								.create()).create());

		setUnmonitorFilesOption(optionBuilder.withId('o').withShortName("o")
				.withLongName("unmonitor").withRequired(false).withDescription(
						"Tells the server to unmonitor the specified files.")
				.withArgument(
						argumentBuilder.withName("file").withMinimum(1)
								.withDescription(
										"A file for the server to unmonitor.")
								.create()).create());

		setUseHostnameOption(optionBuilder
				.withId('n')
				.withShortName("n")
				.withLongName("hostname")
				.withArgument(
						argumentBuilder
								.withName("hostname")
								.withMinimum(1)
								.withMaximum(1)
								.withDescription(
										"The hostname to use when connecting to or creating the server.")
								.create()).withRequired(false).withDescription(
						"Sets the hostname.").create());

		setUsePortOption(optionBuilder
				.withId('p')
				.withShortName("p")
				.withLongName("port")
				.withArgument(
						argumentBuilder
								.withName("port")
								.withMinimum(1)
								.withMaximum(1)
								.withDescription(
										"A number between 0 and 65535 representing the port to use when connecting to the server or creating the server.")
								.create()).withRequired(false).withDescription(
						"Sets the port.").create());

		setOptionsGroup(groupBuilder.withName("Options").withOption(
				getHelpOption()).withOption(getDisplayServerStatusOption())
				.withOption(getDisplayServerScheduleOption()).withOption(
						getStartServerOption()).withOption(
						getStopServerOption()).withOption(
						getSychronizeIndexOption()).withOption(
						getUpdateSynchronizationTriggerOption()).withOption(
						getAddSynchronizationTriggerOption()).withOption(
						getRemoveSynchronizationTriggerOption()).withOption(
						getUsePortOption()).withOption(getUseHostnameOption())
				.create());
	}

	public void displayServerSchedule() {
		try {
			ScheduleListing schedule = getServerDriver().getServerSchedule();
			System.out.println("Current Server Schedule:");
			System.out.println(schedule);
		} catch (IOException ioe) {
			System.err.println("Could not get server schedule.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not get server schedule.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not get server schedule.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	public void displayServerStatus() {
		try {
			ServerStatus status = getServerDriver().getServerStatus();
			System.out.println("Current Server Status:");
			System.out.println(status);
		} catch (IOException ioe) {
			System.err.println("Could not get server status.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not get server status.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not get server status.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	public void ensureConnectivity() {
		if (!getServerDriver().isConnected()) {
			try {
				getServerDriver().connect(false);
			} catch (IOException ioe) {
				System.err
						.println("Could not connect to server. Please ensure that your connection information is correct, a server is running, and that you have network access to the server.");
				System.err.println("Error: " + ioe.getMessage());
				System.err.println("Attempt to continue anyway (y/N)?");
				BufferedReader bufferedInput = new BufferedReader(
						new InputStreamReader(System.in));
				try {
					String response = bufferedInput.readLine().trim()
							.toLowerCase();
					if (response == "" || response == "n")
						System.exit(1);
				} catch (IOException e1) {
					System.err.println("Could not read response. Exiting...");
					System.exit(1);
				}
			}
		}
	}

	/**
	 * Perform operations based on the specified command line arguments.
	 * 
	 * @param args
	 *            The command line arguments to execute.
	 */
	public void execute(String[] args) {
		if (getServerDriver() == null) {
			System.err
					.println("Error, no server regulation driver found. Exiting.");
			System.exit(1);
		} else {
			if (getServerDriver().getServerPrototype() == null) {
				System.err
						.println("Warning, no server prototype found. You will not be able to start a server.");
			}

			if (getServerDriver().getRegulationClient() == null) {
				System.err
						.println("Warning, no regulation client found. You will not be able to regulate servers.");
			}

			CommandLine commands = parse(args);

			if (args.length == 0 || commands.hasOption(help_option)) {
				printUsage();
				System.exit(0);
			}

			if (commands.hasOption(getUsePortOption())) {
				int port = Integer.parseInt((String) commands
						.getValue(getUsePortOption()));
				System.out.println("using port: " + port);
				if (getServerDriver().getServerPrototype() != null) {
					getServerDriver().getServerPrototype().setPort(port);
				} else {
					System.err
							.println("Not setting server prototype port. No server prototype found");
				}
				if (getServerDriver().getRegulationClient() != null) {
					getServerDriver().getRegulationClient().setPort(port);
				} else {
					System.err
							.println("Not setting client port. No regulation client found");
				}
			}

			if (commands.hasOption(getUseHostnameOption())) {
				String hostname = (String) commands
						.getValue(getUseHostnameOption());
				System.out.println("using hostname: " + hostname);
				if (getServerDriver().getRegulationClient() != null) {
					getServerDriver().getRegulationClient().setServerHostname(
							hostname);
				} else {
					System.err
							.println("Not setting client hostname. No regulation client found");
				}
			}

			perfromSequentialOperations(commands);
		}
	}

	protected Option getAddSynchronizationTriggerOption() {
		return add_synchronization_trigger_option;
	}

	protected Option getBlacklistFilesOption() {
		return blacklist_files_option;
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

	protected Option getDisplayServerScheduleOption() {
		return display_server_schedule_option;
	}

	protected Option getDisplayServerStatusOption() {
		return display_server_status_option;
	}

	protected Option getHelpOption() {
		return help_option;
	}

	protected Option getMonitorFilesOption() {
		return monitor_files_option;
	}

	protected Group getOptionsGroup() {
		return options_group;
	}

	protected Option getRemoveSynchronizationTriggerOption() {
		return remove_synchronization_trigger_option;
	}

	/**
	 * Returns the {@link CommandLineServerDriver} this
	 * {@link ServerCommandLineApplicationImpl} will use to administrate
	 * servers.
	 * 
	 * @return the {@link CommandLineServerDriver} this
	 *         {@link ServerCommandLineApplicationImpl} will use to administrate
	 *         servers.
	 */
	public ServerRegulationDriver getServerDriver() {
		return server_driver;
	}

	protected Option getStartServerOption() {
		return start_server_option;
	}

	protected Option getStopServerOption() {
		return stop_server_option;
	}

	protected Option getSychronizeIndexOption() {
		return sychronize_index_option;
	}

	protected Option getUnblacklistFilesOption() {
		return unblacklist_files_option;
	}

	protected Option getUnmonitorFilesOption() {
		return unmonitor_files_option;
	}

	protected Option getUpdateSynchronizationTriggerOption() {
		return update_synchronization_trigger_option;
	}

	protected Option getUseHostnameOption() {
		return use_hostname_option;
	}

	protected Option getUsePortOption() {
		return use_port_option;
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
			switch ((char) op.getId()) {
			case 'p':
			case 'n':
			case 'v':
			case 'r':
				continue;
			}

			System.out.println("Starting: " + op);

			if (op.getId() == 's') {
				startServer();
			} else {
				ensureConnectivity();
			}

			switch ((char) op.getId()) {
			case 'i':
				queueIndexSynchronization();
				break;
			case 'u':
				updateSynchronizationTrigger((String) commands.getValues(op)
						.get(0), (String) commands.getValues(op).get(1));
				break;
			case 'a':
				addSynchronizationTrigger((String) commands.getValues(op)
						.get(0), (String) commands.getValues(op).get(1));
				break;
			case 'd':
				removeSynchronizationTrigger((String) commands.getValue(op));
				break;
			case 'k':
				stopServer();
				break;
			case 't':
				displayServerStatus();
				break;
			case 'l':
				displayServerSchedule();
				break;
			case 'm':
				List<String> filesToMonitor = new LinkedList<String>();
				for (Object obj : commands.getValues(op)) {
					filesToMonitor.add((String) obj);
				}
				monitorFiles(filesToMonitor);
				break;
			case 'o':
				List<String> filesToUnmonitor = new LinkedList<String>();
				for (Object obj : commands.getValues(op)) {
					filesToUnmonitor.add((String) obj);
				}
				unmonitorFiles(filesToUnmonitor);
				break;
			case 'b':
				List<String> filesToBlacklist = new LinkedList<String>();
				for (Object obj : commands.getValues(op)) {
					filesToBlacklist.add((String) obj);
				}
				blacklistFiles(filesToBlacklist);
				break;
			case 'w':
				List<String> filesToUnblacklist = new LinkedList<String>();
				for (Object obj : commands.getValues(op)) {
					filesToUnblacklist.add((String) obj);
				}
				unblacklistFiles(filesToUnblacklist);
				break;
			default:
				System.err.println("Unknown Command: " + op);
				break;
			}

			System.out.println("Completed: " + op);
		}
	}

	/**
	 * This will write usage or help statements to standard out.
	 */
	public void printUsage() {
		HelpFormatter usageCreater = new HelpFormatter();
		usageCreater.setShellCommand("LabradorServerRegulationApplication");
		usageCreater.setGroup(getOptionsGroup());
		usageCreater.print();
	}

	public void queueIndexSynchronization() {
		try {
			getServerDriver().queueIndexSynchronization();
		} catch (IOException ioe) {
			System.err.println("Could not queue index synchronization.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not queue index synchronization.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not queue index synchronization.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	public void removeSynchronizationTrigger(String triggerName) {
		try {
			getServerDriver().removeSynchronizationTrigger(triggerName);
		} catch (IllegalStateException ise) {
			System.err.println("Could not remove synchronization trigger.");
			System.err.println("Error: " + ise.getMessage());
		} catch (IOException ioe) {
			System.err.println("Could not remove synchronization trigger.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not remove synchronization trigger.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not remove synchronization trigger.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	protected void setAddSynchronizationTriggerOption(
			Option addSynchronizationTriggerOption) {
		add_synchronization_trigger_option = addSynchronizationTriggerOption;
	}

	protected void setBlacklistFilesOption(Option blacklistFilesOption) {
		blacklist_files_option = blacklistFilesOption;
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

	protected void setDisplayServerScheduleOption(
			Option displayServerScheduleOption) {
		display_server_schedule_option = displayServerScheduleOption;
	}

	protected void setDisplayServerStatusOption(Option displayServerStatusOption) {
		display_server_status_option = displayServerStatusOption;
	}

	protected void setHelpOption(Option helpOption) {
		this.help_option = helpOption;
	}

	protected void setMonitorFilesOption(Option monitorFilesOption) {
		monitor_files_option = monitorFilesOption;
	}

	protected void setOptionsGroup(Group optionsGroup) {
		options_group = optionsGroup;
	}

	protected void setRemoveSynchronizationTriggerOption(
			Option removeSynchronizationTriggerOption) {
		remove_synchronization_trigger_option = removeSynchronizationTriggerOption;
	}

	/**
	 * Sets the {@link CommandLineServerDriver} this
	 * {@link ServerCommandLineApplicationImpl} will use to administrate
	 * servers.
	 * 
	 * @param serverDriver
	 *            The {@link CommandLineServerDriver} this
	 *            {@link ServerCommandLineApplicationImpl} will use to
	 *            administrate servers.
	 */
	public void setServerDriver(ServerRegulationDriver serverDriver) {
		this.server_driver = serverDriver;
	}

	protected void setStartServerOption(Option startServerOption) {
		start_server_option = startServerOption;
	}

	protected void setStopServerOption(Option stopServerOption) {
		stop_server_option = stopServerOption;
	}

	protected void setSychronizeIndexOption(Option sychronizeIndexOption) {
		sychronize_index_option = sychronizeIndexOption;
	}

	protected void setUnblacklistFilesOption(Option unblacklistFilesOption) {
		unblacklist_files_option = unblacklistFilesOption;
	}

	protected void setUnmonitorFilesOption(Option unmonitorFilesOption) {
		unmonitor_files_option = unmonitorFilesOption;
	}

	protected void setUpdateSynchronizationTriggerOption(
			Option changeSynchronizationIntervalOption) {
		update_synchronization_trigger_option = changeSynchronizationIntervalOption;
	}

	protected void setUseHostnameOption(Option useHostnameOption) {
		use_hostname_option = useHostnameOption;
	}

	protected void setUsePortOption(Option usePortOption) {
		use_port_option = usePortOption;
	}

	public void startServer() {
		try {
			getServerDriver().startServer();
		} catch (IOException ioe) {
			System.err.println("Could not start server.");
			System.err.println("Error: " + ioe.getMessage());
		}
	}

	public void stopServer() {
		try {
			getServerDriver().stopServer();
		} catch (IOException ioe) {
			System.err.println("Could not stop server.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not stop server.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not stop server.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	public void updateSynchronizationTrigger(String triggerName,
			String cronExpression) {
		try {
			getServerDriver().updateSynchronizationTrigger(triggerName,
					cronExpression);
		} catch (IOException ioe) {
			System.err.println("Could not update synchronization trigger.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not update synchronization trigger.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ParseException pe) {
			System.err.println("Could not update synchronization trigger.");
			System.err.println("Error: " + pe.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not update synchronization trigger.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	public void monitorFiles(List<String> files) {
		try {
			getServerDriver().queueMonitorFiles(files);
		} catch (IOException ioe) {
			System.err.println("Could not monitor some or all files.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not monitor some or all files.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not monitor some or all files.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	public void unmonitorFiles(List<String> files) {
		try {
			getServerDriver().queueUnmonitorFiles(files);
		} catch (IOException ioe) {
			System.err.println("Could not unmonitor some or all files.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not unmonitor some or all files.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not unmonitor some or all files.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	public void blacklistFiles(List<String> files) {
		try {
			getServerDriver().queueBlacklistFiles(files);
		} catch (IOException ioe) {
			System.err.println("Could not blacklist some or all files.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not blacklist some or all files.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not monitor some or all files.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

	public void unblacklistFiles(List<String> files) {
		try {
			getServerDriver().queueUnblacklistFiles(files);
		} catch (IOException ioe) {
			System.err.println("Could not unblacklist some or all files.");
			System.err.println("Error: " + ioe.getMessage());
		} catch (MalformedMessageException mme) {
			System.err.println("Could not unblacklist some or all files.");
			System.err.println("Error: " + mme.getMessage());
		} catch (ServerErrorResponseException sere) {
			System.err.println("Could not unblacklist some or all files.");
			System.err.println("Error: " + sere.getMessage());
		}
	}

}