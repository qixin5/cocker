package edu.brown.cs.cocker.analysis;

import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;


public class AFGForSharpFixLocalSearch extends AFG
{
    private AnalysisCodeWordGenerator cwgen;
    
    public AFGForSharpFixLocalSearch(Object k0) {
	super(k0);
	cwgen = new AnalysisCodeWordGenerator(k0);
	cwgen.setFilterStopWords(false);
	cwgen.setFilterShortAndLongWords(false);
    }

    @Override public List<PatternToken> getTokens(ASTNode node) {
        return cwgen.getTokens(node);
    }

    @Override public List<PatternToken> getTokens(List<ASTNode> node_list) {
	return cwgen.getTokens(node_list);
    }
    
    @Override public List<PatternToken> getTokens(ASTNode node,String data) {
	return cwgen.getTokens(node, data);
    }

    @Override public List<PatternToken> getTokens(String cnts) {
	return new ArrayList<PatternToken>();
    }

    public List<String> getTokensInStrings(ASTNode node) {
	List<PatternToken> pts = cwgen.getTokens(node);
	List<String> tks = new ArrayList<String>();
	for (PatternToken pt : pts) { tks.add(pt.getText()); }
	return tks;
    }
}
