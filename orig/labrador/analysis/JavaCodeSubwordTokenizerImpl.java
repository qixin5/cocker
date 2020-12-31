package labrador.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.lucene.analysis.Token;

public class JavaCodeSubwordTokenizerImpl {

	private BufferedReader input;
	private int offset;
	private Queue<Token> token_queue;
	private Queue<Block> block_queue;
	private Lexicon lexicon;

	public JavaCodeSubwordTokenizerImpl(Reader in) {
		this(in, null);
	}
	
	public JavaCodeSubwordTokenizerImpl(Reader in, Lexicon lexicon) {
		reset(in);
		setLexicon(lexicon);
	}
	
	public void reset(Reader in) {
		input = new BufferedReader(in);
		token_queue = new LinkedList<Token>();
		block_queue = new LinkedList<Block>();
		offset = 0;
	}
	
	public Token next() throws IOException {
		while(hasMoreText() && token_queue.isEmpty())
			refillTokenQueue();
		if(token_queue.isEmpty())
			return null;
		
		return token_queue.poll();
	}
	
	private boolean hasMoreText() throws IOException {
		String line = null;
		while(block_queue.isEmpty() && (line = input.readLine()) != null) {
			block_queue.offer(new Block(line, offset));
			offset += line.length() + 1; //+1 because bufferedreader drops the \n
		}
		return !block_queue.isEmpty();
	}
	
	private void refillTokenQueue() {
		Block line = block_queue.poll();
		
		//WE COULD JUST USE A TRIE AND IGNORE ALL THIS NONSENSE... THOUGH
		//WE WOULD HAVE LESS CONTROL IF WE DID THAT
		
		//AN INTERESTING IDEA
		//as long as it doesn't find characters keep going
		//when it finds a character it should begin constructing a token
		//for every additional character it finds it should add that to the token and test again
		//when it hits a non character it should start over
		
		//go through line character by character
		//if its a non character clear the buffer
		//it its a character a put it in the buffer and
		//	test if the newly found is in the dictionary
		//	then remove the letters from the beginning of the buffer one at a time
		//		and see if any of those are in the dictionary
		
		
		//WHAT WE ARE DOING		TODO:
		//split on word border
		//with each word recursively check to see if it contains sub words
			//this is done by generating every possible subword
			//and running those through the dictionary
		
		List<Block> results = new LinkedList<Block>();
		List<Block> words = splitOnWordBorder(line);
		for(Block word : words)//TODO: yay for n^3 loops!!!!!!!!!!!!!
			results.addAll(splitOnSubwordBorder(word));
		
		for(Block result : results) {
				token_queue.offer(new Token(result.getBlockText(), result.getBlockStart(), result.getBlockEnd()));
		}
	}
	
	private List<Block> splitOnWordBorder(Block block) {
		String[] subStrings = block.getBlockText().split("[^a-zA-Z]"); //regex is fast so we can use it to clear out some of the junk
		List<Block> result = new LinkedList<Block>();
		int subOffset = 0;
		for(String subString : subStrings) {
			result.add(new Block(subString, block.getBlockStart() + subOffset));
			subOffset += subString.length() + 1;
		}
		return result;
	}
	
	private List<Block> splitOnSubwordBorder(Block block) {
		LinkedList<Block> results = new LinkedList<Block>();
		if(block.getBlockText().length() < 2)//we arent going to bother if it has 1 or 0 letters
			return results;
		
		Block workingBlock = new Block("", 0); //should start big and crunch down and break when it finds something so it always finds the largest word?
		for(int i = block.getBlockText().length(); i > 2; i--) {
			for(int j = 0; j + i <= block.getBlockText().length(); j++) {
				String subString = block.getBlockText().substring(j, j + i);
				workingBlock.setBlockText(subString);
				if(submitToDictionaries(workingBlock)) { //if its a word
					results.add(new Block(workingBlock.getBlockText(), block.getBlockStart() + j));
					block = new Block(block.getBlockText().substring(0, j) + block.getBlockText().substring(j + i, block.getBlockText().length()), block.getBlockStart() + j);
					i = block.getBlockText().length() + 1;
					j = 0;
					break;
				}
			}
		}
		
		return results;
	}
	
	//TODO:will something ever be both an abbreviation and a word?...
	private boolean submitToDictionaries(Block block) {
		String word = block.getBlockText().toLowerCase();
		if(getLexicon().getDictionary().contains(word)) {
			block.setBlockText(word);
			return true;
		}
		else if(getLexicon().getMapping().containsKey(word)) {
			block.setBlockText(getLexicon().getMapping().get(word));
			return true;
		}
		else
			return false;
	}

	public Lexicon getLexicon() {
		return lexicon;
	}

	public void setLexicon(Lexicon lexicon) {
		this.lexicon = lexicon;
	}
	
//	public static void main(String[] args) {
//		JavaTokenizerImpl jti = new JavaTokenizerImpl(new StringReader("asdf"));
//	}
	
	private class Block {
		private String block_text;
		private int block_start;
		private int block_end;
		
		public Block(String blockText, int blockStart) {
			block_start = blockStart;
			setBlockText(blockText);
		}
		
		public void setBlockText(String blockText) {
			block_text = blockText;
			block_end = block_start + block_text.length();
		}
		public String getBlockText() { return block_text; }
		public int getBlockEnd() { return block_end; }
		public int getBlockStart() { return block_start; }
		
		@Override
		public boolean equals(Object obj) {
	        if (obj instanceof Block)
	            return this.getBlockStart() == ((Block)obj).getBlockStart() && this.getBlockText().equals(((Block)obj).getBlockText()); 
	        else
	            return false;
		}
		
		@Override
		public String toString() { return "(" + getBlockText() + "," + getBlockStart() + "," + getBlockEnd() + ")"; }
	}

}
