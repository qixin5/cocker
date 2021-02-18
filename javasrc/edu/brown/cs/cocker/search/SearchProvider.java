/********************************************************************************/
/*										*/
/*		SearchProvider.java						*/
/*										*/
/*	Interface to our search provider (Compass)				*/
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

import org.apache.lucene.store.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.io.File;
import java.net.URI;
import java.nio.file.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import edu.brown.cs.cocker.analysis.AnalysisJavaCode;
import edu.brown.cs.cocker.analysis.AnalysisConstants;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.document.Document;
import edu.brown.cs.ivy.file.IvyFile;


public class SearchProvider implements AnalysisConstants {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private FSDirectory	lucene_directory;
private IndexWriter	lucene_writer;
private DirectoryReader lucene_reader;
private Analyzer	code_analyzer;

private static SearchProvider the_provider = null;

public static final int MAX_AGGREGATE_RESULTS_ALL = -1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public static synchronized SearchProvider getProvider() throws IOException
{
   if (the_provider == null) {
      the_provider = new SearchProvider();
    }
   return the_provider;
}



private SearchProvider() throws IOException
{
   code_analyzer = new AnalysisJavaCode();
   URI pathuri = AnalysisConstants.Factory.getAnalysisType().getIndexURI();
   
   Path path = Paths.get(pathuri);
   lucene_directory = new NIOFSDirectory(path);
   IndexWriterConfig config = new IndexWriterConfig(code_analyzer);
   lucene_writer = new IndexWriter(lucene_directory,config);
// lucene_reader = DirectoryReader.open(lucene_directory);
// lucene_reader = DirectoryReader.open(lucene_writer,true);
   clearReader();
   
   File file = path.toFile();
   file = new File(file,"write.lock");
   file.deleteOnExit();
}




/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

public void createIndex() throws IOException
{
    lucene_writer.deleteAll();
    Directory d = lucene_writer.getDirectory();
    if (d instanceof FSDirectory) {
       FSDirectory fsd = (FSDirectory) d;
       Path p = fsd.getDirectory();
       IvyFile.updatePermissions(p,0777);
     }
    clearReader();
}





public void deleteIndex() throws IOException
{
   lucene_writer.deleteAll();
   clearReader();
}



public SearchContext openContext()
{
   clearReader();
   return new SearchContext(lucene_writer);
}


public void optimizeIndex() throws IOException
{
   lucene_writer.forceMergeDeletes();
   lucene_writer.commit();
   clearReader();
   Directory d = lucene_writer.getDirectory();
   if (d instanceof FSDirectory) {
      FSDirectory fsd = (FSDirectory) d;
      Path p = fsd.getDirectory();
      IvyFile.updatePermissions(p,0777);
    }
}



/********************************************************************************/
/*										*/
/*	Search methods								*/
/*										*/
/********************************************************************************/

public List<SearchResult> search(String searchString,int max)
{
   QueryParser qp = new QueryParser(SEARCH_FIELD,code_analyzer);
   try {
      Query query = qp.parse(searchString);
      return search(query,max);
    }
   catch (ParseException e) {
      System.err.println("Query parsing problem: " + e);
      e.printStackTrace();
    }

   return Collections.emptyList();
}



public List<SearchResult> search(Query query,int max)
{
   List<SearchResult> result = new LinkedList<SearchResult>();
   DirectoryReader dr = getReader();
   if (dr == null) return result;
      
   IndexSearcher searcher = new IndexSearcher(dr); //Change searcher to be a field?
      
   try {
      if (max <= 0) max = 10240;
      ScoreDoc [] hits = searcher.search(query,null,max).scoreDocs;
      for (ScoreDoc hit : hits) {
	 Document hitdoc = searcher.doc(hit.doc);
	 //hitdoc.get("mloc") can be NULL
	 result.add(new SearchResult(hitdoc.get("path"),hitdoc.get("mloc"),hit.score));
	 /*****************/
	 /*
	 System.err.println("Doc ID: " + hit.doc);
	 Explanation explain = searcher.explain(query,hit.doc);
	 System.err.println(explain);
	 */
	 /*****************/
       }
    }
   catch (IOException e) {
      System.err.println("Search problem: " + e);
      e.printStackTrace();
    }

   return result;
}


private synchronized void clearReader()
{
   lucene_reader = null;
}


private synchronized DirectoryReader getReader()
{
   if (lucene_reader == null) {
      try {
         lucene_reader = DirectoryReader.open(lucene_writer,true);
       }
      catch (IOException e) { }
    }
   return lucene_reader;
}

}	// end of class SearchProvider



/* end of SearchProvider.java */
