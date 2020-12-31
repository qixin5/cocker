package labrador.server.messaging;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import pandorasbox.simpleclientserver.messaging.BasicMessage;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This {@link Message} tells the {@link Server} to unmonitor the specified
 * files.
 * 
 * @author jtwebb
 * 
 */
public class QueueUnmonitorFilesMessage extends BasicMessage implements
		LabradorMessage, Serializable {
	private static final long serialVersionUID = -285272630306770187L;

	private List<String> files;

	/**
	 * Creates a new {@link QueueUnmonitorFilesMessage} with no files specified for
	 * unmonitoring.
	 */
	public QueueUnmonitorFilesMessage() {
		this(new LinkedList<String>());
	}

	/**
	 * Creates a new {@link QueueUnmonitorFilesMessage} with the specified files for
	 * unmonitoring.
	 * 
	 * @param files
	 *            The list of filenames of the files to unmonitor.
	 */
	public QueueUnmonitorFilesMessage(List<String> files) {
		super("Unmonitor files message");
		setFiles(files);
	}

	/**
	 * Returns the list of filenames of the files to unmonitor.
	 * 
	 * @return the list of filenames of the files to unmonitor.
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * Sets the list of filenames of the files to unmonitor.
	 * 
	 * @param files
	 *            The list of filenames of the files to unmonitor.
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}

}
