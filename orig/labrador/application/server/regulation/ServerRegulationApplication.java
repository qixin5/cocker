package labrador.application.server.regulation;

import labrador.driver.server.regulation.ServerRegulationDriver;

public interface ServerRegulationApplication {

	public abstract void addSynchronizationTrigger(String triggerName,
			String cronExpression);

	public abstract void displayServerSchedule();

	public abstract void displayServerStatus();

	public abstract void ensureConnectivity();

	/**
	 * Returns the {@link CommandLineServerDriver} this
	 * {@link ServerCommandLineApplicationImpl} will use to administrate
	 * servers.
	 * 
	 * @return the {@link CommandLineServerDriver} this
	 *         {@link ServerCommandLineApplicationImpl} will use to administrate
	 *         servers.
	 */
	public abstract ServerRegulationDriver getServerDriver();

	public abstract void queueIndexSynchronization();

	public abstract void removeSynchronizationTrigger(String triggerName);

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
	public abstract void setServerDriver(ServerRegulationDriver serverDriver);

	public abstract void startServer();

	public abstract void stopServer();

	public abstract void updateSynchronizationTrigger(String triggerName,
			String cronExpression);

}