package labrador.server;

import labrador.engine.Engine;
import pandorasbox.scheduling.server.SchedulingServer;
import pandorasbox.simpleclientserver.client.Client;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.server.HandleRequestCallback;
import pandorasbox.simpleclientserver.server.Server;
import pandorasbox.simpleclientserver.server.operations.HandleOperationCallback;

public class LabradorServer extends SchedulingServer {

	private Engine engine;

	/**
	 * Creates a new {@link LabradorServer}.
	 */
	public LabradorServer() {
		super(new LabradorHandleRequestCallback(),
				new LabradorHandleOperationCallback());
	}

	/**
	 * Create a new {@link LabradorServer} with the specified information.
	 * 
	 * @param requestCallback
	 *            The callback that will used to handle the exchange of
	 *            {@link Message}s between {@link Client} and {@link Server}.
	 * @param operationsCallback
	 *            The callback that will used to handle the operations for the
	 *            {@link Server}.
	 */
	public LabradorServer(HandleRequestCallback requestCallback,
			HandleOperationCallback operationsCallback) {
		super(requestCallback, operationsCallback);
	}

	/**
	 * Creates a new {@link LabradorServer} with the specified information.
	 * 
	 * @param port
	 *            The port to bind the to which to bind the {@link Server}.
	 */
	public LabradorServer(int port) {
		super(port, new LabradorHandleRequestCallback(),
				new LabradorHandleOperationCallback());
	}

	/**
	 * Creates a new {@link LabradorServer} with the specified information.
	 * 
	 * @param port
	 *            The port to bind the to which to bind the {@link Server}.
	 * @param requestCallback
	 *            The callback that will used to handle the exchange of
	 *            {@link Message}s between {@link Client} and {@link Server}.
	 * @param operationsCallback
	 *            The callback that will used to handle the operations for the
	 *            {@link Server}.
	 */
	public LabradorServer(int port, HandleRequestCallback requestCallback,
			HandleOperationCallback operationsCallback) {
		super(port, requestCallback, operationsCallback);
	}

	/**
	 * Creates a new {@link LabradorServer} with the specified information.
	 * 
	 * @param port
	 *            The port to bind the to which to bind the {@link Server}.
	 * @param threadPoolSize
	 *            The number of threads to put in the thread pool.
	 */
	public LabradorServer(int port, int threadPoolSize) {
		super(port, threadPoolSize, new LabradorHandleRequestCallback(),
				new LabradorHandleOperationCallback());
	}

	/**
	 * Creates a new {@link LabradorServer} with the specified information.
	 * 
	 * @param port
	 *            The port to bind the to which to bind the {@link Server}.
	 * @param threadPoolSize
	 *            The number of threads to put in the thread pool.
	 * @param requestCallback
	 *            The callback that will used to handle the exchange of
	 *            {@link Message}s between {@link Client} and {@link Server}.
	 * @param operationsCallback
	 *            The callback that will used to handle the operations for the
	 *            {@link Server}.
	 */
	public LabradorServer(int port, int threadPoolSize,
			HandleRequestCallback requestCallback,
			HandleOperationCallback operationsCallback) {
		super(port, threadPoolSize, requestCallback, operationsCallback);
	}

	/**
	 * Sets the {@link Engine} this {@link Server} will use for execution.
	 * 
	 * @param engine
	 *            The {@link Engine} this {@link Server} will use for execution.
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/**
	 * Returns The {@link Engine} this {@link Server} will use for execution.
	 * 
	 * @return The {@link Engine} this {@link Server} will use for execution.
	 */
	public Engine getEngine() {
		return engine;
	}
}
