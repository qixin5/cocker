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

   //Indexing
   try {
      AnalysisType anal_type = Factory.getAnalysisType();
      /*
      if (anal_type.toString().startsWith("RBSTM")) {
	  String cnts = anal_type.parseIntoText(input);
	  token_list = our_tokenizer.getTokens(cnts);
      }
      else {
	  List<ASTNode> node_list = anal_type.parseIntoASTNodes(input);
	  token_list = our_tokenizer.getTokens(node_list);
      }
      */
      List<ASTNode> node_list = anal_type.parseIntoASTNodes(input); //input is a field (of type Reader) of Lucene's Tokenizer
      token_list = our_tokenizer.getTokens(node_list); /* Depends on the tokenizer. E.g., many analysis methods use AFGK5W whose method getTokens(node_list) generates two list of pattern tokens (kgrams & words) and merges them as one list. Which tokenizer is determined by the *class name* passed as the argument for an analysis method's enum structure.*/
      //==================
      /*
      String tokens_str = "";
      for (PatternToken token : token_list) {
	  tokens_str += token.getText() + " ";
      }
      System.err.println("--- Indexed Tokens ---");
      System.err.println(tokens_str);
      */
      //==================
    }
   catch (IOException e) { }
   catch (Throwable t) {
      System.err.println("Problem parsing file");
      t.printStackTrace();
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
