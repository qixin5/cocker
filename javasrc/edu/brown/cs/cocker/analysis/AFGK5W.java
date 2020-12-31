package edu.brown.cs.cocker.analysis;

import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;


public class AFGK5W extends AFG
{
    private AnalysisCodeWordGenerator cwgen;
    
    public AFGK5W(Object k0) {
	super(k0);
	cwgen = new AnalysisCodeWordGenerator(k0);
    }

    @Override public List<PatternToken> getTokens(ASTNode node) {

	AnalysisCodeTokenGenerator0 ctgen = new AnalysisCodeTokenGenerator0();
	List<PatternToken> pt_list0 = this.getTokens(node, ctgen);
	List<PatternToken> pt_list1 = cwgen.getTokens(node);
	return mergePatternTokenLists(pt_list0, pt_list1);
    }

    @Override public List<PatternToken> getTokens(List<ASTNode> node_list) {

	AnalysisCodeTokenGenerator0 ctgen = new AnalysisCodeTokenGenerator0();
	List<PatternToken> pt_list0 = this.getTokens(node_list, ctgen);
	List<PatternToken> pt_list1 = cwgen.getTokens(node_list);
	return mergePatternTokenLists(pt_list0, pt_list1);
    }
    
    @Override public List<PatternToken> getTokens(ASTNode node,String data) {

	AnalysisCodeTokenGenerator0 ctgen = new AnalysisCodeTokenGenerator0();
	List<PatternToken> pt_list0 = this.getTokens(node, data, ctgen);
	List<PatternToken> pt_list1 = cwgen.getTokens(node, data);
	return mergePatternTokenLists(pt_list0, pt_list1);
    }

    @Override public List<PatternToken> getTokens(String cnts) {
	return new ArrayList<PatternToken>();
    }

    private List<PatternToken> mergePatternTokenLists(List<PatternToken> pt_list1, List<PatternToken> pt_list2) {
	int curr_pt_pos = pt_list1.size();
	for (PatternToken pt2 : pt_list2) {
	    pt2.setPosition(curr_pt_pos); //Reset the position
	    curr_pt_pos++;
	    pt_list1.add(pt2);
	}
	return pt_list1;
    }
}
