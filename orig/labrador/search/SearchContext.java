package labrador.search;

import java.io.File;
import java.io.IOException;

/**
 * {@link SearchContext}s are used to perform transactional operations on for
 * {@link SearchProvider}s.
 * 
 * @author jtwebb
 * 
 */
public interface SearchContext {

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
	public abstract void addFileToIndex(File file) throws IOException,
			SearchProviderException;

	/**
	 * Commits and closes this {@link SearchContext}. All operations performed
	 * will be persisted beyond this {@link SearchContext}.
	 */
	public abstract void commitContext();

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
	public abstract void removeFileFromIndex(File file) throws IOException,
			SearchProviderException;

	/**
	 * Rolls back this {@link SearchContext}. All operations performed will be
	 * dropped beyond this {@link SearchContext}. They will not be persisted.
	 */
	public abstract void rollbackContext();

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
	public abstract void updateFileInIndex(File file) throws IOException,
			SearchProviderException;

}
