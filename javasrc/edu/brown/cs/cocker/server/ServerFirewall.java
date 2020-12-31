/********************************************************************************/
/*										*/
/*		EngineFirewall.java						*/
/*										*/
/*	Program to run on web server outside firewall to connect to S6 engine	*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2007, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/engine/EngineFirewall.java,v 1.8 2015/09/23 17:57:55 spr Exp $ */


/*********************************************************************************
 *
 * $Log: EngineFirewall.java,v $
 * Revision 1.8  2015/09/23 17:57:55  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2015/02/14 19:40:10  spr
 * Add test case generation.
 *
 * Revision 1.6  2013/09/13 20:32:17  spr
 * Handle UI search.
 *
 * Revision 1.5  2013-05-09 12:26:16  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.4  2009-05-12 22:27:23  spr
 * Add server to finder.  Fix up firewall recovery.  Fix max thread setting.
 *
 * Revision 1.3  2008-11-12 13:51:31  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-06-12 17:47:48  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.cocker.server;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import edu.brown.cs.ivy.xml.IvyXmlReader;
import edu.brown.cs.ivy.xml.IvyXmlReaderThread;



public class ServerFirewall {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   ServerFirewall ef = new ServerFirewall(args);

   ef.start();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Random		random_gen;
private Timer		ping_timer;
private List<CockerServer> all_servers;
private int		port_base;
private int		num_ports;
private int		port_delta;

private static final int	SOCKET_TIMEOUT = 2*60*60*1000;
private static final int	SOCKET_TIMEOUT_CHECK = 10*1000;

private static final long	PING_DELAY = 10*60*1000;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private ServerFirewall(String [] args)
{
   random_gen = new Random();

   all_servers = new ArrayList<CockerServer>();
   ping_timer = new Timer("ENGINE_PINGER",true);
   ping_timer.schedule(new ServerPinger(),PING_DELAY,PING_DELAY);

   port_base = 10263;
   num_ports = 12;
   port_delta = 1000;

   scanArgs(args);
}




/********************************************************************************/
/*										*/
/*	Argument methods							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-p") && i+1 < args.length) {
	    port_base = Integer.parseInt(args[++i]);
	  }
	 else if (args[i].startsWith("-n") && i+1 < args.length) {
	    num_ports = Integer.parseInt(args[++i]);
	  }
	 else if (args[i].startsWith("-d") && i+1 < args.length) {
	    port_delta = Integer.parseInt(args[++i]);
	  }
	 else badArgs();
       }
      else badArgs();
    }
}



private void badArgs()
{
   System.err.println("S6: ENGINE: s6firewall");
   System.exit(1);
}




/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void start()
{
   for (int i = 0; i < num_ports; ++i) {
      int wport = port_base + i;
      CockerServer cs = new CockerServer(wport);
      cs.start();
      ClientServer clnt = new ClientServer(cs,wport);
      clnt.start();
      all_servers.add(cs);
    }
}



/********************************************************************************/
/*										*/
/*	Server socket for talking to the engine 				*/
/*										*/
/********************************************************************************/

private class CockerServer extends Thread {

   private ServerSocket server_socket;
   private List<ServerClient> server_queue;
   private Set<ServerClient> active_clients;

   CockerServer(int port) {
      super("COCKER_SERVER_ACCEPT");
      try {
	 server_socket = new ServerSocket(port+port_delta);
       }
      catch (IOException e) {
	 System.err.println("COCKER: FIREWALL: Problem creating web server socket: " + e);
	 System.exit(1);
       }
      server_queue = new ArrayList<ServerClient>();
      active_clients = new HashSet<ServerClient>();
    }

   ServerClient getActiveClient() {
      synchronized (server_queue) {
	 for ( ; ; ) {
	    if (server_queue.size() > 0) {
	       return server_queue.remove(0);
	     }
	    else if (active_clients.size() > 0) {
	       try {
		  server_queue.wait();
		}
	       catch (InterruptedException e) { }
	     }
	    else return null;
	  }
       }
    }

   private void removeActive(ServerClient ec) {
      synchronized (server_queue) {
	 active_clients.remove(ec);
	 if (server_queue.size() == 0 && active_clients.size() == 0)
	    server_queue.notifyAll();
       }
    }

   void pingServerClients() {
      synchronized (server_queue) {
	 for (Iterator<ServerClient> it = server_queue.iterator(); it.hasNext(); ) {
	    ServerClient ec = it.next();
	    try {
	       ec.send(null);
	     }
	    catch (IOException e) {
	       System.err.println("COCKER: Ping failed: " + e);
	       it.remove();
	       ec.close();
	     }
	  }
       }
    }

   public void run() {
      try {
	 for ( ; ; ) {
	    Socket s = server_socket.accept();
	    setupServer(s);
	  }
       }
      catch (IOException e) {
	 System.err.println("S6: ENGINE: Problem with engine socket accept: " + e);
       }
    }

   private void setupServer(Socket s) {
      try {
	 ServerClient ec = new ServerClient(s);
	 makeActive(ec);
       }
      catch (IOException e) {
	 try {
	    s.close();
	  }
	 catch (IOException ex) { }
       }
    }

   private void makeActive(ServerClient ec) {
      synchronized (server_queue) {
	 int qsz = server_queue.size();
	 int where = random_gen.nextInt(qsz+1);
	 server_queue.add(where,ec);
	 active_clients.add(ec);
	 if (qsz == 0) server_queue.notifyAll();
       }
    }

}	// end of subclass WebServer



/********************************************************************************/
/*										*/
/*	Methods to handle waiting for clients					*/
/*										*/
/********************************************************************************/

private class ClientServer extends Thread {

   private CockerServer using_server;
   private ServerSocket server_socket;

   ClientServer(CockerServer cs,int port) {
      super("COCKER_CLIENT_ACCEPT");
      using_server = cs;
      try {
	 server_socket = new ServerSocket(port);
       }
      catch (IOException e) {
	 System.err.println("S6: FIREWALL: Problem creating client server socket: " + e);
	 System.exit(1);
       }
    }

   public void run() {
      try {
	 for ( ; ; ) {
	    Socket s = server_socket.accept();
	    setupClient(s);
	  }
       }
      catch (IOException e) {
	 System.err.println("S6: FIREWALL: Problem with client socket accept: " + e);
       }
    }

   private void setupClient(Socket s) {
      try {
	 FirewallClient c = new FirewallClient(s,using_server);
	 c.start();
       }
      catch (IOException e) {
	 System.err.println("S6: FIREWALL: Problem creating web client connection: " + e);
       }
    }

}	// end of subclass ClientServer




/********************************************************************************/
/*										*/
/*	Methods to handle client connections					*/
/*										*/
/********************************************************************************/

private class FirewallClient extends IvyXmlReaderThread {

   private Socket client_socket;
   private PrintWriter print_writer;
   private ServerClient engine_client;
   private CockerServer using_server;

   FirewallClient(Socket s,CockerServer cs) throws IOException {
      super("FirewallClient_" + s.getRemoteSocketAddress(),
	       new InputStreamReader(s.getInputStream()));
      System.err.println("S6: Starting engine client " + getName());
      using_server = cs;
      engine_client = null;
      client_socket = s;
      print_writer = new PrintWriter(s.getOutputStream());
    }

   protected void processXmlMessage(String msg) {
      System.err.println("COCKER: FIREWALL: Sending: " + msg);
      String rslt = null;
      for (int i = 0; i < 10; ++i) {
	 if (engine_client == null) engine_client = using_server.getActiveClient();
	 try {
	    if (engine_client != null) rslt = engine_client.send(msg);
	    break;
	  }
	 catch (IOException e) {
	    System.err.println("COCKER: I/O Error: " + e);
	    e.printStackTrace();
	    using_server.removeActive(engine_client);
	    engine_client.close();
	    engine_client = null;
	  }
       }
      if (rslt != null) {
	 rslt = rslt.trim();
	 print_writer.println(rslt);
       }
      print_writer.flush();
      System.err.println("S6: FIREWALL: Recieved: " + rslt);
    }

   protected synchronized void processDone() {
      System.err.println("S6: FIREWALL: Done");
      if (client_socket == null) return;
      try {
	 client_socket.close();
	 client_socket = null;
	 if (engine_client != null) using_server.makeActive(engine_client);
       }
      catch (IOException e) { }
    }

   protected void processIoError(IOException e) {
      System.err.println("S6: FIREWALL: XML reader error for " + getName() + ": " + e);
      if (engine_client != null) using_server.makeActive(engine_client);
    }

}	// end of subclass FirewallClient




/********************************************************************************/
/*										*/
/*	Subclass representing a connection to the cocker engine 	*/
/*										*/
/********************************************************************************/

private static class ServerClient {

   private Socket	engine_socket;
   private IvyXmlReader engine_reader;
   private PrintWriter	engine_writer;
   private boolean	in_use;

   ServerClient(Socket s) throws IOException {
      engine_reader = new IvyXmlReader(new InputStreamReader(s.getInputStream()));
      engine_writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
      engine_socket = s;
      in_use = false;
      System.err.println("ENGINE: FIREWALL: Server connected to " + s);
    }

   String send(String msg) throws IOException {
      if (in_use) return null;
      in_use = true;
      try {
	 String rslt = null;

	 if (engine_socket == null) throw new IOException("Socket closed");

	 System.err.println("FIREWALL: Try sending ping");
	 engine_socket.setSoTimeout(SOCKET_TIMEOUT_CHECK);
	 engine_writer.println("<PING/>");
	 engine_writer.flush();
	 if (engine_writer.checkError()) throw new IOException("Socket ping error");
	 rslt = engine_reader.readXml(true);
	 System.err.println("FIREWALL: Ping response: " + rslt);
	 if (rslt == null) throw new IOException("Socket error");
	 if (msg == null) return "OK";
	 if (engine_writer.checkError()) throw new IOException("Socket after ping error");

	 engine_socket.setSoTimeout(SOCKET_TIMEOUT);
	 engine_writer.println(msg);
	 engine_writer.flush();
	 if (engine_writer.checkError()) throw new IOException("Socket message error");
	 rslt = engine_reader.readXml();
	 if (rslt == null) throw new IOException("Null result");

	 return rslt;
       }
      finally {
	 in_use = false;
       }
    }

   void close() {
      System.err.println("ENGINE: FIREWALL: Disconnecting " + engine_socket);
      if (engine_socket != null) {
	 try {
	    engine_socket.close();
	  }
	 catch (IOException e) { }
       }
      engine_socket = null;
      engine_reader = null;
      engine_writer = null;
    }

}	// end of subclass ServerClient



/********************************************************************************/
/*										*/
/*	Class for pinging engine connections					*/
/*										*/
/********************************************************************************/

private class ServerPinger extends TimerTask {

   public void run() {
      for (CockerServer cs : all_servers) {
	 cs.pingServerClients();
       }
    }

}	// end of subclass ServerPinger


}	// end of ServerFirewall



/* end of ServerFirewall.java */
