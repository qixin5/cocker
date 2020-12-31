package labrador.analysis;

import java.io.File;
import java.io.Serializable;

/**
 * Provides a simple implementation of a {@link Result}. Stores the information
 * necessary to present a result to users.
 * 
 * @author jtwebb
 * 
 */
public class ResultImpl implements Result, Serializable {

	private static final long serialVersionUID = 7624892582548683811L;

	private String file_path;
	private float score;

	/**
	 * Creates a new {@link ResultImpl} with the specified path and score.
	 * 
	 * @param filePath
	 *            The path to the {@link File} that when last index received the
	 *            associated score for the search terms.
	 * @param score
	 *            The score this result received from the search provider. The
	 *            scale is 0 to 1 where 1 is the highest and 0 is the lowest.
	 */
	public ResultImpl(String filePath, float score) {
		file_path = filePath;
		this.score = score;
	}

	/**
	 * Returns the path to the {@link File} that when last index received the
	 * associated score for the search terms.
	 * 
	 * @return the path to the {@link File} that when last index received the
	 *         associated score for the search terms.
	 */
	public String getFilePath() {
		return file_path;
	}

	/**
	 * Returns the score this result received from the search provider. The
	 * scale is 0 to 1 where 1 is the highest and 0 is the lowest.
	 * 
	 * @return the score this result received from the search provider. The
	 *         scale is 0 to 1 where 1 is the highest and 0 is the lowest.
	 */
	public float getScore() {
		return score;
	}

	/**
	 * Sets the path to the {@link File} that when last index received the
	 * associated score for the search terms.
	 * 
	 * @param filePath
	 *            The path to the {@link File} that when last index received the
	 *            associated score for the search terms.
	 */
	public void setFilePath(String filePath) {
		file_path = filePath;
	}

	/**
	 * Sets the score this result received from the search provider. The scale
	 * is 0 to 1 where 1 is the highest and 0 is the lowest.
	 * 
	 * @param score
	 *            The score this result received from the search provider. The
	 *            scale is 0 to 1 where 1 is the highest and 0 is the lowest.
	 */
	protected void setScore(float score) {
		this.score = score;
	}

	/**
	 * Compares this object with the specified object for order. Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * 
	 * The implementor must ensure sgn(x.compareTo(y)) == -sgn(y.compareTo(x))
	 * for all x and y. (This implies that x.compareTo(y) must throw an
	 * exception iff y.compareTo(x) throws an exception.)
	 * 
	 * The implementor must also ensure that the relation is transitive:
	 * (x.compareTo(y)>0 && y.compareTo(z)>0) implies x.compareTo(z)>0.
	 * 
	 * Finally, the implementor must ensure that x.compareTo(y)==0 implies that
	 * sgn(x.compareTo(z)) == sgn(y.compareTo(z)), for all z.
	 * 
	 * It is strongly recommended, but not strictly required that
	 * (x.compareTo(y)==0) == (x.equals(y)). Generally speaking, any class that
	 * implements the Comparable interface and violates this condition should
	 * clearly indicate this fact. The recommended language is
	 * "Note: this class has a natural ordering that is inconsistent with equals."
	 * 
	 * In the foregoing description, the notation sgn(expression) designates the
	 * mathematical signum function, which is defined to return one of -1, 0, or
	 * 1 according to whether the value of expression is negative, zero or
	 * positive.
	 * 
	 * @param otherResult
	 *            the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 * @throws ClassCastException
	 *             if the specified object's type prevents it from being
	 *             compared to this object.
	 */
	public int compareTo(Result otherResult) {
		float result = otherResult.getScore() - this.getScore();
		return result < 0 ? -1 : (result > 0 ? 1 : 0);
	}
}
