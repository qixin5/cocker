/********************************************************************************/
/*										*/
/*		AFG.java			                                */
/*										*/
/*     Produce kgrams of a given size from a list of tokens			*/
/*										*/
/********************************************************************************/
/*	Copyright 2015 Brown University -- Qi Xin			      */
/*********************************************************************************
 *  Copyright 2015 Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/

/* RCS: $Header$ */


/*********************************************************************************
 *
 * $Log$
 *
 ********************************************************************************/

package edu.brown.cs.cocker.analysis;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import edu.brown.cs.cocker.util.*;


public class AFG implements AnalysisConstants, AnalysisConstants.PatternTokenizer {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected int k_value;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public AFG(Object k0)
{
   k_value = 5;
   if (k0 != null && k0 instanceof Number) {
      int k = ((Number) k0).intValue();
      if (k > 0) k_value = k;
   }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public List<PatternToken> getTokens(ASTNode node) {
    AnalysisCodeTokenGenerator ctgen = new AnalysisCodeTokenGenerator();
    return getTokens(node, ctgen);
}
    
protected List<PatternToken> getTokens(ASTNode node, AnalysisCodeTokenGenerator ctgen) {
    List<CodeToken> ct_list = ctgen.getCTs(node, -1);
    return getKGramsNA(ct_list, k_value);
}

@Override public List<PatternToken> getTokens(List<ASTNode> node_list)
{
    AnalysisCodeTokenGenerator ctgen = new AnalysisCodeTokenGenerator();
    return getTokens(node_list, ctgen);
}
    
protected List<PatternToken> getTokens(List<ASTNode> node_list, AnalysisCodeTokenGenerator ctgen) {
    List<CodeToken> ct_list = new ArrayList<CodeToken>();
    for (ASTNode node : node_list) {
	List<CodeToken> ct_list0 = ctgen.getCTs(node, -1);
	for (CodeToken ct0 : ct_list0) { ct_list.add(ct0); }
    }
    return getKGramsNA(ct_list, k_value);
}

@Override public List<PatternToken> getTokens(ASTNode node, String data)
{
    AnalysisCodeTokenGenerator ctgen = new AnalysisCodeTokenGenerator();
    return getTokens(node, data, ctgen);
}
    
/* Used for query search. */
protected List<PatternToken> getTokens(ASTNode node, String data, AnalysisCodeTokenGenerator ctgen) {

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


    //Generate kgrams for each node. List them all.
    List<PatternToken> kgram_list_final = new ArrayList<PatternToken>();
    int pos = 0;     //The reset position
    for (QueryNode qnode : qnode_list) {
	if (qnode.isNested()) { continue; }
	ASTNode qnode_node = qnode.getNode();
	List<CodeToken> ct_list0 = ctgen.getQueryCTs(qnode_node, qnode.getProp());
	List<PatternToken> kgram_list = getKGramsNA(ct_list0, k_value);
	for (PatternToken kgram : kgram_list) {
	    kgram.setPosition(pos); 	    //Reset the position
	    pos += 1;
	    kgram_list_final.add(kgram);
	}
    }

    return kgram_list_final;
}
    
@Override public List<PatternToken> getTokens(String cnts)
{
    //DO NOT HANDLE THIS!!!
    //(Should only be overwritten by AnalysisCodeGenerator and its descendants.)
   return new ArrayList<PatternToken>();
}


static List<PatternToken> getKGrams(List<CodeToken> ct_list,int k)
{
   return getKGrams(ct_list,0,k);
}


protected static List<PatternToken> getKGrams(List<CodeToken> ct_list,int start_pos,int k)
{
   List<PatternToken> kgrams_list = new ArrayList<PatternToken>();
   int ct_list_size = ct_list.size();

   if (ct_list_size <= k) {
      String s = "";
      int prop = (ct_list_size == 0) ? -1 : ct_list.get(0).getProp();
      for (CodeToken ct : ct_list) {
	  s += ct.getText();
	  //kgram's prop should be identical to each of its token's prop
	  if (ct.getProp() != prop) { prop = -1; }
      }
      KGram kgram = new KGram(s, start_pos, k);
      kgram.setProp(prop);
      kgrams_list.add(kgram);
   }

   else {
      for (int i = 0; i <= ct_list_size - k; i++) {
	 String s = "";
	 int prop = ct_list.get(i).getProp();
	 for (int j = i; j < i + k; j++) {
	     CodeToken ct = ct_list.get(j);
	     s += ct.getText();
	     //kgram's prop should be identical to each of its token's prop
	     if (ct.getProp() != prop) { prop = -1; }
	 }
	 KGram kgram = new KGram(s, start_pos+i, k);
	 kgram.setProp(prop);
	 kgrams_list.add(kgram);
       }
    }

   return kgrams_list;
}

static List<PatternToken> getKGramsNA(List<CodeToken> ct_list, int k) {
    List<PatternToken> kgrams_list = new ArrayList<PatternToken>();
    int ct_list_size = ct_list.size();
    int kgram_pos = 0;
    
    int i=0, j=0;
    while (j<ct_list_size) {
	CodeToken ct = ct_list.get(j);
	String ct_str = ct.getText();
	if (ct_str.equals("{") || ct_str.equals("}") || ct_str.equals(";")) {
	    if (i < j) {
		//Collect the tokens indexed from i to j
		List<CodeToken> ct_list0 = new ArrayList<CodeToken>();
		for (int x=i; x<j; x++) {
		    ct_list0.add(ct_list.get(x));
		}
		//Generate kgrams, the recorded positions start with kgram_pos
		List<PatternToken> pt_list0 = getKGrams(ct_list0, kgram_pos, k);
		int pt_list0_size = pt_list0.size();
		kgram_pos += pt_list0_size;
		
		//Add kgrams to kgrams_list
		for (int x=0; x<pt_list0_size; x++) {
		    PatternToken pt = pt_list0.get(x);
		    kgrams_list.add(pt);
		}
	    }
	    
	    i = j + 1;
	}
	j += 1;
    }
    
    //The last part may need to be collected
    if (i < ct_list_size) {
	List<CodeToken> ct_list0 = new ArrayList<CodeToken>();
	for (int x=i; x<ct_list_size; x++) {
	    ct_list0.add(ct_list.get(x));
	}
	List<PatternToken> pt_list0 = getKGrams(ct_list0, kgram_pos, k);
	for(PatternToken pt : pt_list0){
	    kgrams_list.add(pt);
	}
    }
    
    return kgrams_list;
}
    
    

/********************************************************************************/
/*										*/
/*	Holder of a pattern/position						*/
/*										*/
/********************************************************************************/

static class KGram implements PatternToken {

   private String kgram_text;
   private int	  kgram_pos;
   private int    prop;

   KGram(String kgram,int pos,int k) {
      kgram_text = kgram;
      kgram_pos = pos;
      prop = -1;
    }

   @Override public String getText() {
      return kgram_text;
    }

   @Override public int getPosition() {
      return kgram_pos;
    }

   @Override public String toString() {
      return kgram_text + "@" + kgram_pos;
    }

   @Override public void setPosition(int pos) {
       kgram_pos = pos;
   }

   @Override public int getProp() {
       return prop;
   }

   @Override public void setProp(int prop) {
       this.prop = prop;
   }
    
} // end of inner class KGram



} // end of class AFG




/* end of AFG.java */
