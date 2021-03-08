/********************************************************************************/
/*										*/
/*		JavaCodePatternTokenizer.java					*/
/*										*/
/*	Tokenizer for java code patterns					*/
/*										*/
/********************************************************************************/
/*	Copyright 2010 Brown University -- Jarrell Travis Webb		      */
/*	Copyright 2015 Brown University -- Steven P. Reiss		      */
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

import java.io.IOException;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.brown.cs.ivy.file.IvyLog;

import java.util.List;


class AnalysisJavaTokenizer extends Tokenizer implements AnalysisConstants {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private List<PatternToken>		token_list;
private int				token_index;
private PatternTokenizer		our_tokenizer;
private CharTermAttribute		term_attr;
private PositionIncrementAttribute	position_attr;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

AnalysisJavaTokenizer(String field)
{
   term_attr = addAttribute(CharTermAttribute.class);
   position_attr = addAttribute(PositionIncrementAttribute.class);

   our_tokenizer = Factory.getAnalysisType().createTokenizer();
}



/********************************************************************************/
/*										*/
/*	Setup token stream							*/
/*										*/
/********************************************************************************/

@Override
public void reset() throws IOException
{
   super.reset();

   token_list = null;
   token_index = 0;

   //====================
   //Code below does the following for indexing:
   //1. Obtain the analysis type.
   //2. Obtain the list of AST nodes (corresponding to the code fragment to index) from the code reader. See SearchContext.java (methods add*ToIndex) for how to get the AST nodes. Method parseIntoASTNodes is defined in AnalysisConstants.java.
   //3. Use the tokenizer associated with the analysis type to obtain the index tokens. Note the getTokens method is actually defined in AFG.java (and its descendents).
   //====================
   try {
      AnalysisType anal_type = Factory.getAnalysisType(); //E.g., KGRAM3WORDMD.
      List<ASTNode> node_list = anal_type.parseIntoASTNodes(input); //input is a field of Lucene's Tokenizer. Its type is Reader.
      token_list = our_tokenizer.getTokens(node_list);
    }
   catch (IOException e) { }
   catch (Throwable t) {
      IvyLog.logE("ANALYSIS","Problem parsing file",t);
    }
}

    

/********************************************************************************/
/*										*/
/*	Token access methods							*/
/*										*/
/********************************************************************************/

@Override public final boolean incrementToken() throws IOException
{
   if (token_list == null) return false;
   if (token_index >= token_list.size()) return false;

   int prevpos = -1;
   if (token_index > 0) prevpos = token_list.get(token_index-1).getPosition();
   PatternToken tok = token_list.get(token_index++);
   term_attr.setEmpty();
   term_attr.append(tok.getText());
   
   position_attr.setPositionIncrement(tok.getPosition() - prevpos);

   return true;
}


@Override public void end() throws IOException
{
    //super.end(); //SHOULD THIS BE CALLED? (See org.apache.lucene.analysis)
   token_list = null;
   token_index = 0;
}








}	// end of class AnalysisPatternTokenizer




/* end of JavaCodePatternTokenizer.java */
