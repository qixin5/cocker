/********************************************************************************/
/*										*/
/*		AnalysisParser.java						*/
/*										*/
/*	Parsing of code fragments (and resolution if needed)			*/
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

import java.util.*;
import org.eclipse.jdt.core.dom.*;
import edu.brown.cs.ivy.jcomp.*;
//import asts.*;
import edu.brown.cs.cocker.util.ASTNodeFinder;

class AnalysisParser implements AnalysisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcompControl	jcomp_base;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

AnalysisParser()
{
   jcomp_base = null;
   if (Factory.getAnalysisType().needsCompilation()) {
      jcomp_base = new JcompControl();
    }
}


/********************************************************************************/
/*										*/
/*	Parsing methods 							*/
/*										*/
/********************************************************************************/

ASTNode parseIntoASTNode(String cnts,ChunkType type)
{
   try {
      ASTNode root = null;
      if (jcomp_base == null) {
	 FileData fd = new FileData(cnts,type);
	 root = fd.getAstRootNode();
       }
      else {
	 FileData fd = new FileData(cnts,ChunkType.FILE);
	 List<JcompSource> srcs = new ArrayList<JcompSource>();
	 srcs.add(fd);
	 JcompProject proj = jcomp_base.getProject(srcs);
	 proj.resolve(); //RESOLVED!
	 JcompSemantics semdata = jcomp_base.getSemanticData(fd);
	 root = semdata.getAstNode();
	 jcomp_base.freeProject(proj);
       }
      return root;
    }
   catch (Throwable t) {
      System.err.println("Problem parsing file");
      t.printStackTrace();
    }
   return null;
}

List<ASTNode> parseIntoASTNodes(String cnts, String loc, ChunkType type)
{
   List<ASTNode> parse_node_list = new ArrayList<ASTNode>();
   try {
      ASTNode root = null;
      if (jcomp_base == null) {
	 FileData fd = new FileData(cnts,type);
	 root = fd.getAstRootNode();
       }
      else {
	 FileData fd = new FileData(cnts,ChunkType.FILE);
	 List<JcompSource> srcs = new ArrayList<JcompSource>();
	 srcs.add(fd);
	 JcompProject proj = jcomp_base.getProject(srcs);
	 proj.resolve(); //RESOLVED!
	 JcompSemantics semdata = jcomp_base.getSemanticData(fd);
	 root = semdata.getAstNode();
	 jcomp_base.freeProject(proj);
       }

      if ((root == null) || !(root instanceof CompilationUnit)) {
	  return parse_node_list;
      }
      CompilationUnit root_cu = (CompilationUnit) root;
      List<ASTNode> target_node_list = ASTNodeFinder.find(root_cu, loc);
      if (target_node_list.isEmpty()) {
	  System.err.println("Location string is invalid: " + loc);
      }
      return target_node_list;
    }
   catch (Throwable t) {
      System.err.println("Problem parsing file");
      t.printStackTrace();
    }
   return parse_node_list;
}
    

private static class FileData implements JcompExtendedSource {

   private String file_contents;
   private ChunkType chunk_type;
   private ASTNode ast_node;

   FileData(String cnts,ChunkType typ) {
      file_contents = cnts;
      chunk_type = typ;
      ast_node = null;
    }

   @Override public String getFileContents()	{ return file_contents; }

   @Override public String getFileName()	{ return "Dummy.java"; }

   @Override public ASTNode getAstRootNode() {
      if (ast_node == null) {
	 int parsetype;
	 switch (chunk_type) {
	    default:
	    case STATEMENTS :
	       parsetype = ASTParser.K_STATEMENTS;
	       break;
	    case METHOD :
	    case CLASS :
	       parsetype = ASTParser.K_CLASS_BODY_DECLARATIONS;
	       break;
	    case FILE :
	       parsetype = ASTParser.K_COMPILATION_UNIT;
	       break;
	  }
	 ASTParser parser = ASTParser.newParser(AST.JLS4);
	 parser.setKind(parsetype);
	 parser.setSource(file_contents.toCharArray());
	 ast_node = parser.createAST(null);
       }
      return ast_node;
    }
	
}	// end of inner class FileData

}	// end of class AnalysisParser




/* end of AnalysisParser.java */
