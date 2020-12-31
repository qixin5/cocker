package labrador.driver.server.regulation;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.quartz.JobDetail;

import pandorasbox.scheduling.server.ScheduleListing;
import pandorasbox.simpleclientserver.client.Client;
import pandorasbox.simpleclientserver.messaging.MalformedMessageException;
import pandorasbox.simpleclientserver.server.Server;
import pandorasbox.simpleclientserver.server.ServerStatus;

public interface ServerRegulationDriver {

	public abstract void addSynchronizationTrigger(String triggerName,
			String cronExpression) throws IOException,
			MalformedMessageException, ParseException,
			ServerErrorResponseException;

	public abstract void connect() throws IOException;

	public abstract void connect(boolean autoStart) throws IOException;

	public abstract void disconnect();

	public abstract JobDetail getIndexSynchronizationJobDetail();

	public abstract Client getRegulationClient();

	public abstract Server getServerPrototype();

	public abstract ScheduleListing getServerSchedule() throws IOException,
			MalformedMessageException, ServerErrorResponseException;

	public abstract ServerStatus getServerStatus() throws IOException,
			MalformedMessageException, ServerErrorResponseException;

	public abstract boolean isConnected();

	public abstract void queueBlacklistFiles(List<String> files) throws IOException,
			MalformedMessageException, ServerErrorResponseException;

	public abstract void queueIndexSynchronization() throws IOException,
			MalformedMessageException, ServerErrorResponseException;

	public abstract void queueMonitorFiles(List<String> files) throws IOException,
			MalformedMessageException, ServerErrorResponseException;

	public abstract void queueUnblacklistFiles(List<String> files)
			throws IOException, MalformedMessageException,
			ServerErrorResponseException;

	public abstract void queueUnmonitorFiles(List<String> files) throws IOException,
			MalformedMessageException, ServerErrorResponseException;

	public abstract void removeSynchronizationTrigger(String triggerName)
			throws IOException, MalformedMessageException,
			ServerErrorResponseException;

	public abstract void setIndexSynchronizationJobDetail(
			JobDetail indexSynchronizationJobDetail);

	public abstract void setRegulationClient(Client regulationClient);

	public abstract void setServerPrototype(Server serverPrototype);

	public abstract void startServer() throws IOException;

	public abstract void stopServer() throws IOException,
			MalformedMessageException, ServerErrorResponseException;

	public abstract void stopServerWhenDone() throws IOException,
			MalformedMessageException, ServerErrorResponseException;

	public abstract void updateSynchronizationTrigger(String triggerName,
			String cronExpression) throws IOException,
			MalformedMessageException, ParseException,
			ServerErrorResponseException;
}
