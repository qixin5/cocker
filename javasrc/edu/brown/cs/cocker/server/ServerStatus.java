/********************************************************************************/
/*										*/
/*		ServerStatus.java						*/
/*										*/
/*	Representation of the status of a server				*/
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

import edu.brown.cs.cocker.pandora.IvyElapsedTime;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

class ServerStatus {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/


private boolean is_running;
private int	server_port;
private long	server_uptime;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ServerStatus(long uptime,boolean isrunning,int port)
{
   server_uptime = uptime;
   is_running = isrunning;
   server_port = port;
}


/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   StringBuilder resultBuilder = new StringBuilder("Server is ");
   resultBuilder.append((is_running ? "[Running" : "[NOT Running"));
   resultBuilder.append(" on Port ");
   resultBuilder.append(server_port);
   resultBuilder.append("]");
   resultBuilder.append(" Uptime: ");
   resultBuilder.append(new IvyElapsedTime(server_uptime).toString(true));
   return resultBuilder.toString();
}


void toXml(IvyXmlWriter xw)
{
   xw.begin("STATUS");
   xw.field("RUNNING", is_running);
   xw.field("PORT", server_port);
   xw.field("UPTIME", server_uptime);
   xw.text(toString());
   xw.end("STATUS");
}

}	// end of class ServerStatus





/* end of ServerStatus.java */
