package labrador.search;

import java.io.File;
import java.io.IOException;
import java.util.List;

import labrador.analysis.Result;
import labrador.engine.Engine;

/**
 * This interface wraps the basic functionality of indexing tools such as lucene
 * and compass making them uniform and easy to use. {@link SearchProvider}s are
 * used by {@link Engine}s to perform searching and indexing related functions.
 * All searching and indexing tools must make the basic functionality defined by
 * this interface available for use.
 * <p>
 * Many search related functions must be executed in a transactional context via
 * {@link SearchContext}.
 * 
 * @author jtwebb
 * 
 */
public interface SearchProvider {
	/**
	 * A value specifying that all results should be be returned (-1).
	 */
	public static final int MAX_AGGREGATE_RESULTS_ALL = -1;

	/**
	 * Blanks the index. Makes it a clean slate ready for new {@link File}s to
	 * be added. All traces of any {@link File}s previously added to this index
	 * are removed. If the index did not previously exist, it will be created.
	 * 
	 * @throws IOException
	 *             This {@link Exception} is thrown if there is some io related
	 *             error when attempting to clean the index.
	 * @throws SearchProviderException
	 *             This {@link Exception} is thrown if there is some
	 *             {@link SearchProvider} related error when attempting to clean
	 *             the index. Usually this means that for whatever reason the
	 *             {@link SearchProvider} threw an {@link Exception} of its own
	 *             type and this is just a wrapper.
	 */
	public abstract void createIndex() throws IOException,
			SearchProviderException;

	/**
	 * Permanently deletes the index. Removes all traces of it.
	 * 
	 * @throws IOException
	 *             This {@link Exception} is thrown if there is some io related
	 *             error when attempting to delete the index.
	 * @throws SearchProviderException
	 *             This {@link Exception} is thrown if there is some
	 *             {@link SearchProvider} related error when attempting to
	 *             delete the index. Usually this means that for whatever reason
	 *             the {@link SearchProvider} threw an {@link Exception} of its
	 *             own type and this is just a wrapper.
	 */
	public abstract void deleteIndex() throws IOException,
			SearchProviderException;

	/**
	 * Returns the maximum number of results that the
	 * {@link SearchProvider#search(String)} function will return.
	 * {@link SearchProvider#search(String)} can return fewer results, but it
	 * will not return more.
	 * 
	 * @return the maximum number of results that the
	 *         {@link SearchProvider#search(String)} function will return.
	 *         {@link SearchProvider#search(String)} can return fewer results,
	 *         but it will not return more.
	 */
	public abstract int getMaxAggregateResults();

	/**
	 * Returns a new and open {@link SearchContext} for performing multiple
	 * transactional operations.
	 * 
	 * @return a new and open {@link SearchContext} for performing multiple
	 *         transactional operations.
	 */
	public SearchContext openContext();

	/**
	 * Optimizes the index.
	 * 
	 * @throws IOException
	 *             This {@link Exception} is thrown if there is some io related
	 *             error when attempting to optimize the index.
	 * @throws SearchProviderException
	 *             This {@link Exception} is thrown if there is some
	 *             {@link SearchProvider} related error when attempting to
	 *             optimize the index. Usually this means that for whatever
	 *             reason the {@link SearchProvider} threw an {@link Exception}
	 *             of its own type and this is just a wrapper.
	 */
	public abstract void optimizeIndex() throws IOException,
			SearchProviderException;

	/**
	 * Returns a list of results based on the specified searchString (keywords).
	 * The number of results returned will be no more than the number returned
	 * by {@link SearchProvider#getMaxAggregateResults()}.
	 * 
	 * @param searchString
	 *            The keywords or search phrase that defines the query.
	 * @return a list of results based on the specified searchString (keywords).
	 *         The number of results returned will be no more than the number
	 *         returned by {@link SearchProvider#getMaxAggregateResults()}.
	 * 
	 * @throws IOException
	 *             This {@link Exception} is thrown if there is some io related
	 *             error when attempting to search the index.
	 * @throws SearchProviderException
	 *             This {@link Exception} is thrown if there is some
	 *             {@link SearchProvider} related error when attempting to
	 *             search the index. Usually this means that for whatever reason
	 *             the {@link SearchProvider} threw an {@link Exception} of its
	 *             own type and this is just a wrapper.
	 */
	public abstract List<Result> search(String searchString)
			throws IOException, SearchProviderException;

	/**
	 * Sets the maximum number of results that the
	 * {@link SearchProvider#search(String)} function will return.
	 * {@link SearchProvider#search(String)} can return fewer results, but it
	 * will not return more.
	 * 
	 * @param maxAggregateResults
	 *            The maximum number of results that the
	 *            {@link SearchProvider#search(String)} function will return.
	 *            {@link SearchProvider#search(String)} can return fewer
	 *            results, but it will not return more.
	 */
	public abstract void setMaxAggregateResults(int maxAggregateResults);

}
