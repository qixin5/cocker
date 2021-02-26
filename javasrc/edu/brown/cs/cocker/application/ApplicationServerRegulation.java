/********************************************************************************/
/*										*/
/*		ApplicationServerRegulation.java				*/
/*										*/
/*	Command line applciation to regulate the server 			*/
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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.cocker.server.ServerConnectionInformation;
import edu.brown.cs.cocker.server.ServerConstants;
import edu.brown.cs.cocker.server.ServerSession;
import edu.brown.cs.cocker.analysis.AnalysisConstants;
import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;


public class ApplicationServerRegulation implements ServerConstants, AnalysisConstants {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   ApplicationServerRegulation srcli = new ApplicationServerRegulation(args);
   srcli.execute();
}



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private List<String>		message_queue;
private boolean 		start_server;
private ServerConnectionInformation connection_info;
private ServerSession		client_session;
private int			memory_size;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ApplicationServerRegulation(String [] args)
{
   message_queue = new ArrayList<String>();
   start_server = false;
   connection_info = new ServerConnectionInformation(DEFAULT_HOSTNAME,0,DEFAULT_TIMEOUT);
   memory_size = 0;
   scanArgs(args);

   client_session = null;
}



/********************************************************************************/
/*										*/
/*	Argument processing							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   File datapath = null;
   boolean setport = true;
   
   for (int i = 0; i < args.length; ++i) {
      IvyXmlWriter xw = null;
      if (match1(args[i],"update") && i+2 < args.length) {           // -update <when> <interval>
	 try {
	    xw = new IvyXmlWriter();
	    xw.begin("COMMAND");
	    xw.field("CMD","UPDATETIME");
	    xw.field("WHEN",args[++i]);
	    long ival = Long.parseLong(args[++i]);
	    xw.field("INTERVAL",ival*1000*60);
	    xw.end("COMMAND");
	  }
	 catch (NumberFormatException e) {
	    badArgs();
	    xw = null;
	  }
       }
     else if (match(args[i],"idle") && i+1 < args.length) {             // -idle <interval>
        try {
           xw = new IvyXmlWriter();
           xw.begin("COMMAND");
           xw.field("CMD","IDLETIME");
           long ival = Long.parseLong(args[++i]);
           xw.field("INTERVAL",ival*1000*60);
           xw.end("COMMAND");
         }
        catch (NumberFormatException e) {
           badArgs();
           xw = null;
         }
      }
      else if (match1(args[i],"blacklist") && i+1 < args.length) {      // -blacklist <file...>
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","BLACKLIST");
	 while (i+1 < args.length && !args[i+1].startsWith("-")) {
	    xw.textElement("FILE",args[++i]);
	  }
	 xw.end("COMMAND");
       }
      else if (match1(args[i],"whitelist") && i+1 < args.length) {      // -whitelist <file...> (un-blacklist)
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","WHITELIST");
	 while (i+1 < args.length && !args[i+1].startsWith("-")) {
	    xw.textElement("FILE",args[++i]);
	  }
	 xw.end("COMMAND");
       }
      else if (match1(args[i],"monitor") && i+1 < args.length) {        // -monitor <file...>
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","MONITOR");
	 while (i+1 < args.length && !args[i+1].startsWith("-")) {
	    xw.textElement("FILE",args[++i]);
	  }
	 xw.end("COMMAND");
       }
      else if (match1(args[i],"omit") && i+1 < args.length) {           // -omit <file...> (un-monitor)
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","UNMONITOR");
	 while (i+1 < args.length && !args[i+1].startsWith("-")) {
	    xw.textElement("FILE",args[++i]);
	  }
	 xw.end("COMMAND");
       }
      else if (match1(args[i],"listing")) {                             // -listing
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","SCHEDULE");
	 xw.end("COMMAND");
       }
      else if (match1(args[i],"talk")) {                                // -talk :: status
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","STATUS");
	 xw.end("COMMAND");
       }
      else if (match(args[i],"stop")) {                                 // -stop
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","STOP");
	 xw.end("COMMAND");
       }
      else if (match1(args[i],"start")) {                               // -start
	 start_server = true;
       }
      else if (match(args[i],"kill")) {                                 // -kill
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","KILL");
	 xw.end("COMMAND");
       }
      else if (match1(args[i],"index")) {                               // -index
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","SYNCH");
	 xw.end("COMMAND");
       }
      else if (match(args[i],"opt")) {                                  // -optimize
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","OPTIMIZE");
	 xw.end("COMMAND");
       }
      else if (match(args[i],"ping")) {                                  // -ping
	 xw = new IvyXmlWriter();
	 xw.begin("PING");
	 xw.end("PING");
       }
      else if (match(args[i],"Update")) {                                  // -Update  
	 xw = new IvyXmlWriter();
	 xw.begin("COMMAND");
	 xw.field("CMD","UPDATE");
	 xw.end("COMMAND");
       }
      else if (args[i].startsWith("-n") && i+1 < args.length) {         // -name <hostname>
	 connection_info.setHostname(args[++i]);
       }
      else if (args[i].startsWith("-h") && i+1 < args.length) {         // -host <hostname>
	 connection_info.setHostname(args[++i]);
       }
      else if (args[i].startsWith("-p") && i+1 < args.length) {         // -port <port>
	 try {
	    connection_info.setPort(Integer.parseInt(args[++i]));
            setport = false;
	  }
	 catch (NumberFormatException e) { badArgs(); }
       }
      else if (args[i].startsWith("-d") && i+1 < args.length) {         // -dir <base>
         datapath = new File(args[++i]);
         AnalysisConstants.Factory.setIndexDirectory(datapath);
         connection_info.setDatapath(datapath);
         setport = false;
       }
      else if (args[i].startsWith("-a") && i+1 < args.length) {         // -analysis <type>
	 String type = args[++i];
	 AnalysisConstants.Factory.setAnalysisType(type);
       }
      else if (args[i].startsWith("-M") && i+1 < args.length) {         // -M <memory size in meg
	 try {
	    memory_size = Integer.parseInt(args[++i]);
	  }
	 catch (NumberFormatException e) { badArgs(); }
       }
      else badArgs();
      if (xw != null) {
	 System.err.println("COMMAND: " + xw.toString());
	 message_queue.add(xw.toString());
	 xw.close();
       }
    }
   
   if (setport && connection_info.getPort() == 0) {
      AnalysisType anal = AnalysisConstants.Factory.getAnalysisType();
      connection_info.setPort(anal.getDefaultPortNumber());
    }
}



private boolean match1(String arg,String a1)
{
   String a2 = a1.substring(0,1);
   return match(arg,a2);
}



private boolean match(String arg,String a2)
{
   if (arg.startsWith(a2)) return true;
   if (arg.startsWith("-" + a2)) return true;
   return false;
}



private void badArgs()
{
   System.err.println("COCKER: server ...");
   System.exit(1);
}




/********************************************************************************/
/*										*/
/*	Processing commands							*/
/*										*/
/********************************************************************************/

public void execute()
{
   if (start_server) {
      startServer();
      client_session = null;
      ensureConnectivity();
    }
   else {
      ensureConnectivity();
    }

   for (String msg : message_queue) {
      try {
	 Element resp = sendMessage(msg);
	 if (IvyXml.isElement(resp,"RESULT")) {
	    Element chld = IvyXml.getChild(resp,"STATUS");
	    if (chld != null) displayServerStatus(chld);
	    chld = IvyXml.getChild(resp,"SCHEDULE");
	    if (chld != null) displayServerSchedule(chld);

	    // check for status or list and display appropriate
	    System.out.println("STATUS: OK");
	  }
	 else if (resp == null) ;
	 else if (IvyXml.isElement(resp,"PONG")) {
	    System.out.println("STAUTS: PONG");
	  }
	 else {
	    System.out.println("STATUS: ERROR: " + IvyXml.getText(resp));
	  }
       }
      catch (IOException t) {
	 System.out.println("STATUS: IOEX: " + t);
	 t.printStackTrace();
	 break;
       }
      catch (Throwable t) {
	 System.out.println("STATUS: SYSRROR: " + t);
	 t.printStackTrace();
       }
    }
}


public Element sendMessage(String msg) throws IOException, MalformedMessageException
{
   if (client_session == null) throw new IOException("No connection");
   return client_session.sendRequest(msg);
}


public void displayServerSchedule(Element sked)
{
   System.out.println("Server Schedule:");
   for (Element jelt : IvyXml.children(sked,"JOB")) {
      System.out.println("Job: " + IvyXml.getAttrString(jelt,"NAME"));
      Date d = IvyXml.getAttrDate(jelt,"LASTRUN");
      System.out.println("\tLast Run: " + DateFormat.getDateInstance().format(d));
      System.out.println("\tUpdate Every " + (IvyXml.getAttrLong(jelt,"INTERVAL")/60/1000) + " minutes");
    }
}



public void displayServerStatus(Element sts)
{
   System.out.println("Current Server Status:");
   System.out.println(IvyXml.getText(sts));
}

public void ensureConnectivity()
{
   if (client_session == null || !client_session.isOpen()) {
      try {
	 client_session = new ServerSession(connection_info);
       }
      catch (IOException ioe) {
	 System.err.println("Could not connect to server. Please ensure that" +
	 " your connection information is correct, a server is" +
	 " running, and that you have network access to the server.");
	 System.err.println("Error: " + ioe.getMessage());
	 System.exit(1);
       }
    }
}


public void startServer()
{
   try {
      String cp = System.getProperty("java.class.path");
      StringBuffer cmd = new StringBuffer();

      if (!connection_info.getHostname().equals("localhost")) {
	 cmd.append("ssh " + connection_info.getHostname() + " ");
       }
      cmd.append("java");
      cmd.append(" -cp '" + cp + "'");
      if (memory_size > 0) {
	 cmd.append(" -Xmx" + memory_size + "m");
       }
      cmd.append(" edu.brown.cs.cocker.cocker.CockerServer");
      cmd.append(" -port " + connection_info.getPort());
      cmd.append(" -analysis " + AnalysisConstants.Factory.getAnalysisType().toString());
      if (connection_info.getDataPath() != null) {
         cmd.append(" -dir " + connection_info.getDataPath());
       }

      // cmd.append(" > /vol/cocker/server_" + AnalysisConstants.Factory.getAnalysisType().toString());

      // set thread pool size, timeout
      IvyLog.logI("APPLICATION","RUN SERVER: " + cmd);
      new IvyExec(cmd.toString());

      for (int i = 0; i < 10; ++i) {
	 try {
	    Thread.sleep(i*1000);
	  }
	 catch (InterruptedException e) { }
	 try {
            if (connection_info.getPort() == 0 && connection_info.getDataPath() != null) {
               File datapath = new File(connection_info.getDataPath());
               connection_info.setDatapath(datapath);   // updates port when known
             }
            if (connection_info.getPort() != 0) {
               client_session = new ServerSession(connection_info);
               client_session.close();
               return;
             }
	  }
	 catch (IOException e) { }
       }

      throw new IOException("Server not started");
    }
   catch (IOException ioe) {
      IvyLog.logE("APPLICATION","Could not start server: " + ioe);
    }
}


}	// end of class ApplicationServerRegulation



/* end of ApplciationServerRegulation.java */
