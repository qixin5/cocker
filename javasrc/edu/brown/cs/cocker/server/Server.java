/********************************************************************************/
/*										*/
/*		Server.java							*/
/*										*/
/*	Generic implementaiton of a server					*/
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

package edu.brown.cs.cocker.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlReader;
import edu.brown.cs.ivy.xml.IvyXmlWriter;


public class Server implements ServerConstants {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private ServerRequestCallback   request_callback;
private boolean                 is_running;
private ServerOperationsManager operations_manager;
private int		        host_port;
private ServerSocket	        server_socket;
private ServerDispatchThread    server_thread;
private ExecutorService         thread_pool;
private int		        thread_pool_size;
private Date		        start_date;
private Date		        end_date;
private Properties	        server_properties;
private File			property_file;
private int                     web_clients;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public Server(int port,int threadpoolsize,ServerRequestCallback requestcallback)
{
   host_port = port;
   thread_pool_size = threadpoolsize;
   request_callback = requestcallback;
   operations_manager = new ServerOperationsManager(this);
   server_properties = new Properties();
   web_clients = DEFAULT_NUM_WEB_CLIENTS;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

protected RequestHandler retrieveRequestHandler(Socket clientSocket)
{
   return new RequestHandler(clientSocket);
}

public ServerStatus getStatus()
{
   return new ServerStatus(getUptime(),is_running,host_port);
}

protected void setIsRunning(boolean isrunning)
{
   is_running = isrunning;
   if (is_running) {
      start_date = new Date();
      end_date = null;
    }
   else {
      end_date = new Date();
    }
}


public long getUptime()
{
   if (start_date == null) {
      return 0;
    }
   else if (end_date == null) {
      return new Date().getTime() - start_date.getTime();
    }
   else {
      return end_date.getTime() - start_date.getTime();
    }
}

private class ServerDispatchThread extends Thread {

   private List<RequestHandler> firewall_clients;

   public ServerDispatchThread() {
      super("Cocker Server-Socket Dispatch Thread");
      firewall_clients = new ArrayList<>();
    }

   @Override
   public void run()
   {
      try {
         server_socket.setSoTimeout(1*60*1000);
       }
      catch (IOException e) { }
   
      for (int i = 0; i < web_clients; ++i) {
         setupFirewallClient();
       }
   
      while (is_running) {
         try {
            Socket clientSocket = server_socket.accept();
            RequestHandler handler = retrieveRequestHandler(clientSocket);
            thread_pool.execute(handler);
          }
         catch (IOException ioe) {}
         checkFirewallClients();
       }
    }

   private void setupFirewallClient() {
      try {
         Socket s = new Socket(WEB_HOST_NAME,host_port + WEB_HOST_PORT_DELTA);
         s.setSoTimeout(2*60*60*1000);
         RequestHandler hdlr = retrieveRequestHandler(s);
         if (hdlr != null) {
            hdlr.start();
            firewall_clients.add(hdlr);
          }
       }
      catch (IOException e) {
         System.err.println("COCKER: Firewall connection not running: " + e);
       }
    }

   private synchronized void checkFirewallClients() {
      int ct = 0;
      for (Iterator<RequestHandler> it = firewall_clients.iterator(); it.hasNext(); ) {
         RequestHandler hdlr = it.next();
         if (hdlr.isActive()) ++ct;
         else it.remove();
       }
      if (ct < web_clients) {
         System.err.println("COCKER: Reconnect to firewall " + (web_clients - ct));
         for (int i = ct; i < web_clients; ++i) {
            setupFirewallClient();
          }
       }
    }

}	// end of inner class ServerDispatchThread



public ServerOperationsManager getOperationsManager()
{
   return operations_manager;
}


public void setPort(int port)
{
   host_port = port;
   setProperty("port",host_port);
}

protected void setWebClients(int ct)
{
   web_clients = ct;
   setProperty("WebClients",web_clients);
}



protected void setProperties(File propertiesfile)
{
   property_file = propertiesfile;
   server_properties = new Properties();
   try {
      FileInputStream fis = new FileInputStream(property_file);
      server_properties.load(fis);
      fis.close();
    }
   catch (IOException e) { }
}


protected String getProperty(String key)
{
   return server_properties.getProperty(key);
}



protected long getLongProperty(String key,long dflt)
{
   String val = getProperty(key);
   try {
      if (val != null) {
	 return Long.parseLong(val);
       }
    }
   catch (NumberFormatException e) { }
   return dflt;
}


protected int getIntProperty(String key,int dflt)
{
   String val = getProperty(key);
   try {
      if (val != null) {
	 return Integer.parseInt(val);
       }
    }
   catch (NumberFormatException e) { }
   return dflt;
}



protected void setProperty(String key,String value)
{
   server_properties.setProperty(key,value);
   if (property_file != null) {
      try {
	 FileWriter fw = new FileWriter(property_file);
	 server_properties.store(fw,"Cocker Server Properties");
	 fw.close();
       }
      catch (IOException e) { }
    }
}


protected void setProperty(String key,long value)
{
   setProperty(key,Long.toString(value));
}


protected void setRequestCallback(ServerRequestCallback cb)
{
   request_callback = cb;
}


protected ServerRequestCallback getRequestCallback()
{
   return request_callback;
}


public void start() throws IOException
{
   ServerFileChangeBroadcaster.getFscb();

   if (is_running) return;
   server_socket = new ServerSocket(host_port,10);
   if (host_port == 0) {
      host_port = server_socket.getLocalPort();
      setProperty("port",host_port);
    }
    
   setIsRunning(true);

   thread_pool = Executors.newFixedThreadPool(thread_pool_size);
   server_thread = new ServerDispatchThread();
   server_thread.start();
}



public void stop()
{
   if (server_thread != null && is_running) {
      try {
	 setIsRunning(false);
	 server_socket.close();
	 server_thread.interrupt();
	 thread_pool.shutdown();
	 System.exit(0);
       }
      catch (IOException ioe) {}
    }
}


/********************************************************************************/
/*										*/
/*	Handle clients								*/
/*										*/
/********************************************************************************/

private class RequestHandler extends Thread implements ServerConstants {

   private Socket	client_socket;
   private boolean	session_open;
   private IvyXmlReader xml_reader;
   private Writer	output_writer;

   RequestHandler(Socket clientsocket) {
      super("Firewall_" + clientsocket);
      client_socket = clientsocket;
      session_open = false;
      try {
	 client_socket.setSoTimeout(60000);
	 xml_reader = new IvyXmlReader(client_socket.getInputStream());
	 output_writer = new OutputStreamWriter(client_socket.getOutputStream());
       }
      catch (IOException e) { }
    }

   boolean isActive() {
      return session_open;
    }

   private Element getRequest() throws IOException {
      String xml = xml_reader.readXml();
      if (xml == null) return null;
      System.err.println("COCKER: REPLY: " + xml);
      return IvyXml.convertStringToXml(xml);
    }

   private void writeResponse(String resp) throws IOException {
      output_writer.write(resp);
      output_writer.flush();
    }

   public void run() {
      // TODO what happens if the client times out?
      session_open = true;

      // keep reading messages while the session is open
      while (session_open) {
	 Element request = null;
	 IvyXmlWriter xw = new IvyXmlWriter();
	 try {
	    for ( ; ; ) {
	       request = getRequest(); // try to read
	       if (request == null || !IvyXml.isElement(request,"PING")) break;
	       writeResponse("<PONG/>\n");
	       System.err.println("COCKER: Handled PING");
	     }
	  }
	 catch (IOException ioe) {
	    System.err.println("COCKER: I/O error on request: " + ioe);
	    break;
	  }
	 if (request == null) break;

	 try {
	    xw.begin("RESULT");
	    request_callback.handleMessage(request,xw,Server.this);
	    xw.end("RESULT");
	  }
	 catch (Throwable t) {
	    t.printStackTrace();
	    xw = new IvyXmlWriter();
	    xw.begin("ERROR");
	    xw.textElement("FAULT",t.toString());
	    xw.end("ERROR");
	  }

	 try {
	    writeResponse(xw.toString());
	  }
	 catch (IOException ioe) {
	    break;
	  }
	 shutdown();
       }

      // no more session so quit
      shutdown();
    }

   protected void shutdown() {
      try {
	 session_open = false;
	 client_socket.close();
       }
      catch (IOException ioe) {
	 System.err.println("I/O error on close: " + ioe);
       }
    }

}	// end of inner class RequestHandler



}	// end of class Server




/* end of Server.java */
