package edu.brown.cs.cocker.analysis;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import edu.brown.cs.cocker.analysis.rebase.*;
import edu.brown.cs.cocker.util.*;


public class AnalysisCodeWordGenerator implements AnalysisConstants, AnalysisConstants.PatternTokenizer {

    protected int k_value; //Currently, it's never used.
    protected static RebaseWordStemmer stemmer;

    static { stemmer = new RebaseWordStemmer(); }
    
    public AnalysisCodeWordGenerator(Object k0) {
	k_value = 5;
	if (k0 != null && k0 instanceof Number) {
	    int k = ((Number) k0).intValue();
	    if (k > 0) k_value = k;
	}
    }

    @Override public List<PatternToken> getTokens(ASTNode node) {
	CodeTokenGenerator ctgen = new CodeTokenGenerator();
	return getTokensHelper(ctgen.getCTs(node, -1));
    }

    @Override public List<PatternToken> getTokens(List<ASTNode> node_list) {
	CodeTokenGenerator ctgen = new CodeTokenGenerator();
	List<CodeToken> ct_list = new ArrayList<CodeToken>();
	for (ASTNode node : node_list) {
	    List<CodeToken> ct_list0 = ctgen.getCTs(node, -1);
	    for (CodeToken ct0 : ct_list0) { ct_list.add(ct0); }
	}
	return getTokensHelper(ct_list);
    }
    
    @Override public List<PatternToken> getTokens(ASTNode node, String data) {
	CodeTokenGenerator ctgen = new CodeTokenGenerator();
	if (!(node instanceof CompilationUnit)) { return getTokens(node); }
	CompilationUnit cu = (CompilationUnit) node;
	List<QueryNode> qnode_list = QueryNodeFinder.find(cu, data);
	Map<ASTNode, Integer> property_map = new HashMap<ASTNode, Integer>();
	for (QueryNode qnode : qnode_list) {
	    ASTNode qnode_node = qnode.getNode();
	    if (qnode_node != null) {
		property_map.put(qnode_node, qnode.getProp());
	    }
	}
	ctgen.setPropertyMap(property_map);

	List<CodeToken> ct_list0 = new ArrayList<CodeToken>();
	for (QueryNode qnode : qnode_list) {
	    if (qnode.isNested()) { continue; }
	    ASTNode qnode_node = qnode.getNode();
	    List<CodeToken> ct_list1 = ctgen.getCTs(qnode_node, qnode.getProp());
	    for (CodeToken ct1 : ct_list1) { ct_list0.add(ct1); }
	}
	List<PatternToken> pt_list = getTokensHelper(ct_list0);
	return pt_list;
    }
    
    @Override public List<PatternToken> getTokens(String cnts) {
	//For indexing, subclasses overwrite this.
	return new ArrayList<PatternToken>();
    }

    protected List<PatternToken> getTokensHelper(List<CodeToken> ct_list) {
	int pos = 0;
	List<PatternToken> pt_list = new ArrayList<PatternToken>();
	for (CodeToken ct : ct_list) {
	    int prop = ct.getProp();
	    String ct_text = ct.getText();
	    List<String> stemmed_words = stem(ct_text);
	    for (String stemmed_word : stemmed_words) {
		PatternToken pt = new CodeWord(stemmed_word, pos);
		pt.setProp(prop);
		pos++;
		pt_list.add(pt);
	    }
	}
	return pt_list;
    }

    /* Extract the rebase words from cnts. */
    public static List<String> stem(String cnts) {
	List<String> token_list = new ArrayList<String>();
	int size = cnts.length();
	for (int i = 0; i < size; ++i) {
	    char ch = cnts.charAt(i);
	    if (Character.isJavaIdentifierPart(ch)) {
		boolean havealpha = Character.isAlphabetic(ch);
		int start = i;
		while (Character.isJavaIdentifierPart(ch)) {
		    havealpha |= Character.isAlphabetic(ch);
		    if (++i >= size) break;
		    ch = cnts.charAt(i);
		}
		//If the token has alpha, then it is a name
		if (havealpha) {
		    List<String> token_sublist = RebaseWordFactory.getCandidateWords(stemmer, cnts, start, i-start);
		    if (token_sublist != null) {
			for (String token : token_sublist) {
			    token_list.add(token);
			}
		    }
		}
	    }
	}
	return token_list;
    }
    
    static class CodeWord implements PatternToken {

	private String text;
	private int    pos;
	private int    prop;

	CodeWord(String text, int pos) {
	    this.text = text;
	    this.pos = pos;
	    prop = -1;
	}

	@Override public String getText() {
	    return text;
	}

	@Override public int getPosition() {
	    return pos;
	}

	@Override public void setPosition(int pos) {
	    this.pos = pos;
	}
	
	@Override public String toString() {
	    return text + "@" + pos;
	}

	@Override public int getProp() {
	    return prop;
	}

	@Override public void setProp(int prop) {
	    this.prop = prop;
	}
    }
}