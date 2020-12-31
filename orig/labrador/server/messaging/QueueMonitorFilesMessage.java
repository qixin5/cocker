package labrador.server.messaging;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import pandorasbox.simpleclientserver.messaging.BasicMessage;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This {@link Message} tells the {@link Server} to monitor the specified files.
 * 
 * @author jtwebb
 * 
 */
public class QueueMonitorFilesMessage extends BasicMessage implements
		LabradorMessage, Serializable {
	private static final long serialVersionUID = -285272630306770187L;

	private List<String> files;

	/**
	 * Creates a new {@link QueueMonitorFilesMessage} with no files specified for
	 * monitoring.
	 */
	public QueueMonitorFilesMessage() {
		this(new LinkedList<String>());
	}

	/**
	 * Creates a new {@link QueueMonitorFilesMessage} with the specified files for
	 * monitoring.
	 * 
	 * @param files
	 *            The list of filenames of the files to monitor.
	 */
	public QueueMonitorFilesMessage(List<String> files) {
		super("Monitor files message");
		setFiles(files);
	}

	/**
	 * Returns the list of filenames of the files to monitor.
	 * 
	 * @return the list of filenames of the files to monitor.
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * Sets the list of filenames of the files to monitor.
	 * 
	 * @param files
	 *            The list of filenames of the files to monitor.
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}

}
