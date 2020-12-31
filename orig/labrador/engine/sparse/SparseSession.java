package labrador.engine.sparse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.logging.LogFactory;

import org.apache.commons.logging.Log;

import labrador.engine.EngineException;
import labrador.engine.sessions.Session;
import labrador.search.SearchContext;
import labrador.search.SearchProvider;
import labrador.search.SearchProviderException;

import pandorasbox.persistence.Dao;
import pandorasbox.filesystemchangebroadcasting.FileChangeEvent;
import pandorasbox.filesystemchangebroadcasting.FileChangeListener;
import pandorasbox.filesystemchangebroadcasting.FileSystemChangeBroadcaster;

public class SparseSession implements Session {

	protected class MonitoredFileChangedListener implements FileChangeListener,
			Serializable {

		private static final long serialVersionUID = 4644562868613612415L;

		public void fileCreated(FileChangeEvent fileChange) {
			if (fileChange.getFile().isFile())
				addFileToIndex(fileChange.getFile());
			// System.err.println("fileCreated " + fileChange.getFile());

			if (getAutoCheckpoint())
				incrementModificationsSinceCheckpoint();
		}

		public void fileDeleted(FileChangeEvent fileChange) {
			if (fileChange.getFile().isFile())
				removeFileFromIndex(fileChange.getFile());
			// System.err.println("fileDeleted " + fileChange.getFile());

			if (getAutoCheckpoint())
				incrementModificationsSinceCheckpoint();
		}

		public void fileModified(FileChangeEvent fileChange) {
			if (fileChange.getFile().isFile())
				updateFileInIndex(fileChange.getFile());
			// System.err.println("fileModified " + fileChange.getFile());

			if (getAutoCheckpoint())
				incrementModificationsSinceCheckpoint();
		}

	}

	private static Log log = LogFactory.getLog(SparseSession.class);

	private boolean auto_checkpoint;
	private FileSystemChangeBroadcaster broadcaster;
	private int checkpoint_interval;
	private Dao<FileSystemChangeBroadcaster> fscb_dao;
	private boolean is_open;
	private FileChangeListener listener;
	private int modifications_since_checkpoint;
	private SearchContext search_context;
	private SearchProvider search_provider;

	public SparseSession(SearchProvider searchProvider,
			Dao<FileSystemChangeBroadcaster> fscbDao) {
		setListener(new MonitoredFileChangedListener());
		setSearchProvider(searchProvider);
		setFscbDao(fscbDao);
		setIsOpen(false);
		setAutoCheckpoint(false);
	}

	protected void addFileToIndex(File file) {
		try {
			getSearchContext().addFileToIndex(file);
		} catch (IOException ioe) {
			if (log.isErrorEnabled())
				log.error("Could not add file: " + file.getAbsolutePath()
						+ " to index! Reason: " + ioe);
		} catch (SearchProviderException se) {
			if (log.isErrorEnabled())
				log.error("Could not add file: " + file.getAbsolutePath()
						+ " to index! Reason: " + se);
		}
	}

	public void blacklistFile(File file) throws EngineException {
		validateSession();
		getFscb().getBlacklist().add(file);

		if (getAutoCheckpoint())
			incrementModificationsSinceCheckpoint();
	}

	public boolean blacklistsFile(File file) throws EngineException {
		validateSession();
		boolean result = getFscb().getBlacklist().contains(file);
		return result;
	}

	public void checkpoint() {
		try {
			System.out.println("CHECKPOINT");
			commit();
			modifications_since_checkpoint = 0;
			open();
		} catch (IOException ioe) {
			log.fatal("Could not checkpoint: " + ioe);
		} catch (EngineException ee) {
			log.fatal("Could not checkpoint: " + ee);
		}
	}

	public void commit() throws IOException, EngineException {
		if (isOpen()) {
			commitSearchContext();
			commitHibernate(); // TODO: we need a 2 phase commit...
			setIsOpen(false);
		}
	}

	protected void commitHibernate() throws IOException {
		getFscb().removeFileChangeListener(getListener());
		getFscbDao().setPeristable(getFscb());
		setFscb(null);
	}

	protected void commitSearchContext() {
		getSearchContext().commitContext();
		setSearchContext(null);
	}

	public boolean getAutoCheckpoint() {
		return auto_checkpoint;
	}

	public int getCheckpointInterval() {
		return checkpoint_interval;
	}

	public FileSystemChangeBroadcaster getFscb() {
		return broadcaster;
	}

	protected Dao<FileSystemChangeBroadcaster> getFscbDao() {
		return fscb_dao;
	}

	protected FileChangeListener getListener() {
		return listener;
	}

	public SearchContext getSearchContext() {
		return search_context;
	}

	public SearchProvider getSearchProvider() {
		return search_provider;
	}

	protected void incrementModificationsSinceCheckpoint() {
		modifications_since_checkpoint++;
		if (modifications_since_checkpoint > getCheckpointInterval())
			checkpoint();
	}

	public boolean isOpen() {
		return is_open;
	}

	public void monitorFile(File file) throws EngineException {
		validateSession();
		getFscb().trackFile(file);
	}

	public boolean monitorsFile(File file) throws EngineException {
		validateSession();
		boolean result = getFscb().tracksFile(file);
		return result;
	}

	public void open() throws IOException, EngineException {
		if (!isOpen()) {
			openHibernate();
			setSearchContext(getSearchProvider().openContext());
			setIsOpen(true);
		}
	}

	protected void openHibernate() {
		setFscb(getFscbDao().getTacitPersistable());
		getFscb().addFileChangeListener(getListener());
	}

	protected void removeFileFromIndex(File file) {
		try {
			getSearchContext().removeFileFromIndex(file);
		} catch (IOException ioe) {
			if (log.isErrorEnabled())
				log.error("Could not add file: " + file.getAbsolutePath()
						+ " to index! Reason: " + ioe);
		} catch (SearchProviderException se) {
			if (log.isErrorEnabled())
				log.error("Could not add file: " + file.getAbsolutePath()
						+ " to index! Reason: " + se);
		}
	}

	public void rollback() throws IOException, EngineException {
		if (isOpen()) {
			rollbackSearchContext();
			rollbackHibernate();
			setIsOpen(false);
		}
	}

	protected void rollbackHibernate() {
		getFscb().removeFileChangeListener(getListener());
		setFscb(null);
	}

	protected void rollbackSearchContext() {
		getSearchContext().rollbackContext();
		setSearchContext(null);
	}

	public void setAutoCheckpoint(boolean autoCheckpoint) {
		this.auto_checkpoint = autoCheckpoint;
	}

	public void setCheckpointInterval(int checkpointInterval) {
		checkpoint_interval = checkpointInterval;
	}

	protected void setFscb(FileSystemChangeBroadcaster braodcaster) {
		this.broadcaster = braodcaster;
	}

	protected void setFscbDao(Dao<FileSystemChangeBroadcaster> fscbDao) {
		fscb_dao = fscbDao;
	}

	protected void setIsOpen(boolean isOpen) {
		is_open = isOpen;
	}

	protected void setListener(FileChangeListener listener) {
		this.listener = listener;
	}

	public void setSearchContext(SearchContext searchContext) {
		search_context = searchContext;
	}

	protected void setSearchProvider(SearchProvider searchProvider) {
		search_provider = searchProvider;
	}

	public void synchronizeIndex() throws EngineException {
		validateSession();
		getFscb().synchronize();
	}

	public void unblacklistFile(File file) throws EngineException {
		validateSession();
		getFscb().getBlacklist().remove(file);

		if (getAutoCheckpoint())
			incrementModificationsSinceCheckpoint();
	}

	public void unmonitorFile(File file) throws EngineException {
		validateSession();
		getFscb().untrackFile(file);
	}

	protected void updateFileInIndex(File file) {
		try {
			getSearchContext().updateFileInIndex(file);
		} catch (IOException ioe) {
			if (log.isErrorEnabled())
				log.error("Could not add file: " + file.getAbsolutePath()
						+ " to index! Reason: " + ioe);
		} catch (SearchProviderException se) {
			if (log.isErrorEnabled())
				log.error("Could not add file: " + file.getAbsolutePath()
						+ " to index! Reason: " + se);
		}
	}

	protected void validateSession() throws EngineException {
		if (!isOpen())
			throw new EngineException(
					"Session is closed. Please open session before attempting to perform operations.");
	}

}
