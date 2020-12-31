package labrador.application.server.query.commandline;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;

import labrador.analysis.Result;
import labrador.application.commandline.CommandLineApplication;
import labrador.driver.server.query.QueryClientConnectionInformation;
import labrador.driver.server.query.ServerQueryDriver;

public class ServerQueryCommandLineApplicationImpl implements CommandLineApplication {

	private Parser command_parser;
	private ServerQueryDriver driver;
	private Group options_group;
	private Option use_hostname_option;
	private Option use_max_aggregate_results_option;
	private Option use_port_option;
	private Option use_timeout_option;

	public ServerQueryCommandLineApplicationImpl() {
		this(null);
	}

	public ServerQueryCommandLineApplicationImpl(ServerQueryDriver driver) {
		setDriver(driver);
		setCommandParser(new Parser());
		buildOptions();
	}

	protected void buildOptions() {
		final DefaultOptionBuilder optionBuilder = new DefaultOptionBuilder();
		final ArgumentBuilder argumentBuilder = new ArgumentBuilder();
		final GroupBuilder groupBuilder = new GroupBuilder();

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
										"A number between 0 and 65535 representing the port to use when connecting to the server.")
								.create()).withRequired(false).withDescription(
						"Sets the port.").create());

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
										"The hostname to use when connecting to the server.")
								.create()).withRequired(false).withDescription(
						"Sets the hostname.").create());

		setUseTimeoutOption(optionBuilder
				.withId('t')
				.withShortName("t")
				.withLongName("timeout")
				.withArgument(
						argumentBuilder
								.withName("timeout")
								.withMinimum(1)
								.withMaximum(1)
								.withDescription(
										"The timeout to use when connecting to the server. 0 is treated as no timeout.")
								.create()).withRequired(false).withDescription(
						"Sets the timeout.").create());

		setUseMaxAggregateResultsOption(optionBuilder
				.withId('m')
				.withShortName("m")
				.withLongName("maxresults")
				.withArgument(
						argumentBuilder
								.withName("maxresults")
								.withMinimum(1)
								.withMaximum(1)
								.withDescription(
										"The maximum number of results the server should return. It may return fewer, but no more.")
								.create())
				.withRequired(false)
				.withDescription(
						"Sets the maximum number of results the server should return..")
				.create());

		setOptionsGroup(groupBuilder.withName("Options").withOption(
				getUsePortOption()).withOption(getUseTimeoutOption())
				.withOption(getUseHostnameOption()).withOption(
						getUseMaxAggregateResultsOption()).create());
	}

	protected QueryClientConnectionInformation createConnectionInformation(
			CommandLine serverCommand) {
		QueryClientConnectionInformation result = new QueryClientConnectionInformation();

		if (serverCommand.hasOption(getUseHostnameOption())) {
			result.setHostname((String) serverCommand
					.getValue(getUseHostnameOption()));
		}

		if (serverCommand.hasOption(getUsePortOption())) {
			result.setPort(Integer.parseInt((String) serverCommand
					.getValue(getUsePortOption())));
		}

		if (serverCommand.hasOption(getUseTimeoutOption())) {
			result.setTimeout(Integer.parseInt((String) serverCommand
					.getValue(getUseTimeoutOption())));
		}

		if (serverCommand.hasOption(getUseMaxAggregateResultsOption())) {
			result.setMaxAggregateResults(Integer
					.parseInt((String) serverCommand
							.getValue(getUseMaxAggregateResultsOption())));
		}

		return result;
	}

	public void execute(String[] args) {
		if (getDriver() == null) {
			System.err.println("Error, no server driver found. Exiting.");
			System.exit(1);
		} else {
			if (getDriver().getQueryClient() == null) {
				System.err
						.println("Warning, no query client found. You will not be able to query servers. Exiting.");
				System.exit(1);
			} else {
				if (args.length == 0) {
					System.out.println("Error: no options specified.");
					printUsage();
					System.exit(1);
				}

				StringBuilder recompositer = new StringBuilder();
				for (String arg : args) {
					recompositer.append(arg).append(" ");
				}

				String recomposition = recompositer.toString();

				if (recomposition.contains("-h")
						|| recomposition.contains("-help")) {
					System.out.println("Displaying help text:");
					printUsage();
					System.exit(0);
				}

				String[] connectionStrings = recomposition.split(":");

				if (connectionStrings.length < 2) {
					System.out
							.println("Error: a search string and at least 1 server must be specified!");
					printUsage();
					System.exit(0);
				}

				for (int i = 1; i < connectionStrings.length; i++) {
					CommandLine serverCommand = parseServerOptions(connectionStrings[i]
							.trim().split(" "));
					getDriver().getServerQueryClientConnections().add(
							createConnectionInformation(serverCommand));
				}

				List<List<Result>> results = getDriver().queryServers(
						connectionStrings[0]);

				printResults(results);
			}
		}
	}

	/**
	 * Returns the {@link Parser} this {@link CommandLineApplication} will use
	 * to parse command line arguments.
	 * 
	 * @return the {@link Parser} this {@link CommandLineApplication} will use
	 *         to parse command line arguments.
	 */
	public Parser getCommandParser() {
		return command_parser;
	}

	public ServerQueryDriver getDriver() {
		return driver;
	}

	protected Group getOptionsGroup() {
		return options_group;
	}

	protected Option getUseHostnameOption() {
		return use_hostname_option;
	}

	protected Option getUseMaxAggregateResultsOption() {
		return use_max_aggregate_results_option;
	}

	protected Option getUsePortOption() {
		return use_port_option;
	}

	protected Option getUseTimeoutOption() {
		return use_timeout_option;
	}

	protected CommandLine parseServerOptions(String[] args) {
		CommandLine commands = null;
		getCommandParser().setGroup(getOptionsGroup());
		try {
			commands = getCommandParser().parse(args);
		} catch (OptionException oe) {
			System.err.println("Could not parse options for server! " + oe);
			printUsage();
			System.exit(1);
		}
		return commands;
	}

	protected void printResults(List<List<Result>> results) {
		List<Result> mergedResults = new LinkedList<Result>();

		for (List<Result> sublist : results) {
			mergedResults.addAll(sublist);
		}

		Collections.sort(mergedResults);

		for (Result result : mergedResults) {
			System.out.println("file://" + result.getFilePath());
		}
	}

	public void printUsage() {
		System.out
				.println("-h, -help, or no arguments will show this usage help text.");
		System.out
				.println("To use the program first specify a search string followed by a \':\' followed by a \':\' sperated list of servers to search. The format for specifying servers is as specified:");

		HelpFormatter usageCreater = new HelpFormatter();
		usageCreater.setShellCommand("Server specification");
		usageCreater.setGroup(getOptionsGroup());
		usageCreater.print();

		System.out
				.println("Example:\nroman AND numeral:-p 1322 -n localhost -m 10:-p19992 -n anotherhost -t 10000");
	}

	/**
	 * Sets the {@link Parser} this {@link CommandLineApplication} will use to
	 * parse command line arguments.
	 * 
	 * @param parser
	 *            The {@link Parser} this {@link CommandLineApplication} will
	 *            use to parse command line arguments.
	 */
	public void setCommandParser(Parser parser) {
		command_parser = parser;
	}

	public void setDriver(ServerQueryDriver driver) {
		this.driver = driver;
	}

	protected void setOptionsGroup(Group optionsGroup) {
		options_group = optionsGroup;
	}

	protected void setUseHostnameOption(Option useHostnameOption) {
		use_hostname_option = useHostnameOption;
	}

	protected void setUseMaxAggregateResultsOption(
			Option useMaxAggregateResultsOption) {
		use_max_aggregate_results_option = useMaxAggregateResultsOption;
	}

	protected void setUsePortOption(Option usePortOption) {
		use_port_option = usePortOption;
	}

	protected void setUseTimeoutOption(Option useTimeoutOption) {
		use_timeout_option = useTimeoutOption;
	}

}
