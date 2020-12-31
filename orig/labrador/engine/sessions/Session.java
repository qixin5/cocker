package labrador.engine.sessions;

import java.io.File;
import java.io.IOException;

import labrador.engine.EngineException;
import labrador.search.SearchProvider;
import pandorasbox.filesystemchangebroadcasting.FileSystemChangeBroadcaster;

public interface Session {
	
	public abstract void blacklistFile(File file) throws EngineException;

	public abstract boolean blacklistsFile(File file) throws EngineException;

	public abstract void commit() throws IOException, EngineException;

	public abstract FileSystemChangeBroadcaster getFscb();

	public abstract SearchProvider getSearchProvider();

	public abstract boolean isOpen();

	public abstract void monitorFile(File file) throws EngineException;

	public abstract boolean monitorsFile(File file) throws EngineException;

	public abstract void open() throws IOException, EngineException;

	public abstract void synchronizeIndex() throws EngineException;

	public abstract void unblacklistFile(File file) throws EngineException;

	public abstract void unmonitorFile(File file) throws EngineException;

	public abstract void setAutoCheckpoint(boolean autoCheckpoint);

	public abstract boolean getAutoCheckpoint();

	public abstract void setCheckpointInterval(int checkpointInterval);

	public abstract int getCheckpointInterval();
	
	public abstract void checkpoint();
	
	public abstract void rollback() throws IOException, EngineException;
	
}