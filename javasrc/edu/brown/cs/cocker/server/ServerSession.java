/********************************************************************************/
/*										*/
/*		Session.java							*/
/*										*/
/*	Server session representation :: handle commands for a user		*/
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlReader;

public class ServerSession implements ServerConstants {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private String	server_hostname;
private int	server_port;
private Socket	server_socket;
private int	session_timeout;
private Writer	server_writer;
private IvyXmlReader xml_reader;
private boolean is_open;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ServerSession(String serverhostname,int serverport,int timeout) throws IOException
{
   server_hostname = serverhostname;
   server_port = serverport;
   session_timeout = timeout;
   open();
}



public ServerSession(ServerConnectionInformation cci) throws IOException
{
   this(cci.getHostname(),cci.getPort(),cci.getTimeout());
}



/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

public void close()
{
   if (is_open) {
      try {
	 // sendRequest("<COMMAND CMD='STOP' />");
	 server_writer.close();
	 xml_reader.close();
	 server_socket.close();
       }
      catch (IOException e) {}
      // catch (MalformedMessageException e) {}
    }
   server_socket = null;
}


private void open() throws IOException
{
   if (!is_open) {
      if (server_socket == null) {
	 try {
	    server_socket = new Socket(server_hostname,server_port);
	    server_socket.setSoTimeout(session_timeout);
	    server_writer = new OutputStreamWriter(server_socket.getOutputStream());
	    xml_reader = new IvyXmlReader(server_socket.getInputStream());
	  }
	 catch (IOException ioe) {
	    server_socket = null;
	    throw ioe;
	  }
	 is_open = true;
       }
    }
}


public Element sendRequest(String rqst) throws IOException, MalformedMessageException
{
   if (!isOpen()) throw new IOException("Session must be open in order to send requests.");

   server_writer.write(rqst);
   server_writer.flush();

   String rslttxt = xml_reader.readXml();
   if (rslttxt == null) return null;

   return IvyXml.convertStringToXml(rslttxt);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public boolean isOpen()
{
   return is_open;
}


}	// end of class Session




/* end of Session.java */
