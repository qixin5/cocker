/********************************************************************************/
/*										*/
/*		AnalysisConstants.java						*/
/*										*/
/*	Constants and interfaces for analysis methods				*/
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


import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.*;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.*;

import edu.brown.cs.cocker.cocker.CockerConstants;
import edu.brown.cs.cocker.util.ResourceFinder;
import edu.brown.cs.ivy.file.IvyFile;


public interface AnalysisConstants
{

/********************************************************************************/
/*										*/
/*	Basic search information						*/
/*										*/
/********************************************************************************/


String	SEARCH_FIELD = "code";
String	DEFAULT_ANALYSIS_TYPE = "KGRAM5CFG3";
String	INDEX_PATH_NAME = "cocker-index-";
String	DATABASE_NAME_PREFIX = "cocker_";
int	DEFAULT_PORT_BASE = 10263;

enum ChunkType {
   STATEMENTS,
   METHOD,
   CLASS,
   FILE
}




public interface PatternToken {

   String getText();
   int getPosition();
   void setPosition(int pos);
   int getProp();
   void setProp(int prop);
}

interface PatternTokenizer {

   //Get kgram patterns from the code's tree structure
   List<PatternToken> getTokens(ASTNode node);
   List<PatternToken> getTokens(List<ASTNode> node_list); //For indexing.
   List<PatternToken> getTokens(ASTNode node,String data);

   //Get tokens from the code's text contents (including comments)
   List<PatternToken> getTokens(String cnts);
}



/********************************************************************************/
/*										*/
/*	Analysis Types								*/
/*										*/
/********************************************************************************/

enum AnalysisType {

   //No underscores or hyphens for an ANALTYPE name!
   //Classes AFG* are tokenizers used to produce tokens (based on AST) for indexing and search.
   KGRAM5CFG3(edu.brown.cs.cocker.analysis.AFG.class, 5,true),
   SSFIX(edu.brown.cs.cocker.analysis.AFGK5W.class, 5,true),
   SSFIXFULL(edu.brown.cs.cocker.analysis.AFGK5W.class, 5,true),
   SHARPFIXLOCAL(edu.brown.cs.cocker.analysis.AFGForSharpFixLocalSearch.class, 5,true),
   STMTSEARCHLOCAL(edu.brown.cs.cocker.analysis.AFGK5W.class, 5, true),
   STMTSEARCHGLOBAL(edu.brown.cs.cocker.analysis.AFGK5W.class, 5, true),
   KGRAM5WORDCFG3(edu.brown.cs.cocker.analysis.AFGK5W.class, 5,true),
   KGRAM5WORDCFG5(edu.brown.cs.cocker.analysis.AFGK5W.class, 5,true),
   KGRAM5WORDCFG7(edu.brown.cs.cocker.analysis.AFGK5W.class, 5,true),
   KGRAM5WORDMD(edu.brown.cs.cocker.analysis.AFGK5W.class, 5,true),
   KGRAM5WORDCFG1(edu.brown.cs.cocker.analysis.AFGK5W.class, 5,true),
   KGRAM5WORDCFG2(edu.brown.cs.cocker.analysis.AFGK5W.class, 5,true),
   KGRAM5WORDCFG4(edu.brown.cs.cocker.analysis.AFGK5W.class, 5,true),
   KGRAM3WORDMD(edu.brown.cs.cocker.analysis.AFGK5W.class, 3,true),
   KGRAM7WORDMD(edu.brown.cs.cocker.analysis.AFGK5W.class, 7,true),
   ITEM0MDONEFIELD(edu.brown.cs.cocker.analysis.AFGCI0.class, 5,true);

   private Class<? extends PatternTokenizer> pattern_class;
   private Object			     pattern_arg;
   private boolean			     needs_jcomp;
   private static AnalysisParser	     our_parser = null;

   AnalysisType(Class<? extends PatternTokenizer> cls,Object o,boolean compile) {
      pattern_class = cls;
      pattern_arg = o;
      needs_jcomp = compile;
   }

   public static AnalysisType getType(String s) {
      if (s == null) s = DEFAULT_ANALYSIS_TYPE;
      if (s != null) s = s.toUpperCase();
      s = s.replace("_","");
      s = s.replace("-","");
      for (AnalysisType t : values()) {
         if (s == null) s = t.toString();
         if (t.toString().toUpperCase().equals(s)) return t;
       }
      return KGRAM5CFG3;
    }


   public PatternTokenizer createTokenizer() {
      try {
	 if (pattern_arg != null) {
	    Constructor<? extends PatternTokenizer> cnst = pattern_class.getConstructor(Object.class);
	    return cnst.newInstance(pattern_arg);
	  }
       }
      catch (Throwable t) { }
      try {
	 return pattern_class.getConstructor().newInstance();
   //	 return pattern_class.newInstance();
       }
      catch (Throwable t) { t.printStackTrace(); }
      return null;
    }


   public boolean needsCompilation()			{ return needs_jcomp; }

   public File getIndexPath() {
      File dir = Factory.getIndexDirectory();
      String inm = INDEX_PATH_NAME + toString().toLowerCase();
      File f1 = new File(dir,inm);
      return f1;
    }
   
   public URI getIndexURI() {
      try {
         File f = getIndexPath();
         String urinm = "file://" + f.getPath().replace(File.separator,"/");
         URI uri = new URI(urinm);
         return uri;
       }
      catch (URISyntaxException e) { }
      return null;
    }

   public String getDatabaseName() {
      String s1 = Factory.getIndexDirectory().toString();
      String s2 = IvyFile.digestString(s1);
      if (s2.length() > 6) s2 = s2.substring(0,6);
      return DATABASE_NAME_PREFIX + s2 + "_" + toString().toLowerCase();
    }

   public int getDefaultPortNumber() {
      return DEFAULT_PORT_BASE + ordinal();
    }

   /* Get the code's textual content as a string. */
   public String parseIntoText(Reader rdr) throws IOException {
       String cnts = IvyFile.loadFile(rdr);
       if (rdr instanceof AnalysisCodeReader) {
	   AnalysisCodeReader code_rdr = (AnalysisCodeReader) rdr;
	   return parseIntoText(cnts, code_rdr.getExtendedStartPosition(),
				code_rdr.getExtendedEndPosition(), ChunkType.FILE);
       }
       else {
	   return cnts;
       }
   }

   public String parseIntoText(String cnts, int ex_start_pos, int ex_end_pos, ChunkType type) {
       return cnts.substring(ex_start_pos, ex_end_pos);
   }


   /* For indexing.
      In SearchContext.java, files are scanned, and code fragments to be indexed are generated.
      The list of AST nodes corresponding to the code fragments are saved as field in rdr.
      AnalysisJavaTokenizer calls this method to get a list of nodes.
      Later, the tokenizer (instance of class AFG*) scans these nodes to produce tokens to index.
   */
   public List<ASTNode> parseIntoASTNodes(Reader rdr) throws IOException  {
      if (rdr instanceof AnalysisCodeReader) {
	  AnalysisCodeReader code_rdr = (AnalysisCodeReader) rdr;
	  return code_rdr.getNodeList(); }
      else {
	  String cnts = IvyFile.loadFile(rdr);
	  return parseIntoASTNodes(cnts,ChunkType.FILE);
      }
    }

   /* For query. 
      Obtain the AST nodes from file content.
      See method codequery in CockerServer.java, which handles query. */
   public List<ASTNode> parseIntoASTNodes(String cnts, ChunkType type) {
      if (our_parser == null) our_parser = new AnalysisParser();
      ASTNode parse_node = our_parser.parseIntoASTNode(cnts,type);
      List<ASTNode> parse_node_list = new ArrayList<ASTNode>();
      parse_node_list.add(parse_node);
      return parse_node_list;
   }

   public List<ASTNode> parseIntoASTNodes(String cnts, String loc, ChunkType type) {
      if (our_parser == null) our_parser = new AnalysisParser();
      return our_parser.parseIntoASTNodes(cnts,loc,type);
    }
   
   public String getPropertyFile() {
      String pn = CockerConstants.PROPERTY_FILE_NAME; 
      pn = pn.replace("$",toString().toLowerCase());
      return pn;
    }
   
   public File getLogFile() {
      String pn = "cocker-" + toString().toLowerCase() + ".log";
      File dir = Factory.getIndexDirectory(); 
      return new File(dir,pn);
    }
   
}	// end of inner enum AnalysisType



class Factory {
   private static AnalysisType analysis_type = AnalysisType.getType(null);
   private static File index_directory = null;

   public static void setAnalysisType(String type) {
      analysis_type = AnalysisType.getType(type);
    }

   public static void setAnalysisType(AnalysisType type) {
      if (type == null) type = AnalysisType.getType(null);
      analysis_type = type;
    }

   public static AnalysisType getAnalysisType() {
      return analysis_type;
    }

   public static void setIndexDirectory(File dir) {
      index_directory = dir;
    }
   
   public static File getIndexDirectory() {
      if (index_directory == null) {
         String idxnm = System.getProperty("COCKER_INDEX");
         if (idxnm == null) idxnm = System.getenv("COCKER_INDEX");
         if (idxnm != null) {
            File f1 = new File(idxnm);
            if (f1.exists() && f1.isDirectory()) 
               index_directory = f1;
          }
       }
      
      File home = new File(System.getProperty("user.home"));
      File fprop = new File(home,".cockerrc");
      if (fprop.exists() && fprop.canRead()) {
         Properties p = new Properties();
         try (Reader ir = new FileReader(fprop)) {
            p.load(ir);
          }
         catch (IOException e) { }
         String dir = p.getProperty("index");
         if (dir != null) {
            File f = new File(dir);
            if (f.exists() && f.isDirectory()) {
               index_directory = f;
             }
          }
       }
      
      if (index_directory == null) {
         ResourceFinder rf = new ResourceFinder("COCKER_HOME");
         index_directory = rf.getDirectory("index");
       }
      return index_directory;
    }
   
}	// end of inner class Factory




}	// end of interface AnalysisConstants




/* end of AnalysisConstants.java */
