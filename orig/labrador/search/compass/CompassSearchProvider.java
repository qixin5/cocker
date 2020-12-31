package labrador.search.compass;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import labrador.analysis.Result;
import labrador.analysis.ResultImpl;
import labrador.engine.Engine;
import labrador.search.SearchProviderException;
import labrador.search.SearchProvider;
import labrador.search.SearchContext;

import org.compass.core.Compass;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;

/**
 * This class implements {@link SearchProvider} functionality with
 * {@link Compass}. It is meant to be used by {@link Engine}s to perform
 * searching and indexing related functions.
 * <p>
 * Many search related functions must be executed in a transactional context via
 * {@link CompassSearchContext}.
 * 
 * @author jtwebb
 * 
 */
public class CompassSearchProvider implements SearchProvider {

	// private static Log log = LogFactory.getLog(CompassSearchProvider.class);

	private Compass compass;
	private int max_aggregate_results;

	/**
	 * Creates a new {@link CompassSearchProvider} with the default settings.
	 */
	public CompassSearchProvider() {
		setMaxAggregateResults(MAX_AGGREGATE_RESULTS_ALL);
	}

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
	public void createIndex() {
		getCompass().getSearchEngineIndexManager().createIndex();
	}

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
	public void deleteIndex() {
		getCompass().getSearchEngineIndexManager().deleteIndex();
	}

	/**
	 * Returns the {@link Compass} object this {@link SearchProvider} uses to
	 * perform its indexing and search operations.
	 * 
	 * @return the {@link Compass} object this {@link SearchProvider} uses to
	 *         perform its indexing and search operations.
	 */
	public Compass getCompass() {
		return compass;
	}

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
	public int getMaxAggregateResults() {
		return max_aggregate_results;
	}

	/**
	 * Returns a new and open {@link SearchContext} for performing multiple
	 * transactional operations.
	 * 
	 * @return a new and open {@link SearchContext} for performing multiple
	 *         transactional operations.
	 */
	public SearchContext openContext() {
		return new CompassSearchContext(getCompass().openSession());
	}

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
	public void optimizeIndex() {
		getCompass().getSearchEngineOptimizer().optimize();
	}

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
	public List<Result> search(String searchString) {
		List<Result> result = new LinkedList<Result>();

		CompassSession compassSession = getCompass().openSession();
		CompassTransaction compassTransaction = compassSession
				.beginLocalTransaction();
		CompassHits hits = compassSession.find(searchString);

		int maxResults = getMaxAggregateResults() == MAX_AGGREGATE_RESULTS_ALL ? hits
				.getLength()
				: hits.getLength() < getMaxAggregateResults() ? hits
						.getLength() : getMaxAggregateResults();
		for (int i = 0; i < maxResults; i++) {
			Resource resource = hits.resource(i);
			result
					.add(new ResultImpl(resource.getValue("path"), hits
							.score(i)));
			// resource.getValues("path")[0], hits.score(i))); //the old way
		}

		hits.close();
		compassTransaction.rollback();
		compassSession.close();

		return result;
	}

	/**
	 * Sets the {@link Compass} object this {@link SearchProvider} uses to
	 * perform its indexing and search operations.
	 * 
	 * @param compass
	 *            The {@link Compass} object this {@link SearchProvider} uses to
	 *            perform its indexing and search operations.
	 */
	public void setCompass(Compass compass) {
		this.compass = compass;
	}

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
	public void setMaxAggregateResults(int maxAggregateResults) {
		max_aggregate_results = maxAggregateResults;
	}

}
