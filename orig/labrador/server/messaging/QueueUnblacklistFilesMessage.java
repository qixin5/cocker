package labrador.server.messaging;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import pandorasbox.simpleclientserver.messaging.BasicMessage;
import pandorasbox.simpleclientserver.messaging.Message;
import pandorasbox.simpleclientserver.server.Server;

/**
 * This {@link Message} tells the {@link Server} to unblacklist the specified
 * files.
 * 
 * @author jtwebb
 * 
 */
public class QueueUnblacklistFilesMessage extends BasicMessage implements
		LabradorMessage, Serializable {
	private static final long serialVersionUID = -285272630306770187L;

	private List<String> files;

	/**
	 * Creates a new {@link QueueUnblacklistFilesMessage} with no files specified for
	 * unblacklisting.
	 */
	public QueueUnblacklistFilesMessage() {
		this(new LinkedList<String>());
	}

	/**
	 * Creates a new {@link QueueUnblacklistFilesMessage} with the specified files
	 * for unblacklisting.
	 * 
	 * @param files
	 *            The list of filenames of the files to Unblacklist.
	 */
	public QueueUnblacklistFilesMessage(List<String> files) {
		super("Unblacklist files message");
		setFiles(files);
	}

	/**
	 * Returns the list of filenames of the files to Unblacklist.
	 * 
	 * @return the list of filenames of the files to Unblacklist.
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * Sets the list of filenames of the files to Unblacklist.
	 * 
	 * @param files
	 *            The list of filenames of the files to Unblacklist.
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}

}
