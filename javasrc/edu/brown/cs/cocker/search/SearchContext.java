/********************************************************************************/
/*										*/
/*		SearchContext.java						*/
/*										*/
/*	Context for doing a search						*/
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

package edu.brown.cs.cocker.search;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import edu.brown.cs.cocker.server.ServerConstants;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.nio.file.Path;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.cocker.analysis.AnalysisCodeReader;
import edu.brown.cs.cocker.analysis.AnalysisConstants;
import edu.brown.cs.cocker.util.*;

//import asts.ASTUtils;
import org.eclipse.jdt.core.dom.*;

public class SearchContext implements ServerConstants {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private IndexWriter     lucene_writer;
private AnalysisConstants.AnalysisType anal_type;
public final static Set<String> cfg_anal_types;
public final static Set<String> cfg_item_anal_types;
public final static Set<String> md_anal_types;
public final static Set<String> md_item_anal_types;

private GeneralIndexComponentGenerator general_icgen;
private SSFIXIndexComponentGenerator ssfix_icgen;
    //private CodeItemExtractor cie;
    

/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

static {
    cfg_anal_types = new HashSet<String>(); //to be added
    cfg_anal_types.add("KGRAM5CFG3");
    cfg_anal_types.add("SSFIX");
    cfg_anal_types.add("SSFIXFULL");
    cfg_anal_types.add("KGRAM5WORDCFG3");
    cfg_anal_types.add("KGRAM5WORDCFG5");
    cfg_anal_types.add("KGRAM5WORDCFG7");
    cfg_anal_types.add("KGRAM5WORDCFG1");
    cfg_anal_types.add("KGRAM5WORDCFG2");
    cfg_anal_types.add("KGRAM5WORDCFG4");

    cfg_item_anal_types = new HashSet<String>(); //to be added
    cfg_item_anal_types.add("ITEM0CFG5ONEFIELD");
    
    md_anal_types = new HashSet<String>(); //to be added
    md_anal_types.add("KGRAM3WORDMD");    
    md_anal_types.add("KGRAM5WORDMD");
    md_anal_types.add("KGRAM7WORDMD");

    md_item_anal_types = new HashSet<String>(); //to be added
    md_item_anal_types.add("ITEM0MDONEFIELD");
}
    
public SearchContext(IndexWriter writer)
{
   lucene_writer = writer;
   anal_type = AnalysisConstants.Factory.getAnalysisType();
   general_icgen = new GeneralIndexComponentGenerator();
   ssfix_icgen = new SSFIXIndexComponentGenerator();
   //cie = new CodeItemExtractor();
}




/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

public void addFileToIndex(ServerFile file) throws IOException
{
    String anal_type_str = (anal_type == null) ? "" : anal_type.toString();
//  Reader fr = file.getReader();
    String fpath = file.getPath();
    File f = new File(fpath);
//  String ftext = IvyFile.loadFile(fr);
    CompilationUnit cu = null;
    //try { cu = (CompilationUnit) ASTUtils.getResolvedASTNode(fpath, ftext); }
    try { cu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(f); }
    catch (Throwable t) {
	IvyLog.logE("SEARCH","Problem parsing file " + fpath,t);
     }
    if (cu == null) { return; }
    //List<MethodDeclaration> md_list = ASTUtils.getMethodDeclarations(cu);
    List<MethodDeclaration> md_list = MethodDeclarationGetter.getMethodDeclarations(cu);

    if (md_anal_types.contains(anal_type_str) ||
	md_item_anal_types.contains(anal_type_str)) {
	if (anal_type_str.startsWith("SSFIX")) {
	    addMethodToIndex(cu, md_list, file, ssfix_icgen);
	}
	else {
	    addMethodToIndex(cu, md_list, file, general_icgen);
	}
    }
    else if (cfg_anal_types.contains(anal_type_str) ||
	     cfg_item_anal_types.contains(anal_type_str)) {
	if (anal_type_str.startsWith("SSFIX")) {
	    addCodeFragmentToIndex(cu, md_list, file, ssfix_icgen, 3);
	}
	else {
	    if (anal_type_str.contains("CFG1")) {
		addCodeFragmentToIndex(cu, md_list, file, general_icgen, 1);
	    }
	    else if (anal_type_str.contains("CFG2")) {
		addCodeFragmentToIndex(cu, md_list, file, general_icgen, 2);
	    }
	    else if (anal_type_str.contains("CFG3")) {
		addCodeFragmentToIndex(cu, md_list, file, general_icgen, 3);
	    }
	    else if (anal_type_str.contains("CFG4")) {
		addCodeFragmentToIndex(cu, md_list, file, general_icgen, 4);
	    }
	    else if (anal_type_str.contains("CFG5")) {
		addCodeFragmentToIndex(cu, md_list, file, general_icgen, 5);
	    }
	    else if (anal_type_str.contains("CFG7")) {
		addCodeFragmentToIndex(cu, md_list, file, general_icgen, 7);
	    }
	    else {
		addCodeFragmentToIndex(cu, md_list, file, general_icgen, 3);
	    }
	}
    }
}
    
/* No comments are indexed. */
private void addMethodToIndex(CompilationUnit cu, List<MethodDeclaration> md_list, ServerFile file, IndexComponentGenerator icgen) throws IOException
{
    String fpath = file.getPath();
    for (MethodDeclaration md : md_list) {
	//Get the slc locating string
	int start_pos = md.getStartPosition();
	String loc = "slc:" + cu.getLineNumber(start_pos) + "," + cu.getColumnNumber(start_pos);
	int ex_start_pos = cu.getExtendedStartPosition(md); //including comments
	int ex_length = cu.getExtendedLength(md); 
	IvyLog.logD("SEARCH","Add file " + fpath + ", " + loc);

        try {
           //Create a method reader
           //Note here a newly created Reader from file.getReader() is used
           AnalysisCodeReader md_code_rdr = new AnalysisCodeReader(file.getReader());
           md_code_rdr.setLocString(loc);
	   md_code_rdr.setExtendedStartPosition(ex_start_pos);
	   md_code_rdr.setExtendedEndPosition(ex_start_pos+ex_length);
	   List<ASTNode> node_list = new ArrayList<ASTNode>();
	   node_list.add(md);
	   md_code_rdr.setNodeList(node_list);
           
           //Create a new document
           Document doc = new Document();
           Field pathfield = new StringField("path",fpath,Field.Store.YES);
           doc.add(pathfield);
           doc.add(new LongField("modified",file.lastModified(),Field.Store.NO));
           doc.add(new StringField("mloc",loc,Field.Store.YES));
           doc.add(new TextField("code",md_code_rdr));

           lucene_writer.addDocument(doc);
           IvyLog.logD("SEARCH","Done file " + fpath + ", " + loc);
         }
        catch (Throwable t) {
           IvyLog.logE("SEARCH","Problem scanning file " + fpath,t);
         }
    }
}


/* No comments are indexed. */
private void addCodeFragmentToIndex(CompilationUnit cu, List<MethodDeclaration> md_list, ServerFile file, IndexComponentGenerator icgen, int index_k) throws IOException
{
    String fpath = file.getPath();
    for (MethodDeclaration md : md_list) {
	//Get the src-locations for all the components to be indexed
	List<IndexComponent> ic_list = icgen.getIndexComponentsForMD(md, index_k);
	for (IndexComponent ic : ic_list) {
	    List<ASTNode> ic_node_list = ic.getNodeList();
	    String slc_loc = "";
	    int ic_node_list_size = ic_node_list.size();
	    int ex_start_pos = -1;
	    int ex_end_pos = -1;
	    //Get the location string, extended start position and extended length for this component
	    for (int i=0; i<ic_node_list_size; i++) {
		ASTNode ic_node = ic_node_list.get(i);
		int ic_node_start_pos = ic_node.getStartPosition();
		if (i==0) {
		    slc_loc = "slc:"+cu.getLineNumber(ic_node_start_pos)+","+cu.getColumnNumber(ic_node_start_pos);
		    ex_start_pos = cu.getExtendedStartPosition(ic_node);
		}
		else {
		    slc_loc += ";slc:"+cu.getLineNumber(ic_node_start_pos)+","+cu.getColumnNumber(ic_node_start_pos);
		}
		if (i==ic_node_list_size-1) {
		    ex_end_pos = cu.getExtendedStartPosition(ic_node) + cu.getExtendedLength(ic_node);
		}
	    }
	    IvyLog.logD("SEARCH","Add file " + fpath + ", " + slc_loc);

	    if (("".equals(slc_loc)) || (ex_start_pos == -1) || (ex_end_pos == -1)) {
		IvyLog.logE("SEARCH","Problem scanning file " + fpath);
		continue;
	    }
	    
	    //Create a component file-reader
	    //Note here a newly created Reader from file.getReader() is used
	    try {
		AnalysisCodeReader md_code_rdr = new AnalysisCodeReader(file.getReader());
		md_code_rdr.setLocString(slc_loc);
		md_code_rdr.setExtendedStartPosition(ex_start_pos);
		md_code_rdr.setExtendedEndPosition(ex_end_pos);
		md_code_rdr.setNodeList(ic_node_list);
		
		//Create a new document
		Document doc = new Document();
		Field pathfield = new StringField("path",fpath,Field.Store.YES);
		doc.add(pathfield);
		doc.add(new LongField("modified",file.lastModified(),Field.Store.NO));
		doc.add(new StringField("mloc",slc_loc,Field.Store.YES));
		doc.add(new TextField("code",md_code_rdr));
		
		lucene_writer.addDocument(doc);
		IvyLog.logD("SEARCH","Done file " + fpath + ", " + slc_loc);
	    }
	    catch (Throwable t) {
		IvyLog.logE("SEARCH","Problem scanning file " + fpath,t);
	    }
	}        
    }

}

    
public void commitContext() throws IOException
{
    lucene_writer.commit();
    Directory d = lucene_writer.getDirectory();
    if (d instanceof FSDirectory) {
       FSDirectory fsd = (FSDirectory) d;
       Path p = fsd.getDirectory();
       IvyFile.updatePermissions(p,0777);
     }
}




public void removeFileFromIndex(ServerFile file) throws IOException
{
   Term t = new Term("path",file.getPath());
   IvyLog.logD("SEARCH","Delete file " + file.getPath());
   lucene_writer.deleteDocuments(t);
}



public void rollbackContext() throws IOException
{
   lucene_writer.rollback();
    Directory d = lucene_writer.getDirectory();
    if (d instanceof FSDirectory) {
       FSDirectory fsd = (FSDirectory) d;
       Path p = fsd.getDirectory();
       IvyFile.updatePermissions(p,0777);
     }
}



public void updateFileInIndex(ServerFile file) throws IOException
{
   Reader fr = file.getReader();
   Document doc = new Document();
   Field pathfield = new StringField("path",file.getPath(),Field.Store.YES);
   doc.add(pathfield);
   doc.add(new LongField("modified",file.lastModified(),Field.Store.NO));
   doc.add(new TextField("code",fr));
   
   lucene_writer.updateDocument(new Term("path",file.getPath()),doc);
}



}	// end of class SearchContext




/* end of SearchContext.java */
