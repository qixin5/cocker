package labrador.analysis;

import java.io.File;
import java.io.Serializable;

/**
 * Provides a contract for necesary information about a search result.
 * 
 * @author jtwebb
 * 
 */
public interface Result extends Comparable<Result>, Serializable {

	/**
	 * Returns the path to the {@link File} that when last index received the
	 * associated score for the search terms.
	 * 
	 * @return the path to the {@link File} that when last index received the
	 *         associated score for the search terms.
	 */
	public abstract String getFilePath();

	/**
	 * Returns the score this result received from the search provider. The
	 * scale is 0 to 1 where 1 is the highest and 0 is the lowest.
	 * 
	 * @return the score this result received from the search provider. The
	 *         scale is 0 to 1 where 1 is the highest and 0 is the lowest.
	 */
	public abstract float getScore();

}
