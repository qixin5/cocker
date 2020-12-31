package labrador.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;

public class JavaCodeAnalyzer extends Analyzer implements CompassConfigurable {
	private static final long serialVersionUID = 3190263424730024007L;
	private Lexicon lexicon;

	public JavaCodeAnalyzer() {
		this(null);
	}

	public JavaCodeAnalyzer(Lexicon lexicon) {
		setLexicon(lexicon);
	}

	public TokenStream tokenStream(String fieldName, Reader reader) {
		JavaCodeSubwordTokenizer tokenStream = new JavaCodeSubwordTokenizer(
				reader, getLexicon());
		TokenStream result = new LowerCaseFilter(tokenStream);
		result = new StopFilter(result, getLexicon().getStopwords());
		return result;
	}

	private static final class SavedStreams {
		JavaCodeSubwordTokenizer tokenStream;
		TokenStream filteredTokenStream;
	}

	public TokenStream reusableTokenStream(String fieldName, Reader reader)
			throws IOException {
		SavedStreams streams = (SavedStreams) getPreviousTokenStream();
		if (streams == null) {
			streams = new SavedStreams();
			setPreviousTokenStream(streams);
			streams.tokenStream = new JavaCodeSubwordTokenizer(reader,
					getLexicon());
			streams.filteredTokenStream = new LowerCaseFilter(
					streams.tokenStream);
			streams.filteredTokenStream = new StopFilter(
					streams.filteredTokenStream, getLexicon().getStopwords());
		} else {
			streams.tokenStream.reset(reader);
		}

		return streams.filteredTokenStream;
	}

	public Lexicon getLexicon() {
		return lexicon;
	}

	public void setLexicon(Lexicon lexicon) {
		this.lexicon = lexicon;
	}

	public void setLexiconResourceRoot(String resourceRoot) {
		if (getLexicon() == null)
			setLexicon(new BasicLexicon());
		getLexicon().setResourceRoot(resourceRoot);
	}

	public void setThreshold(String val) {
		System.out.println(val);
	}

	public void configure(CompassSettings settings) throws CompassException {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(settings
					.getSetting("propertiesFile"))));
			setLexiconResourceRoot(properties.getProperty("environment.resources"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
