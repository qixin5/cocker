package labrador.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BasicLexicon implements Lexicon {

	public static final FileFilter DEFAULT_DICTIONARY_FILE_FILTER = new ExtensionFileFilter(
			DEFAULT_DICTIONARY_FILE_EXTENSION);
	public static final FileFilter DEFAULT_MAP_FILE_FILTER = new ExtensionFileFilter(
			DEFAULT_MAP_FILE_EXTENSION);
	public static final FileFilter DEFAULT_STOPWORDS_FILE_FILTER = new ExtensionFileFilter(
			DEFAULT_STOPWORDS_FILE_EXTENSION);

	private static class ExtensionFileFilter implements FileFilter {

		private String extension;

		public ExtensionFileFilter() {
		}

		public ExtensionFileFilter(String extension) {
			setExtension(extension);
		}

		public boolean accept(File file) {
			return file.getPath().endsWith(getExtension());
		}

		public String getExtension() {
			return extension;
		}

		public void setExtension(String extension) {
			this.extension = extension;
		}

	}

	private static final String MAP_ENTRY_SPLIT_CHAR = "=";
	private Set<String> dictionary;
	private Map<String, String> mapping;

	private Set<String> stopwords;

	public BasicLexicon() {
		this(null);
	}
	
	public BasicLexicon(String filename) {
		dictionary = new HashSet<String>();
		mapping = new HashMap<String, String>();
		stopwords = new HashSet<String>();
		if(filename != null)
			setResourceRoot(filename);
	}

	public Set<String> getDictionary() {
		return dictionary;
	}

	public Map<String, String> getMapping() {
		return mapping;
	}

	public Set<String> getStopwords() {
		return stopwords;
	}

	public void loadDictionariesFolder(File root) {
		loadDictionariesFolder(root, DEFAULT_DICTIONARY_FILE_FILTER);
	}

	public void loadDictionariesFolder(File root, FileFilter filter) {
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isDirectory())
				loadDictionariesFolder(root, filter);
			else if (filter.accept(file))
				loadDictionaryFile(file);
		}
	}

	private void loadDictionaryFile(File file) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				getDictionary().add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void loadDictionaryFiles(File[] files) {
		for (File file : files) {
			loadDictionaryFile(file);
		}
	}

	public void loadMappingFiles(File[] files) {
		for (File file : files) {
			loadMappingsFile(file);
		}
	}

	private void loadMappingsFile(File file) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				String[] entry = line.split(MAP_ENTRY_SPLIT_CHAR);
				getMapping().put(entry[0], entry[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void loadMappingsFolder(File root) {
		loadMappingsFolder(root, DEFAULT_MAP_FILE_FILTER);
	}

	public void loadMappingsFolder(File root, FileFilter filter) {
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isDirectory())
				loadMappingsFolder(root, filter);
			else if (filter.accept(file))
				loadMappingsFile(file);
		}
	}

	private void loadStopwordsFile(File file) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				getStopwords().add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void loadStopwordsFiles(File[] files) {
		for (File file : files) {
			loadStopwordsFile(file);
		}
	}

	public void loadStopwordsFolder(File root) {
		loadStopwordsFolder(root, DEFAULT_STOPWORDS_FILE_FILTER);
	}

	public void loadStopwordsFolder(File root, FileFilter filter) {
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isDirectory())
				loadStopwordsFolder(root, filter);
			else if (filter.accept(file))
				loadStopwordsFile(file);
		}
	}

	public void setResourceRoot(String filename) {
		File resourceRoot = new File(filename);
		loadDictionariesFolder(resourceRoot);
		loadMappingsFolder(resourceRoot);
		loadStopwordsFolder(resourceRoot);
	}

}
