package labrador.analysis;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.Set;

public interface Lexicon {

	public static final String DEFAULT_DICTIONARY_FILE_EXTENSION = ".dict";
	public static final String DEFAULT_MAP_FILE_EXTENSION = ".map";
	public static final String DEFAULT_STOPWORDS_FILE_EXTENSION = ".stop";

	public abstract Set<String> getDictionary();

	public abstract Map<String, String> getMapping();

	public abstract Set<String> getStopwords();

	public abstract void loadDictionariesFolder(File root);

	public abstract void loadDictionariesFolder(File root, FileFilter filter);

	public abstract void loadDictionaryFiles(File[] files);

	public abstract void loadMappingFiles(File[] files);

	public abstract void loadMappingsFolder(File root);

	public abstract void loadMappingsFolder(File root, FileFilter filter);

	public abstract void loadStopwordsFiles(File[] files);

	public abstract void loadStopwordsFolder(File root);

	public abstract void loadStopwordsFolder(File root, FileFilter filter);

	public abstract void setResourceRoot(String filename);

}