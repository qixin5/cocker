package labrador.engine;

import java.io.File;
import java.io.IOException;
import java.util.List;

import labrador.analysis.Result;
import labrador.engine.sessions.Session;
import labrador.engine.sessions.SessionOperationCallback;
import labrador.search.SearchProvider;
import labrador.search.SearchProviderException;
import pandorasbox.filesystemchangebroadcasting.FileSystemChangeBroadcaster;
import pandorasbox.persistence.Dao;

/**
 * Represents an execution engine for labrador. This will carry out all the
 * tasks associated with searching and indexing in a consistent manner. In a
 * very basic sense this class is transactional merge of
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
public interface Engine {

	/**
	 * This helper function will transactionally blacklist a {@link File}. This
	 * operation is transactional. Any failures will leave the index unchanged.
	 * 
	 * @param file
	 *            The {@link File} to blacklist.
	 * 
	 * @throws IOException
	 *             This {@link Exception} will be thrown if some error occurs
	 *             during the blacklisting process.
	 * @throws SearchProviderException
	 *             This {@link Exception} will be thrown if the
	 *             {@link SearchProvider} encounters an error attempting to
	 *             blacklist the {@link File}.
	 */
	public abstract void blacklistFile(File file) throws EngineException,
			IOException;

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
	public abstract void createIndex() throws IOException,
			SearchProviderException;

	/**
	 * Returns a new transactional {@link Session} ready to be opened and used.
	 * 
	 * @return a new transactional {@link Session} ready to be opened and used.
	 */
	public abstract Session createSession();

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
	public abstract void deleteIndex() throws IOException,
			SearchProviderException;

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
	public abstract void executeOperationInSession(
			SessionOperationCallback callback) throws EngineException,
			IOException;

	/**
	 * Returns the {@link Dao} that this {@link Engine} will use to access its
	 * {@link FileSystemChangeBroadcaster} for operations.
	 * 
	 * @return the {@link Dao} that this {@link Engine} will use to access its
	 *         {@link FileSystemChangeBroadcaster} for operations.
	 */
	public abstract Dao<FileSystemChangeBroadcaster> getFscbDao();

	/**
	 * Returns the {@link SearchProvider} that this {@link Engine} will use for
	 * operations.
	 * 
	 * @return the {@link SearchProvider} that this {@link Engine} will use for
	 *         operations.
	 */
	public abstract SearchProvider getSearchProvider();

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
	public abstract void monitorFile(File file) throws EngineException,
			IOException;

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
	public abstract void optimizeIndex() throws IOException,
			SearchProviderException;

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
	public abstract List<Result> search(String searchString)
			throws IOException, SearchProviderException;

	/**
	 * Sets the {@link Dao} that this {@link Engine} will use to access its
	 * {@link FileSystemChangeBroadcaster} for operations.
	 * 
	 * @param fscbDao
	 *            The {@link Dao} that this {@link Engine} will use to access
	 *            its {@link FileSystemChangeBroadcaster} for operations.
	 */
	public abstract void setFscbDao(Dao<FileSystemChangeBroadcaster> fscbDao);

	/**
	 * Sets the {@link SearchProvider} that this {@link Engine} will use for
	 * operations.
	 * 
	 * @param searchProvider
	 *            The {@link SearchProvider} that this {@link Engine} will use
	 *            for operations.
	 */
	public abstract void setSearchProvider(SearchProvider searchProvider);

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
	public abstract void synchronizeIndex() throws EngineException, IOException;

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
	public abstract void unblacklistFile(File file) throws EngineException,
			IOException;

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
	public abstract void unmonitorFile(File file) throws EngineException,
			IOException;

}