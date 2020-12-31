/********************************************************************************/
/*										*/
/*		HandleRequestCallback.java					*/
/*										*/
/*	Basic command handler.	Extend for more detailed commands		*/
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


import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class ServerRequestCallback implements ServerConstants {


public void handleMessage(Element xml,IvyXmlWriter xw,Server server)
	throws MalformedMessageException
{
   if (IvyXml.isElement(xml, "COMMAND")) {
      String cmd = IvyXml.getAttrString(xml,"CMD");
      if (cmd == null) throw new MalformedMessageException("No CMD given");
      switch (cmd) {
	 case "KILL":
	    server.stop();
	    break;
	 case "STATUS":
	    ServerStatus sts = server.getStatus();
	    sts.toXml(xw);
	    break;
	 case "EXIT" :
	 case "STOP":
	    // handle stop when done
	    ServerOperation op = new ServerOperation.StopWhenDone();
	    server.getOperationsManager().synchronousOperation(op);
	    break;
	 default:
	    throw new MalformedMessageException("Unknown command " + cmd);
       }
    }
   else throw new MalformedMessageException("Input was not a command");
}


}	// end of class HandleRequestCallback




/* end of HandleRequestCallback.java */
