package labrador.driver.server.regulation;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import labrador.server.messaging.QueueBlacklistFilesMessage;
import labrador.server.messaging.QueueMonitorFilesMessage;
import labrador.server.messaging.SynchronizeIndexAddTriggerMessage;
import labrador.server.messaging.QueueIndexSynchronizationMessage;
import labrador.server.messaging.SynchronizeIndexRemoveTriggerMessage;
import labrador.server.messaging.SynchronizeIndexUpdateTriggerMessage;
import labrador.server.messaging.QueueUnblacklistFilesMessage;
import labrador.server.messaging.QueueUnmonitorFilesMessage;

import org.quartz.JobDetail;

import pandorasbox.scheduling.server.ScheduleListing;
import pandorasbox.scheduling.server.messaging.RequestScheduleListingMessage;
import pandorasbox.scheduling.server.messaging.ScheduleListingResponseMessage;
import pandorasbox.simpleclientserver.client.Client;
import pandorasbox.simpleclientserver.client.Session;
import pandorasbox.simpleclientserver.messaging.FailureMessage;
import pandorasbox.simpleclientserver.messaging.MalformedMessageException;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.messaging.RequestStatusMessage;
import pandorasbox.simpleclientserver.messaging.StatusResponseMessage;
import pandorasbox.simpleclientserver.messaging.StopServerMessage;
import pandorasbox.simpleclientserver.messaging.StopServerWhenDoneMessage;
import pandorasbox.simpleclientserver.server.Server;
import pandorasbox.simpleclientserver.server.ServerStatus;

public class ServerRegulationDriverImpl implements ServerRegulationDriver {

	private Session client_session;
	private JobDetail index_synchronization_job_detail;
	private Client regulation_client;
	private Server server_prototype;

	public ServerRegulationDriverImpl() {
		this(null, null, null);
	}

	public ServerRegulationDriverImpl(Client regulationClient) {
		this(null, regulationClient, null);
	}

	public ServerRegulationDriverImpl(Client regulationClient,
			JobDetail indexSynchronizationJobDetail) {
		this(null, regulationClient, indexSynchronizationJobDetail);
	}

	public ServerRegulationDriverImpl(JobDetail indexSynchronizationJobDetail) {
		this(null, null, indexSynchronizationJobDetail);
	}

	public ServerRegulationDriverImpl(Server serverPrototype) {
		this(serverPrototype, null, null);
	}

	public ServerRegulationDriverImpl(Server serverPrototype,
			Client regulationClient) {
		this(serverPrototype, regulationClient, null);
	}

	public ServerRegulationDriverImpl(Server serverPrototype,
			Client regulationClient, JobDetail indexSynchronizationJobDetail) {
		setServerPrototype(serverPrototype);
		setRegulationClient(regulationClient);
		setIndexSynchronizationJobDetail(indexSynchronizationJobDetail);
	}

	public ServerRegulationDriverImpl(Server serverPrototype,
			JobDetail indexSynchronizationJobDetail) {
		this(serverPrototype, null, indexSynchronizationJobDetail);
	}

	public void addSynchronizationTrigger(String triggerName,
			String cronExpression) throws IOException,
			MalformedMessageException, ParseException,
			ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new SynchronizeIndexAddTriggerMessage(triggerName,
						cronExpression, getIndexSynchronizationJobDetail()));
		verifyServerResponse(response);
	}

	protected void attemptServerConnection() throws IOException {
		setClientSession(getRegulationClient().createSession());
	}

	public void connect() throws IOException {
		connect(true);
	}

	public void connect(boolean autoStart) throws IOException {
		try {
			attemptServerConnection();
		} catch (IOException ioe) {
			startServer();
		}
	}

	public void disconnect() {
		if (isConnected())
			getClientSession().close();
	}

	protected Session getClientSession() {
		return client_session;
	}

	public JobDetail getIndexSynchronizationJobDetail() {
		return index_synchronization_job_detail;
	}

	public Client getRegulationClient() {
		return regulation_client;
	}

	public Server getServerPrototype() {
		return server_prototype;
	}

	public ScheduleListing getServerSchedule() throws IOException,
			MalformedMessageException, ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new RequestScheduleListingMessage());
		if (response instanceof ScheduleListingResponseMessage) {
			return ((ScheduleListingResponseMessage) response)
					.getScheduleListing();
		} else {
			throw new ServerErrorResponseException(response);
		}
	}

	public ServerStatus getServerStatus() throws IOException,
			MalformedMessageException, ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new RequestStatusMessage());
		if (response instanceof StatusResponseMessage) {
			return ((StatusResponseMessage) response).getServerStatus();
		} else {
			throw new ServerErrorResponseException(response);
		}
	}

	public boolean isConnected() {
		return getClientSession() != null && getClientSession().isOpen();
	}

	public void queueIndexSynchronization() throws IOException,
			MalformedMessageException, ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new QueueIndexSynchronizationMessage());
		verifyServerResponse(response);
	}

	public void removeSynchronizationTrigger(String triggerName)
			throws IOException, MalformedMessageException,
			ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new SynchronizeIndexRemoveTriggerMessage(triggerName));
		verifyServerResponse(response);
	}

	protected void setClientSession(Session clientSession) {
		this.client_session = clientSession;
	}

	public void setIndexSynchronizationJobDetail(
			JobDetail indexSynchronizationJobDetail) {
		index_synchronization_job_detail = indexSynchronizationJobDetail;
	}

	public void setRegulationClient(Client regulationClient) {
		regulation_client = regulationClient;
	}

	public void setServerPrototype(Server serverPrototype) {
		server_prototype = serverPrototype;
	}

	/**
	 * This will attempt to start a {@link Server} and open a {@link Session}
	 * with the {@link Server}. If the attempt to open a {@link Session} fails,
	 * then the {@link Server} will be stopped explicitly (unlike the
	 * {@link ServerRegulationDriver#stopServer()} which sends a {@link Message}
	 * telling the {@link Server} to stop).
	 * 
	 * @throws IOException
	 */
	public void startServer() throws IOException {
		getServerPrototype().start();
		try {
			attemptServerConnection();
		} catch (IOException ioe) {
			setClientSession(null);
			getServerPrototype().stop();
			throw ioe;
		}
	}

	/**
	 * This will tell the {@link Server} to stop and automatically disconnect
	 * the {@link Client}.
	 * 
	 * @throws IOException
	 */
	public void stopServer() throws IOException, MalformedMessageException,
			ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new StopServerMessage());
		verifyServerResponse(response);
		disconnect();
	}

	public void stopServerWhenDone() throws IOException,
			MalformedMessageException, ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new StopServerWhenDoneMessage());
		verifyServerResponse(response);
	}
	
	public void queueMonitorFiles(List<String> files) throws IOException,
			MalformedMessageException, ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new QueueMonitorFilesMessage(files));
		verifyServerResponse(response);
	}
	
	public void queueUnmonitorFiles(List<String> files) throws IOException,
			MalformedMessageException, ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new QueueUnmonitorFilesMessage(files));
		verifyServerResponse(response);
	}
	
	public void queueBlacklistFiles(List<String> files) throws IOException,
			MalformedMessageException, ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new QueueBlacklistFilesMessage(files));
		verifyServerResponse(response);
	}
	
	public void queueUnblacklistFiles(List<String> files) throws IOException,
			MalformedMessageException, ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new QueueUnblacklistFilesMessage(files));
		verifyServerResponse(response);
	}

	public void updateSynchronizationTrigger(String triggerName,
			String cronExpression) throws IOException,
			MalformedMessageException, ParseException,
			ServerErrorResponseException {
		Message response = getClientSession().sendRequest(
				new SynchronizeIndexUpdateTriggerMessage(triggerName,
						cronExpression, getIndexSynchronizationJobDetail()));
		verifyServerResponse(response);
	}

	protected void verifyServerResponse(Message response)
			throws ServerErrorResponseException {
		if (response instanceof FailureMessage) {
			throw new ServerErrorResponseException(response);
		}
	}

}
