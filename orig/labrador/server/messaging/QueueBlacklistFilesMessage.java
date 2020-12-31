package labrador.server.messaging;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import pandorasbox.simpleclientserver.messaging.BasicMessage;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This {@link Message} tells the {@link Server} to blacklist the specified
 * files.
 * 
 * @author jtwebb
 * 
 */
public class QueueBlacklistFilesMessage extends BasicMessage implements
		LabradorMessage, Serializable {
	private static final long serialVersionUID = -285272630306770187L;

	private List<String> files;

	/**
	 * Creates a new {@link QueueBlacklistFilesMessage} with no files specified for
	 * blacklisting.
	 */
	public QueueBlacklistFilesMessage() {
		this(new LinkedList<String>());
	}

	/**
	 * Creates a new {@link QueueBlacklistFilesMessage} with the specified files for
	 * blacklisting.
	 * 
	 * @param files
	 *            The list of filenames of the files to blacklist.
	 */
	public QueueBlacklistFilesMessage(List<String> files) {
		super("Blacklist files message");
		setFiles(files);
	}

	/**
	 * Returns the list of filenames of the files to blacklist.
	 * 
	 * @return the list of filenames of the files to blacklist.
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * Sets the list of filenames of the files to blacklist.
	 * 
	 * @param files
	 *            The list of filenames of the files to blacklist.
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}

}
