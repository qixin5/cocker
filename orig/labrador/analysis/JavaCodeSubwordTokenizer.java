package labrador.analysis;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

public class JavaCodeSubwordTokenizer extends Tokenizer {
	private JavaCodeSubwordTokenizerImpl scanner;
	private Lexicon lexicon;

	public JavaCodeSubwordTokenizer(Reader in) {
		this(in, null);
	}
	
	public JavaCodeSubwordTokenizer(Reader in, Lexicon lexicon) {
		input = in;
		setLexicon(lexicon);
		setScanner(new JavaCodeSubwordTokenizerImpl(in, lexicon));
	}

	// private void setInput(Reader reader) { this.input = reader; } //durhh?
	// they had it so..

	@Override
	public Token next() throws IOException {
		return getScanner().next();
	}

	public Token next(Token result) throws IOException {
		return getScanner().next();
	}

	@Override
	public void reset(Reader reader) throws IOException {
		input = reader;
		reset();
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		getScanner().reset(input);
	}

	public Lexicon getLexicon() {
		return lexicon;
	}

	public void setLexicon(Lexicon lexicon) {
		this.lexicon = lexicon;
		if(getScanner() != null)
			getScanner().setLexicon(getLexicon());
	}
	
	protected void setScanner(JavaCodeSubwordTokenizerImpl scanner) {
		this.scanner = scanner;
		getScanner().setLexicon(getLexicon());
	}
	
	protected JavaCodeSubwordTokenizerImpl getScanner() {
		return scanner;
	}

	public static void main(String[] args) throws IOException {
		// JavaTokenizer jt = new JavaTokenizer(new FileReader(new
		// File("/map/aux0fred/javasrc1.6/j2se/src/share/classes/javax/swing/text/html/StyleSheet.java")));
		JavaCodeSubwordTokenizer jt = new JavaCodeSubwordTokenizer(
				new FileReader(
						new File("/u/jtwebb/TestSrc/cool.java")));
		jt.setLexicon(new BasicLexicon("src/resources"));
								//"/map/aux0fred/javasrc1.6/j2se/src/share/classes/com/sun/org/apache/xalan/internal/lib/ExsltStrings.java")));
		// JavaTokenizer jt = new JavaTokenizer(new FileReader(new
		// File("/u/jtwebb/testfile.java")));
		Token t = null;
		FileWriter fw = new FileWriter(new File("/u/jtwebb/out"));
		while ((t = jt.next()) != null) {
			fw.write(t.toString() + "\n");
		}
		fw.close();
	}
}