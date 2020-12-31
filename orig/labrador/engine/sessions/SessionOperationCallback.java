package labrador.engine.sessions;

import java.io.IOException;

import labrador.engine.Engine;
import labrador.engine.EngineException;

/**
 * Implementing this callback will allow developers to execute transactional
 * {@link Engine} operations using {@link Session}s without much of the
 * {@link Exception} propagation and management hassle. Giving a
 * {@link SessionOperationCallback} to
 * {@link Engine#executeOperationInSession(SessionOperationCallback)} will let
 * the {@link Engine} take care of the {@link Session} management leaving the
 * developer free to worry about the logic.
 * 
 * @author jtwebb
 * 
 */
public interface SessionOperationCallback {

	/**
	 * Perform a {@link Session} based operation using an open and functional
	 * {@link Session}.
	 * 
	 * @param session
	 *            The {@link Session} to use for the operation.
	 * @throws IOException
	 *             This method allows for {@link IOException}s to be thrown in
	 *             case there is an error with the performed operation.
	 * @throws EngineException
	 *             This method allows for {@link EngineException}s to be thrown
	 *             in case there is an error with the performed operation.
	 */
	public abstract void executeInSession(Session session) throws IOException,
			EngineException;

}
