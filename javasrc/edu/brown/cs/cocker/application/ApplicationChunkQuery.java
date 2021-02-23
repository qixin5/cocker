/********************************************************************************/
/*										*/
/*		ApplicationChunkQuery.java					*/
/*										*/
/*	Handle queries based on code chunks					*/
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



package edu.brown.cs.cocker.application;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.cocker.search.SearchResult;
import edu.brown.cs.cocker.server.ServerConnectionInformation;
import edu.brown.cs.cocker.server.ServerConstants;
import edu.brown.cs.cocker.server.ServerSession;
import edu.brown.cs.cocker.analysis.AnalysisConstants;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.ivy.file.*;


import java.io.File;

public class ApplicationChunkQuery implements ServerConstants, AnalysisConstants
{

/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   ApplicationChunkQuery chunker = new ApplicationChunkQuery(args);
   String s = chunker.execute();
   System.out.println(s);
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ServerConnectionInformation connection_info;
private int			max_results;
private File			input_file;
private ChunkType		chunk_type;
private String			query_data;
private String			search_strategy;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/


public ApplicationChunkQuery(String [] args)
{
   connection_info = new ServerConnectionInformation(DEFAULT_HOSTNAME,
	 0,DEFAULT_TIMEOUT);

   max_results = -1;
   input_file = null;
   chunk_type = ChunkType.STATEMENTS;
   query_data = null;
   search_strategy = null;

   scanArgs(args);
   
   if (connection_info.getPort() == 0) {
      AnalysisType anal = AnalysisConstants.Factory.getAnalysisType();
      connection_info.setPort(anal.getDefaultPortNumber());
    }
}



/********************************************************************************/
/*										*/
/*	Argument scanning							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   File datapath = null;

   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-p") && i+1 < args.length) {           // -port <port>
	    try {
	       connection_info.setPort(Integer.parseInt(args[++i]));
	     }
	    catch (NumberFormatException e) { badArgs(); }
	  }
	 else if (args[i].startsWith("-n") && i+1 < args.length) {      // -name <hostname>
	    connection_info.setHostname(args[++i]);
	  }
	 else if (args[i].startsWith("-h") && i+1 < args.length) {      // -host <hostname>
	    connection_info.setHostname(args[++i]);
	  }
	 else if (args[i].startsWith("-t") && i+1 < args.length) {      // -time <timeout>
	    try {
	       connection_info.setTimeout(Integer.parseInt(args[++i]));
	     }
	    catch (NumberFormatException e) { badArgs(); }
	  }
	 else if (args[i].startsWith("-m") && i+1 < args.length) {      // -max <maxresults>
	    try {
	       max_results = Integer.parseInt(args[++i]);
	     }
	    catch (NumberFormatException e) { badArgs(); }
	  }
	 else if (args[i].startsWith("-f") && i+1 < args.length) {      // -file <file>
	    input_file = new File(args[++i]);
	  }
	 else if (args[i].startsWith("-S")) {                           // -STATEMENTS
	    chunk_type = ChunkType.STATEMENTS;
	  }
	 else if (args[i].startsWith("-M")) {                           // -METHOD
	    chunk_type = ChunkType.METHOD;
	  }
	 else if (args[i].startsWith("-C")) {                           // -CLASS
	    chunk_type = ChunkType.CLASS;
	  }
	 else if (args[i].startsWith("-F")) {                           // -FILE
	    chunk_type = ChunkType.FILE;
	  }
	 else if (args[i].startsWith("-a") && i+1 < args.length) {      // -analysis <type>
	    String type = args[++i];
	    AnalysisConstants.Factory.setAnalysisType(type);
	    // set port to default for type?
	  }
         else if (args[i].startsWith("-dir") && i+1 < args.length) {    // -dir datapath
            datapath = new File(args[++i]);
            AnalysisConstants.Factory.setIndexDirectory(datapath);
            connection_info.setDatapath(datapath);
          }
	 else if (args[i].startsWith("-d") && i+1 < args.length) {      // -data <querydata>
	    query_data = args[++i];
	  }
         else if (args[i].startsWith("-q") && i+1 < args.length) {      // -query <querydata>
	    query_data = args[++i];
	  }
	 else if (args[i].startsWith("-s") && i+1 < args.length) {      // -strategy <strategy>
	    search_strategy = args[++i];
	  }
	 else badArgs();
       }
      else if (input_file == null) {
	 input_file = new File(args[i]);
       }
      else badArgs();
    }
}



private void badArgs()
{
   System.err.println("COCKER: query ...");
   System.exit(1);
}



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

public String execute()
{
   String cnts = null;
   try {
      if (input_file == null) {
	 cnts = IvyFile.loadFile(new InputStreamReader(System.in));
       }
      else {
	 cnts = IvyFile.loadFile(input_file);
       }
    }
   catch (IOException e) {
      System.err.println("COCKER: Bad input: " + e);
    }

   if (cnts == null || cnts.length() == 0) return "";

   List<SearchResult> results = queryServer(max_results,cnts);

   if (results == null) {
      System.err.println("ERROR getting results");
      System.exit(1);
    }

   //printResults(results);
   return getResultString(results);
}




private List<SearchResult> queryServer(int max,String text)
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("COMMAND");
   xw.field("CMD","CODEQUERY");
   xw.field("MAX",max);
   xw.field("TYPE",chunk_type);
   if (query_data != null) xw.field("DATA",query_data);
   if (search_strategy != null) xw.field("SEARCHSTRATEGY",search_strategy);
   xw.cdataElement("CODE",text);
   xw.end("COMMAND");
   String msg = xw.toString();
   xw.close();
   try {
      ServerSession sess = new ServerSession(connection_info); Element resp = sess.sendRequest(msg);
      List<SearchResult> results = new ArrayList<SearchResult>();
      for (Element relt : IvyXml.children(resp,"FILE")) {
	 SearchResult r = new SearchResult(IvyXml.getText(relt),
	       IvyXml.getAttrString(relt,"MLOC"),
	       IvyXml.getAttrFloat(relt,"SCORE",0));
	 results.add(r);
       }
      Collections.sort(results);
      sess.close();
      return results;
    }
   catch (IOException e) { }
   catch (MalformedMessageException e) { }

   return null;
}




protected void printResults(List<SearchResult> results)
{
   Collections.sort(results);

   for (SearchResult result : results) {
     System.out.println("file://" + result.getFilePath() + "," + result.getFileLoc() + "," + result.getScore());
    }
}

protected String getResultString(List<SearchResult> results) {
    Collections.sort(results);

    StringBuilder sb = new StringBuilder();
    for (SearchResult result : results) {
      sb.append("file://" + result.getFilePath() + "," + result.getFileLoc() + "," + result.getScore() + "\n");
    }

    return sb.toString();
}

}	// end of class ApplicationChunkQuery




/* end of ApplicationChunkQuery.java */
