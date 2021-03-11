/********************************************************************************/
/*										*/
/*		CockerServer.java						*/
/*										*/
/*	Implementation of cocker-specific server for indexing/searching 	*/
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

package edu.brown.cs.cocker.cocker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Element;

import edu.brown.cs.cocker.search.SearchResult;
import edu.brown.cs.cocker.server.ServerRequestCallback;
import edu.brown.cs.cocker.util.ResourceFinder;
import edu.brown.cs.cocker.server.ServerConstants;
import edu.brown.cs.cocker.server.ServerOperation;
import edu.brown.cs.cocker.server.Server;
import edu.brown.cs.cocker.analysis.AnalysisConstants;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.file.IvyLog;

import org.eclipse.jdt.core.dom.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import java.io.File;

public class CockerServer extends Server implements CockerConstants, ServerConstants, AnalysisConstants {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private CockerEngine the_engine;
private long	update_interval;
private Timer	server_timer;
private Updater current_updater;
private long	last_action;
private long	idle_time;
private IdleCheck idle_checker;




/********************************************************************************/
/*										*/
/*	Main Program								*/
/*										*/
/********************************************************************************/

public static void main(String[] args)
{
   int port = 0;
   int tpsize = DEFAULT_THREAD_POOL_SIZE;
   boolean log = true;
   File datapath = null;
   IvyLog.LogLevel loglevel = IvyLog.LogLevel.DEBUG;

   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-p") && i+1 < args.length) {           // -port <port>
	    try {
	       port = Integer.parseInt(args[++i]);
	     }
	    catch (NumberFormatException e) {
	       badArgs();
	     }
	  }
	 else if (args[i].startsWith("-t") && i+1 < args.length) {      // -thread <#threads>
	    try {
	       tpsize = Integer.parseInt(args[++i]);
	     }
	    catch (NumberFormatException e) {
	       badArgs();
	     }
	  }
	 else if (args[i].startsWith("-a") && i+1 < args.length) {     // -analysis <type>
	    AnalysisConstants.Factory.setAnalysisType(args[++i]);
	  }
	 else if (args[i].startsWith("-nol")) {                        // -nolog :: NO LOGGING
	    log = false;
	  }
	 else if (args[i].startsWith("-l")) {                          // -log :: LOGGING
	    log = false;
	  }
	 else if (args[i].startsWith("-dir") && i+1 < args.length) {   // -dir <data path>
	    datapath = new File(args[++i]);
	    datapath.mkdirs();
	    if (!datapath.exists()) {
	       badArgs();
	     }
	  }
	 else badArgs();
       }
      else badArgs();
    }

   if (datapath != null) {
      AnalysisConstants.Factory.setIndexDirectory(datapath);
    }

   if (log) {
      File f = AnalysisConstants.Factory.getAnalysisType().getLogFile();
      f.delete();
      IvyLog.setupLogging("COCKER",false);
      IvyLog.setLogFile(f);
      IvyLog.setLogLevel(loglevel);
      IvyLog.useStdErr(false);
      IvyFile.updatePermissions(f,0666);
    }
   
   IvyLog.logI("COCKER","Ready to start " + datapath + " " + port + " " + tpsize);

   CockerServer cs = new CockerServer(datapath,port,tpsize);

   try {
      cs.start();
    }
   catch (IOException e) {
      IvyLog.logE("COCKER","Problem starting server: ",e);
      System.err.println("COCKER: Problem starting server: " + e);
      System.exit(1);
    }
}


private static void badArgs()
{
   System.err.println("COCKER: server [-p <port>] [-t <threadpoolsize.]");
   System.exit(1);
}


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private CockerServer(File datapath,int port,int threadPoolSize)
{
   super(port,threadPoolSize,null);

   try {
      the_engine = new CockerEngine();

      ResourceFinder rf;
      if (datapath != null) {
	 File pfile = new File(datapath,DEFAULT_PROPERTY_FILE_NAME);
	 setProperties(pfile);
         String pnm = AnalysisConstants.Factory.getAnalysisType().getPropertyFile();
	 File pfile1 = new File(datapath,pnm);
	 setProperties(pfile1);
       }
      else {
	 rf = new ResourceFinder("COCKER_HOME");
	 InputStream ins = rf.getInputStream(DEFAULT_PROPERTY_FILE_NAME);
	 setProperties(ins);
       }

      update_interval = getLongProperty("UpdateInterval",DEFAULT_UPDATE_INTERVAL);
      last_action = System.currentTimeMillis();
      idle_time = 0;
      idle_checker = null;
      server_timer = new Timer("CockerTimer");
      current_updater = null;
      setRequestCallback(new CockerHandleRequestCallback());
      if (datapath != null) {
	 int wc = getIntProperty("WebClients",0);
	 setWebClients(wc);
       }
    }
   catch (IOException e) {
      System.err.println("COCKER: Problem starting server: " + e);
      System.exit(1);
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

CockerEngine getEngine()
{
   return the_engine;
}




/********************************************************************************/
/*										*/
/*	Startup 								*/
/*										*/
/********************************************************************************/

@Override public void start() throws IOException
{
   super.start();

   Date upt = new Date(System.currentTimeMillis() + UPDATE_DELAY);
   setUpdateTime(update_interval,upt);

   IvyLog.logI("COCKER","Server started");
}



/********************************************************************************/
/*										*/
/*	Handle periodic updates 						*/
/*										*/
/********************************************************************************/

private void setUpdateTime(long interval,Date next)
{
   if (update_interval != interval) {
      update_interval = interval;
      setProperty("UpdateInterval",interval);
    }

   if (current_updater != null) current_updater.cancel();

   if (interval == 0) {
      current_updater = null;
      return;
    }

   current_updater = new Updater();
   if (next == null) server_timer.schedule(current_updater,update_interval,update_interval);
   else server_timer.schedule(current_updater,next,update_interval);
}



/********************************************************************************/
/*										*/
/*	Handle idle timeout							*/
/*										*/
/********************************************************************************/

private void setIdleTime(long interval)
{
   idle_time = interval;
   noteAction();
}



private synchronized void noteAction()
{
   if (idle_checker != null) idle_checker.cancel();
   idle_checker = null;
   last_action = System.currentTimeMillis();

   if (idle_time != 0) {
      idle_checker = new IdleCheck();
      server_timer.schedule(idle_checker,idle_time);
    }
}


private synchronized void checkIdle()
{
   if (idle_time == 0) return;

   long now = System.currentTimeMillis();
   if (now - last_action < idle_time) return;

   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("COMMAND");
   xw.field("CMD","STOP");
   xw.end("COMMAND");
   Element cmd = IvyXml.convertStringToXml(xw.toString());
   xw.close();
   try {
      getRequestCallback().handleMessage(cmd,null,this);
    }
   catch (MalformedMessageException e) {
      IvyLog.logE("COCKER","Bad exit message: " + xw.toString(),e);
    }
}


private class IdleCheck extends TimerTask {

    IdleCheck() { }

    @Override public void run() {
       checkIdle();
     }

}	// end of inner class IdleCheck


private void showSchedule(IvyXmlWriter xw)
{
   xw.begin("SCHEDULE");
   if (current_updater != null) {
      xw.begin("JOB");
      xw.field("NAME","IndexUpdater");
      xw.field("LASTRUN",new Date(current_updater.scheduledExecutionTime()));
      xw.field("INTERVAL",update_interval);
      xw.end("JOB");
    }
   xw.end("SCHEDULE");
}






/********************************************************************************/
/*										*/
/*	Request handlers							*/
/*										*/
/********************************************************************************/

private class CockerHandleRequestCallback extends ServerRequestCallback {

   @Override public void handleMessage(Element xml,IvyXmlWriter xw,Server server)
   throws MalformedMessageException {
      noteAction();
      if (IvyXml.isElement(xml,"COMMAND")) {
         String cmd = IvyXml.getAttrString(xml,"CMD");
         IvyLog.logI("COCKER","Handle command: " + cmd);
         try {
            boolean done = true;
            boolean output = false;
            ServerOperation op = null;
            switch (cmd) {
               case "CODEQUERY" :
        	  codequery(IvyXml.getAttrInt(xml,"MAX"),
        		IvyXml.getTextElement(xml,"CODE"),
        		IvyXml.getAttrEnum(xml,"TYPE",ChunkType.STATEMENTS),
        		IvyXml.getTextElement(xml,"DATA"),
        		IvyXml.getTextElement(xml,"SEARCHSTRATEGY"),
        		server,xw);
        	  output = true;
        	  break;
               case "SYNCH" :
        	  op = new CockerOperation.Synchronize();
        	  break;
               case "OPTIMIZE" :
        	  op = new CockerOperation.Optimize();
        	  break;
               case "UPDATE" :
        	  op = new CockerOperation.Update();
        	  break;
               case "MONITOR" :
        	  op = new CockerOperation.Monitor(IvyXml.getTextElements(xml,"FILE"));
        	  break;
               case "UNMONITOR" :
        	  op = new CockerOperation.Unmonitor(IvyXml.getTextElements(xml,"FILE"));
        	  break;
               case "BLACKLIST" :
        	  op = new CockerOperation.Blacklist(IvyXml.getTextElements(xml,"FILE"));
        	  break;
               case "WHITELIST" :
        	  op = new CockerOperation.Whitelist(IvyXml.getTextElements(xml,"FILE"));
        	  break;
               case "UPDATETIME" :
        	  setUpdateTime(IvyXml.getAttrLong(xml,"INTERVAL"),IvyXml.getAttrDate(xml,"WHEN"));
        	  break;
               case "IDLETIME" :
        	  setIdleTime(IvyXml.getAttrLong(xml,"INTERVAL"));
        	  break;
               case "SCHEDULE" :
        	  showSchedule(xw);
        	  output = true;
        	  break;
               case "FILES" :
        	  getEngine().showFiles(xw);
        	  output = true;
        	  break;
               case "GETFILE" :
        	  File f = new File(IvyXml.getAttrString(xml,"FILE"));
        	  try {
        	     String cnts = IvyFile.loadFile(f);
        	     xw.bytesElement("CONTENTS",cnts.getBytes());
        	   }
        	  catch (IOException e) { }
        	  output = true;
        	  break;
               default :
        	  done = false;
        	  break;
             }
            if (op != null) {
               server.getOperationsManager().synchronousOperation(op);
             }
            if (done) {
               if (!output) xw.field("STATUS","OK");
               return;
             }
          }
         catch (Throwable e) {
            throw new MalformedMessageException("Scheduler problem: " + e,e);
          }
       }
      super.handleMessage(xml,xw,server);
   }








   private void codequery(int max,String code,ChunkType type,String data,String searchstrategy,Server s,IvyXmlWriter xw) throws IOException
   {
      noteAction();

      IvyLog.logI("COCKER","QUERY " + searchstrategy + " " + data + " " + type + " " + code);
      IvyLog.logI("COCKER","--------------------------------");

      CockerServer cs = (CockerServer) s;
      AnalysisType anal = AnalysisConstants.Factory.getAnalysisType();
      String anal_str = anal.toString(); //E.g., KGRAM5WORDMD
      PatternTokenizer tokenizer = anal.createTokenizer(); //E.g., AFGK5W
      if (data != null) type = ChunkType.FILE;

      //"code" is supposed to be the content of a Java class,
      //"node_list" is thus a list of size 1 which contains only
      //the parsed CompilationUnit which corresponds to "code"
      List<ASTNode> node_list = anal.parseIntoASTNodes(code,type);
      ASTNode root = node_list.get(0);
      List<PatternToken> toks = tokenizer.getTokens(root,data); //First find the target node and then obtain the query tokens
      IvyLog.logI("COCKER","QUERY TOKENS:");
      for (PatternToken tok : toks) {
	 IvyLog.logI1("COCKER",tok.getText());
	 int tok_prop = tok.getProp();
	 IvyLog.logI1("COCKER","(p"+tok_prop+") ");
       }

      Query q = null;
      BooleanQuery.setMaxClauseCount(2048); //Query tokens CANNOT exceed this number
      BooleanQuery bq = new BooleanQuery();
      bq.setMinimumNumberShouldMatch(toks.size()/8); //A candidate code SHOULD AT LEAST match this number of tokens

      boolean is_item0_onefield = (anal_str.startsWith("ITEM0") && anal_str.endsWith("ONEFIELD"));
      for (PatternToken pt : toks) {
	 Term t = new Term(SEARCH_FIELD,pt.getText()); //Specified searching the "code" (SEARCH_FIELD) field
	 TermQuery tq = new TermQuery(t);
	 int pt_prop = pt.getProp();
	 if (is_item0_onefield) {
	    if (pt_prop == 0) { tq.setBoost(10); } //class name
	    else if (pt_prop == 1) { tq.setBoost(10); } //method name
	    else if (pt_prop == 2) { tq.setBoost(5); } //parameter type name
	    else if (pt_prop == 3) { tq.setBoost(5); } //parameter name
	    else if (pt_prop == 4) { tq.setBoost(2.5f); } //method call name
	    else if (pt_prop == 5) { tq.setBoost(1.25f); } //type name
	    else if (pt_prop == 6) { tq.setBoost(0.625f); } //var name
	  }
	 else {
	    if ("bug_weighted".equals(searchstrategy)) {
	       if (pt_prop == 0) { tq.setBoost(2); }
	       else if (pt_prop == 1) { tq.setBoost(1); }
	       else if (pt_prop == 2) { tq.setBoost(0.5f); }
	       else if (pt_prop == 3) { tq.setBoost(0.25f); }
	     }
	    else if ("ctxt_weighted".equals(searchstrategy)) {
	       if (pt_prop == 0) { tq.setBoost(0.5f); }
	       else if (pt_prop == 1) { tq.setBoost(1); }
	       else if (pt_prop == 2) { tq.setBoost(0.5f); }
	       else if (pt_prop == 3) { tq.setBoost(0.25f); }
	     }
	  }
	 bq.add(tq,BooleanClause.Occur.SHOULD); //May use *BooleanClause.Occur.MUST* for certain tokens (e.g., API calls)
       }

      q = bq;

      List<SearchResult> rslt = cs.getEngine().search(q,max);
      for (SearchResult r : rslt) {
	 xw.begin("FILE");
	 xw.field("SCORE",r.getScore());
	 xw.field("MLOC",r.getFileLoc());
	 xw.text(r.getFilePath());
	 xw.end("FILE");
       }
      IvyLog.logI("COCKER","---END----------------------------------");
   }



}	// end of inner class CockerHandleRequestCallback




/********************************************************************************/
/*										*/
/*	Periodic update task							*/
/*										*/
/********************************************************************************/

private class Updater extends TimerTask {

   Updater() { }

   @Override public void run() {
      ServerOperation op = new CockerOperation.Update();
      getOperationsManager().synchronousOperation(op);
    }

}	// end of inner class Updater

} // end of class CockerServer


/* end of CockerServer.java */
