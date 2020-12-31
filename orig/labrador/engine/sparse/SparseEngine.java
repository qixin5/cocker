package labrador.engine.sparse;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import labrador.analysis.Result;
import labrador.engine.Engine;
import labrador.engine.EngineException;
import labrador.engine.sessions.Session;
import labrador.engine.sessions.SessionOperationCallback;
import labrador.search.SearchProviderException;
import labrador.search.SearchProvider;

import pandorasbox.filesystemchangebroadcasting.FileSystemChangeBroadcaster;
import pandorasbox.persistence.Dao;

/**
 * This is a very simplistic implementation of an execution engine for labrador.
 * This will carry out all the tasks associated with searching and indexing in a
 * consistent manner. In a very basic sense this class is transactional merge of
 * {@link FileSystemChangeBroadcaster}s and {@link SearchProvider}s. It ensures
 * that operations performed using either will be consistent with the other.
 * <p>
 * Engines have {@link Session} based operations and non-{@link Session} based
 * operations. {@link Session}s are transactional. Most operations that require
 * a {@link Session} are operations that involve multiple sub operations and
 * complex interlinking between the {@link FileSystemChangeBroadcaster} and the
 * {@link SearchProvider}. Common {@link Session} operations have helpful
 * shortcuts that hide the internal {@link Session} management; though it is
 * completely possible and in some cases preferable to perform your own session
 * management. Additionally {@link Engine}s provide helper functions for
 * executing {@link Session} based operations via callbacks. This allows
 * developers to have the {@link Engine} manage {@link Session}s for them but
 * implement their own operations.
 * 
 * @author jtwebb
 * 
 */
public class SparseEngine implements Engine {

	/**
	 * This performs an operation involving a {@link File} inside a
	 * {@link Session}. It is static so that it can be used by developers who
	 * want to extend the functionality of {@link File} based {@link Session}
	 * operations.
	 * 
	 * @author jtwebb
	 * 
	 */
	protected static abstract class BasicFileOperationCallback implements
			SessionOperationCallback {

		private File file;

		/**
		 * Creates a new {@link BasicFileOperationCallback} without specifying a
		 * {@link File}. One will need to be specified with
		 * {@link BasicFileOperationCallback#setFile(File)}.
		 */
		public BasicFileOperationCallback() {
			this(null);
		}

		/**
		 * Creates a new {@link BasicFileOperationCallback} with the specified
		 * {@link File}.
		 * 
		 * @param file
		 *            The {@link File} used by this {@link File} based
		 *            {@link Session} operation.
		 */
		public BasicFileOperationCallback(File file) {
			setFile(file);
		}

		/**
		 * Perform a {@link Session} based operation using an open and
		 * functional {@link Session}.
		 * 
		 * @param session
		 *            The {@link Session} to use for the operation.
		 * @throws IOException
		 *             This method allows for {@link IOException}s to be thrown
		 *             in case there is an error with the performed operation.
		 * @throws EngineException
		 *             This method allows for {@link EngineException}s to be
		 *             thrown in case there is an error with the performed
		 *             operation.
		 */
		public abstract void executeInSession(Session session)
				throws IOException, EngineException;

		/**
		 * Returns the {@link File} used by this {@link File} based
		 * {@link Session} operation.
		 * 
		 * @return the {@link File} used by this {@link File} based
		 *         {@link Session} operation.
		 */
		public File getFile() {
			return file;
		}

		/**
		 * Sets the {@link File} used by this {@link File} based {@link Session}
		 * operation.
		 * 
		 * @param file
		 *            The {@link File} used by this {@link File} based
		 *            {@link Session} operation.
		 */
		public void setFile(File file) {
			this.file = file;
		}

	}

	/**
	 * This class provides a shortcut for blacklisting a {@link File} inside a
	 * {@link Session}.
	 * 
	 * @author jtwebb
	 * 
	 */
	protected class BlacklistFileCallback extends BasicFileOperationCallback {

		/**
		 * Perform a {@link Session} based blacklist operation.
		 * 
		 * @param session
		 *            The {@link Session} to use for the operation.
		 * @throws IOException
		 *             This method allows for {@link IOException}s to be thrown
		 *             in case there is an error with the performed operation.
		 * @throws EngineException
		 *             This method allows for {@link EngineException}s to be
		 *             thrown in case there is an error with the performed
		 *             operation.
		 */
		public void executeInSession(Session session) throws IOException,
				EngineException {
			session.blacklistFile(getFile());
		}

	}

	/**
	 * This class provides a shortcut for monitoring a {@link File} inside a
	 * {@link Session}.
	 * 
	 * @author jtwebb
	 * 
	 */
	protected class MonitorFileCallback extends BasicFileOperationCallback {

		/**
		 * Perform a {@link Session} based monitor operation.
		 * 
		 * @param session
		 *            The {@link Session} to use for the operation.
		 * @throws IOException
		 *             This method allows for {@link IOException}s to be thrown
		 *             in case there is an error with the performed operation.
		 * @throws EngineException
		 *             This method allows for {@link EngineException}s to be
		 *             thrown in case there is an error with the performed
		 *             operation.
		 */
		public void executeInSession(Session session) throws IOException,
				EngineException {
			session.monitorFile(getFile());
		}

	}

	/**
	 * This class provides a shortcut for synchronizing the index inside a
	 * {@link Session}.
	 * 
	 * @author jtwebb
	 * 
	 */
	protected class SynchronizeIndexCallback implements
			SessionOperationCallback {

		/**
		 * Perform a {@link Session} based synchronize index operation.
		 * 
		 * @param session
		 *            The {@link Session} to use for the operation.
		 * @throws IOException
		 *             This method allows for {@link IOException}s to be thrown
		 *             in case there is an error with the performed operation.
		 * @throws EngineException
		 *             This method allows for {@link EngineException}s to be
		 *             thrown in case there is an error with the performed
		 *             operation.
		 */
		public void executeInSession(Session session) throws IOException,
				EngineException {
			session.synchronizeIndex();
		}

	}

	/**
	 * This class provides a shortcut for unblacklisting a {@link File} inside a
	 * {@link Session}.
	 * 
	 * @author jtwebb
	 * 
	 */
	protected class UnblacklistFileCallback extends BasicFileOperationCallback {

		/**
		 * Perform a {@link Session} based unblacklist operation.
		 * 
		 * @param session
		 *            The {@link Session} to use for the operation.
		 * @throws IOException
		 *             This method allows for {@link IOException}s to be thrown
		 *             in case there is an error with the performed operation.
		 * @throws EngineException
		 *             This method allows for {@link EngineException}s to be
		 *             thrown in case there is an error with the performed
		 *             operation.
		 */
		public void executeInSession(Session session) throws IOException,
				EngineException {
			session.unblacklistFile(getFile());
		}

	}

	/**
	 * This class provides a shortcut for unmonitoring a {@link File} inside a
	 * {@link Session}.
	 * 
	 * @author jtwebb
	 * 
	 */
	protected class UnmonitorFileCallback extends BasicFileOperationCallback {

		/**
		 * Perform a {@link Session} based unmonitor operation.
		 * 
		 * @param session
		 *            The {@link Session} to use for the operation.
		 * @throws IOException
		 *             This method allows for {@link IOException}s to be thrown
		 *             in case there is an error with the performed operation.
		 * @throws EngineException
		 *             This method allows for {@link EngineException}s to be
		 *             thrown in case there is an error with the performed
		 *             operation.
		 */
		public void executeInSession(Session session) throws IOException,
				EngineException {
			session.unmonitorFile(getFile());
		}

	}

	private static Log log = LogFactory.getLog(SparseEngine.class);

	private BlacklistFileCallback blacklist_file_callback;
	private pandorasbox.persistence.Dao<FileSystemChangeBroadcaster> fscb_dao;
	private MonitorFileCallback monitor_file_callback;
	private SearchProvider search_provider;
	private SynchronizeIndexCallback synchronize_index_callback;
	private UnblacklistFileCallback unblacklist_file_callback;
	private UnmonitorFileCallback unmonitor_file_callback;

	/**
	 * Creates a new {@link SparseEngine}.
	 */
	public SparseEngine() {
		setBlacklistFileCallback(new BlacklistFileCallback());
		setMonitorFileCallback(new MonitorFileCallback());
		setSynchronizeIndexCallback(new SynchronizeIndexCallback());
		setUnblacklistFileCallback(new UnblacklistFileCallback());
		setUnmonitorFileCallback(new UnmonitorFileCallback());
	}

	/**
	 * This shortcut method opens a {@link Session}, blacklists the specified
	 * {@link File}, and then commits the {@link Session}.
	 * 
	 * @param file
	 *            The {@link File} to blacklist.
	 */
	public void blacklistFile(File file) throws EngineException, IOException {
		getBlacklistFileCallback().setFile(file);
		executeOperationInSession(getBlacklistFileCallback());
		getBlacklistFileCallback().setFile(null);
	}

	/**
	 * The engine will delete all indexed information and all file tracking
	 * information that is already present. Whether or not any is present this
	 * will create a brand new index with no searching or file system
	 * information. This operation is non transactional and cannot be undone.
	 * 
	 * @throws IOException
	 *             This {@link Exception} will be thrown if some error occurs
	 *             during the creation process.
	 * @throws SearchProviderException
	 *             This {@link Exception} will be thrown if the
	 *             {@link SearchProvider} encounters an error attempting to
	 *             create indexing data.
	 */
	public void createIndex() throws IOException, SearchProviderException {
		FileSystemChangeBroadcaster fscb = getFscbDao().getTacitPersistable();
		fscb.clear();
		getSearchProvider().createIndex();
		getFscbDao().setPeristable(fscb);
	}

	/**
	 * Returns a new transactional {@link Session} ready to be opened and used.
	 * 
	 * @return a new transactional {@link Session} ready to be opened and used.
	 */
	public Session createSession() {
		return new SparseSession(getSearchProvider(), getFscbDao());
	}

	/**
	 * The engine will delete all indexed information and all file tracking
	 * information. This operation is non transactional and cannot be undone.
	 * 
	 * @throws IOException
	 *             This {@link Exception} will be thrown if some error occurs
	 *             during the delete process.
	 * @throws SearchProviderException
	 *             This {@link Exception} will be thrown if the
	 *             {@link SearchProvider} encounters an error attempting to
	 *             delete indexing data.
	 */
	public void deleteIndex() throws IOException, SearchProviderException {
		FileSystemChangeBroadcaster fscb = getFscbDao().getTacitPersistable();
		fscb.clear();
		getSearchProvider().deleteIndex();
		getFscbDao().setPeristable(fscb);
	}

	/**
	 * This is a helper method for executing {@link Session} based operations
	 * inside a transactional {@link Session}. It does the grunt work of opening
	 * and closing the {@link Session} and the associated {@link Exception}
	 * handling therin.
	 * 
	 * @param callback
	 *            The callback that executes the operation.
	 * @throws IOException
	 *             This {@link Exception} will be thrown if some error occurs
	 *             opening or closing the session or if the operation itself
	 *             through an error. See the exception message for more details.
	 * @throws EngineException
	 *             This {@link Exception} will be thrown if some error related
	 *             specifically to the {@link SearchProvider} occurs opening or
	 *             closing the session or if the operation itself through an
	 *             error. See the exception message for more details.
	 */
	public void executeOperationInSession(SessionOperationCallback callback)
			throws EngineException, IOException {
		boolean succeeded = false;
		Session session = createSession();
		try {
			session.open();
			try {
				callback.executeInSession(session);
				succeeded = true;
			} catch (EngineException ee) {
				if (log.isErrorEnabled())
					log.error("Could not perform operation. Exception: " + ee);
				throw ee;
			} catch (IOException ioe) {
				if (log.isErrorEnabled())
					log.error("Could not perform operation. Exception: " + ioe);
				throw ioe;
			}
		} catch (EngineException ee) {
			if (!session.isOpen()) {
				if (log.isErrorEnabled())
					log.error("Could not open session. Exception: " + ee);
			}
			throw ee;
		} catch (IOException ioe) {
			if (!session.isOpen()) {
				if (log.isErrorEnabled())
					log.error("Could not open session. Exception: " + ioe);
			}
			throw ioe;
		} finally {
			try {
				if (succeeded) {
					session.commit();
				} else {
					session.rollback();
				}
			} catch (EngineException ee) {
				if (log.isErrorEnabled())
					log.error("Could not close session. Exception: " + ee);
				throw ee;
			} catch (IOException ioe) {
				if (log.isErrorEnabled())
					log.error("Could not close session. Exception: " + ioe);
				throw ioe;
			}
		}
	}

	/**
	 * Returns the {@link BlacklistFileCallback} used to help with blacklist
	 * file operations.
	 * 
	 * @return the {@link BlacklistFileCallback} used to help with blacklist
	 *         file operations.
	 */
	protected BlacklistFileCallback getBlacklistFileCallback() {
		return blacklist_file_callback;
	}

	/**
	 * Returns the {@link Dao} that this {@link Engine} will use to access its
	 * {@link FileSystemChangeBroadcaster} for operations.
	 * 
	 * @return the {@link Dao} that this {@link Engine} will use to access its
	 *         {@link FileSystemChangeBroadcaster} for operations.
	 */
	public Dao<FileSystemChangeBroadcaster> getFscbDao() {
		return fscb_dao;
	}

	/**
	 * Returns the {@link MonitorFileCallback} used to help with monitor file
	 * operations.
	 * 
	 * @return the {@link MonitorFileCallback} used to help with monitor file
	 *         operations.
	 */
	protected MonitorFileCallback getMonitorFileCallback() {
		return monitor_file_callback;
	}

	/**
	 * Returns the {@link SearchProvider} that this {@link Engine} will use for
	 * operations.
	 * 
	 * @return the {@link SearchProvider} that this {@link Engine} will use for
	 *         operations.
	 */
	public SearchProvider getSearchProvider() {
		return search_provider;
	}

	/**
	 * Returns the {@link SynchronizeIndexCallback} used to help with
	 * synchronize index operations.
	 * 
	 * @return the {@link SynchronizeIndexCallback} used to help with
	 *         synchronize index operations.
	 */
	protected SynchronizeIndexCallback getSynchronizeIndexCallback() {
		return synchronize_index_callback;
	}

	/**
	 * Returns the {@link UnblacklistFileCallback} used to help with unblacklist
	 * file operations.
	 * 
	 * @return the {@link UnblacklistFileCallback} used to help with unblacklist
	 *         file operations.
	 */
	protected UnblacklistFileCallback getUnblacklistFileCallback() {
		return unblacklist_file_callback;
	}

	/**
	 * Returns the {@link UnmonitorFileCallback} used to help with unmonitor
	 * file operations.
	 * 
	 * @return the {@link UnmonitorFileCallback} used to help with unmonitor
	 *         file operations.
	 */
	protected UnmonitorFileCallback getUnmonitorFileCallback() {
		return unmonitor_file_callback;
	}

	/**
	 * This helper function will transactionally monitor a {@link File} and all
	 * of its sub-files. This operation is transactional. Any failures will
	 * leave the index unchanged.
	 * 
	 * @param file
	 *            The {@link File} to monitor.
	 * 
	 * @throws IOException
	 *             This {@link Exception} will be thrown if some error occurs
	 *             during the synchronization process associated with monitoring
	 *             a {@link File}.
	 * @throws SearchProviderException
	 *             This {@link Exception} will be thrown if the
	 *             {@link SearchProvider} encounters an error attempting to
	 *             monitor and synchronize the {@link File}.
	 */
	public void monitorFile(File file) throws EngineException, IOException {
		getMonitorFileCallback().setFile(file);
		executeOperationInSession(getMonitorFileCallback());
		getMonitorFileCallback().setFile(null);
	}

	/**
	 * The engine will optimize all indexed information for faster searching and
	 * indexing. This operation is non transactional and cannot be undone.
	 * 
	 * @throws IOException
	 *             This {@link Exception} will be thrown if some error occurs
	 *             during the optimization process.
	 * @throws SearchProviderException
	 *             This {@link Exception} will be thrown if the
	 *             {@link SearchProvider} encounters an error attempting to
	 *             optimize the indexing data.
	 */
	public void optimizeIndex() throws IOException, SearchProviderException {
		getSearchProvider().optimizeIndex();
	}

	/**
	 * This is a non {@link Session} based operation that is read-only. It
	 * returns a {@link List} of {@link Result}s based on searching the index
	 * using the specified search {@link String}.
	 * 
	 * @param searchString
	 *            The search {@link String} with which to search the index.
	 * @return a {@link List} of {@link Result}s based on searching the index
	 *         using the specified search {@link String}.
	 * @throws IOException
	 *             This {@link Exception} will be thrown when
	 * @throws SearchProviderException
	 */
	public List<Result> search(String searchString) throws IOException,
			SearchProviderException {
		return getSearchProvider().search(searchString);
	}

	protected void setBlacklistFileCallback(
			BlacklistFileCallback blacklistFileCallback) {
		blacklist_file_callback = blacklistFileCallback;
	}

	/**
	 * Sets the {@link Dao} that this {@link Engine} will use to access its
	 * {@link FileSystemChangeBroadcaster} for operations.
	 * 
	 * @param fscbDao
	 *            The {@link Dao} that this {@link Engine} will use to access
	 *            its {@link FileSystemChangeBroadcaster} for operations.
	 */
	public void setFscbDao(Dao<FileSystemChangeBroadcaster> fscbDao) {
		fscb_dao = fscbDao;
	}

	/**
	 * Sets the {@link MonitorFileCallback} used to help with monitor file
	 * operations.
	 * 
	 * @param unmonitorFileCallback
	 *            The {@link MonitorFileCallback} used to help with monitor file
	 *            operations.
	 */
	protected void setMonitorFileCallback(
			MonitorFileCallback monitorFileCallback) {
		monitor_file_callback = monitorFileCallback;
	}

	/**
	 * Sets the {@link SearchProvider} that this {@link Engine} will use for
	 * operations.
	 * 
	 * @param searchProvider
	 *            The {@link SearchProvider} that this {@link Engine} will use
	 *            for operations.
	 */
	public void setSearchProvider(SearchProvider searchProvider) {
		search_provider = searchProvider;
	}

	/**
	 * Sets the {@link SynchronizeIndexCallback} used to help with synchronize
	 * index operations.
	 * 
	 * @param synchronizeIndexCallback
	 *            The {@link SynchronizeIndexCallback} used to help with
	 *            synchronize index operations.
	 */
	protected void setSynchronizeIndexCallback(
			SynchronizeIndexCallback synchronizeIndexCallback) {
		synchronize_index_callback = synchronizeIndexCallback;
	}

	/**
	 * Sets the {@link UnblacklistFileCallback} used to help with unblacklist
	 * file operations.
	 * 
	 * @param unmonitorFileCallback
	 *            The {@link UnblacklistFileCallback} used to help with
	 *            unblacklist file operations.
	 */
	protected void setUnblacklistFileCallback(
			UnblacklistFileCallback unblacklistFileCallback) {
		unblacklist_file_callback = unblacklistFileCallback;
	}

	/**
	 * Sets the {@link UnmonitorFileCallback} used to help with unmonitor file
	 * operations.
	 * 
	 * @param unmonitorFileCallback
	 *            The {@link UnmonitorFileCallback} used to help with unmonitor
	 *            file operations.
	 */
	protected void setUnmonitorFileCallback(
			UnmonitorFileCallback unmonitorFileCallback) {
		unmonitor_file_callback = unmonitorFileCallback;
	}

	/**
	 * This helper function will synchronize the remembered file system against
	 * the real and mirror any changes into the index. This operation is
	 * transactional. Any failures will leave the index unchanged.
	 * 
	 * @throws IOException
	 *             This {@link Exception} will be thrown if some error occurs
	 *             during the delete process.
	 * @throws SearchProviderException
	 *             This {@link Exception} will be thrown if the
	 *             {@link SearchProvider} encounters an error attempting to
	 *             delete indexing data.
	 */
	public void synchronizeIndex() throws EngineException, IOException {
		executeOperationInSession(getSynchronizeIndexCallback());
	}

	/**
	 * This helper function will transactionally stop blacklisting a
	 * {@link File}. This operation is transactional. Any failures will leave
	 * the index unchanged.
	 * 
	 * @param file
	 *            The {@link File} to stop blacklisting.
	 * 
	 * @throws IOException
	 *             This {@link Exception} will be thrown if some error occurs
	 *             during the unblacklisting process.
	 * @throws SearchProviderException
	 *             This {@link Exception} will be thrown if the
	 *             {@link SearchProvider} encounters an error attempting to
	 *             unblacklist the {@link File}.
	 */
	public void unblacklistFile(File file) throws EngineException, IOException {
		getUnblacklistFileCallback().setFile(file);
		executeOperationInSession(getUnblacklistFileCallback());
		getUnblacklistFileCallback().setFile(null);
	}

	/**
	 * This helper function will transactionally stop monitoring a {@link File}
	 * and all of its sub-{@link File}s. This operation is transactional. Any
	 * failures will leave the index unchanged.
	 * 
	 * @param file
	 *            The {@link File} to stop monitoring.
	 * 
	 * @throws IOException
	 *             This {@link Exception} will be thrown if some error occurs
	 *             during the synchronization process associated with
	 *             unmonitoring a {@link File}.
	 * @throws SearchProviderException
	 *             This {@link Exception} will be thrown if the
	 *             {@link SearchProvider} encounters an error attempting to
	 *             unmonitor the {@link File} and synchronize the index.
	 */
	public void unmonitorFile(File file) throws EngineException, IOException {
		getUnmonitorFileCallback().setFile(file);
		executeOperationInSession(getUnmonitorFileCallback());
		getUnmonitorFileCallback().setFile(null);
	}

}
