package labrador.server.operations;

import java.util.LinkedList;
import java.util.List;

import pandorasbox.simpleclientserver.server.operations.BasicOperation;
import pandorasbox.simpleclientserver.server.operations.Operation;

/**
 * This {@link Operation} performs some task on a list of files.
 * 
 * @author jtwebb
 * 
 */
public class FilesOperation extends BasicOperation {
	private static final long serialVersionUID = -285272630306770187L;

	private List<String> files;

	/**
	 * Creates a new {@link FilesOperation} with no files specified on which to
	 * operate.
	 */
	public FilesOperation() {
		this(new LinkedList<String>());
	}

	/**
	 * Creates a new {@link FilesOperation} with the specified files on which to
	 * operate.
	 * 
	 * @param files
	 *            The list of filenames of the files on which to operate.
	 */
	public FilesOperation(List<String> files) {
		super("Operate of files", "Files operation");
		setFiles(files);
	}

	/**
	 * Returns the list of filenames of the files on which to operate.
	 * 
	 * @return the list of filenames of the files on which to operate.
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * Sets the list of filenames of the files on which to operate.
	 * 
	 * @param files
	 *            The list of filenames of the files on which to operate.
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}

}
