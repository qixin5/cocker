package labrador.search.compass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import labrador.search.SearchProvider;
import labrador.search.SearchProviderException;
import labrador.search.SearchContext;

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;

/**
 * {@link CompassSearchContext}s are used to perform transactional operations on
 * for {@link CompassSearchProvider}s.
 * 
 * @author jtwebb
 * 
 */
public class CompassSearchContext implements SearchContext {

	private CompassSession session_compass;
	private CompassTransaction transaction_compass;

	/**
	 * Creates a new {@link CompassSearchContext} from the specific
	 * {@link CompassSession}. This will being a new {@link CompassTransaction}.
	 * 
	 * @param sessionCompass
	 *            The {@link CompassSession} to use in this
	 *            {@link SearchContext}.
	 */
	public CompassSearchContext(CompassSession sessionCompass) {
		setCompassSession(sessionCompass);
		setCompassTransaction(getCompassSession().beginTransaction());
	}

	/**
	 * Adds the specified {@link File} to the index so that it can be searched
	 * against later. Different {@link SearchProvider}s will have different
	 * behaviors with regards to duplicate {@link File} additions.
	 * 
	 * @param file
	 *            The {@link File} to add to the index.
	 * @throws IOException
	 *             This {@link Exception} is thrown if there is some io related
	 *             error when attempting to add the {@link File} to the index.
	 * @throws SearchProviderException
	 *             This {@link Exception} is thrown if there is some
	 *             {@link SearchProvider} related error when attempting to add
	 *             the {@link File} to the index. Usually this means that for
	 *             whatever reason the {@link SearchProvider} threw an
	 *             {@link Exception} of its own type and this is just a wrapper.
	 */
	public void addFileToIndex(File file) throws IOException,
			SearchProviderException {
		try {
			getCompassSession().save(createResource(file));
		} catch (CompassException ce) {
			throw new SearchProviderException(ce);
		}
	}

	/**
	 * Commits and closes this {@link SearchContext}. All operations performed
	 * will be persisted beyond this {@link SearchContext}.
	 */
	public void commitContext() {
		getTransactionCompass().commit();
		getCompassSession().close();
	}

	/**
	 * Returns a {@link Resource} created from the specified file such that it
	 * can be indexed by the {@link SearchProvider} and thus searched against
	 * later.
	 * 
	 * @param file
	 *            The {@link File} from which to create the {@link Resource}.
	 * @return a {@link Resource} created from the specified file such that it
	 *         can be indexed by the {@link SearchProvider} and thus searched
	 *         against later.
	 * @throws FileNotFoundException
	 *             This {@link Exception} will be thrown if there was some
	 *             problem accessing the {@link File} when trying to create the
	 *             {@link Resource}.
	 */
	protected Resource createResource(File file) throws FileNotFoundException {
		// ResourceFactory resourceFactory = getCompass().getResourceFactory();
		// Resource result = resourceFactory.createResource("codefile");
		Resource result = getCompassSession().resourceFactory().createResource(
				"codefile");
		// result.addProperty("id", file.getAbsolutePath());
		result.addProperty("path", file.getAbsolutePath());
		result.addProperty("code", new FileReader(file));
		result.addProperty("lastmodified", file.lastModified());
		return result;
	}

	/**
	 * Returns the {@link CompassSession} to use in this {@link SearchContext}.
	 * 
	 * @return the {@link CompassSession} to use in this {@link SearchContext}.
	 */
	protected CompassSession getCompassSession() {
		return session_compass;
	}

	/**
	 * Returns the {@link CompassTransaction} that this
	 * {@link CompassSearchContext} will use to carry out its transactional
	 * operations.
	 * 
	 * @return the {@link CompassTransaction} that this
	 *         {@link CompassSearchContext} will use to carry out its
	 *         transactional operations.
	 */
	protected CompassTransaction getTransactionCompass() {
		return transaction_compass;
	}

	/**
	 * Removes the specified {@link File} from the index so that it can no
	 * longer be searched against. Attempts to remove a {@link File} that does
	 * not exist will be ignored.
	 * 
	 * @param file
	 *            The {@link File} to remove from the index.
	 * @throws IOException
	 *             This {@link Exception} is thrown if there is some io related
	 *             error when attempting to remove the {@link File} from the
	 *             index.
	 * @throws SearchProviderException
	 *             This {@link Exception} is thrown if there is some
	 *             {@link SearchProvider} related error when attempting to
	 *             remove the {@link File} from the index. Usually this means
	 *             that for whatever reason the {@link SearchProvider} threw an
	 *             {@link Exception} of its own type and this is just a wrapper.
	 */
	public void removeFileFromIndex(File file) {
		getCompassSession().delete("codefile", file.getAbsolutePath());
	}

	/**
	 * Rolls back this {@link SearchContext}. All operations performed will be
	 * dropped beyond this {@link SearchContext}. They will not be persisted.
	 */
	public void rollbackContext() {
		getTransactionCompass().rollback();
		getCompassSession().close();
	}

	/**
	 * Sets the {@link CompassSession} to use in this {@link SearchContext}.
	 * 
	 * @param session
	 *            The {@link CompassSession} to use in this
	 *            {@link SearchContext}.
	 */
	protected void setCompassSession(CompassSession session) {
		session_compass = session;
	}

	/**
	 * Sets the {@link CompassTransaction} that this
	 * {@link CompassSearchContext} will use to carry out its transactional
	 * operations.
	 * 
	 * @param transaction
	 *            The {@link CompassTransaction} that this
	 *            {@link CompassSearchContext} will use to carry out its
	 *            transactional operations.
	 */
	protected void setCompassTransaction(CompassTransaction transaction) {
		transaction_compass = transaction;
	}

	/**
	 * Updates the specified {@link File} in the index so that it can be
	 * searched against later with additional information. Different
	 * {@link SearchProvider}s will have different behaviors. In all cases
	 * attempting to update a {@link File} that is not in the index will result
	 * in the {@link File} being added to the index; however some
	 * {@link SearchProvider}s will take performance hits from attempting to
	 * update a {@link File} which was not previously added. Thus, it is best to
	 * use {@link SearchContext#addFileToIndex(File)} when it can be guaranteed
	 * that a {@link File} is not in the index.
	 * 
	 * @param file
	 *            The {@link File} to update in the index.
	 * @throws IOException
	 *             This {@link Exception} is thrown if there is some io related
	 *             error when attempting to update the {@link File} in the
	 *             index.
	 * @throws SearchProviderException
	 *             This {@link Exception} is thrown if there is some
	 *             {@link SearchProvider} related error when attempting to
	 *             update the {@link File} in the index. Usually this means that
	 *             for whatever reason the {@link SearchProvider} threw an
	 *             {@link Exception} of its own type and this is just a wrapper.
	 */
	public void updateFileInIndex(File file) throws IOException,
			SearchProviderException {
		try {
			getCompassSession().save(createResource(file));
		} catch (CompassException ce) {
			throw new SearchProviderException(ce);
		}
	}
}
