/********************************************************************************/
/*										*/
/*		ClientConnectionInformation.java				*/
/*										*/
/*	Holder for infromation about a client connection to a server		*/
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

public class ServerConnectionInformation {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private String cci_hostname;
private int    cci_port;
private int    cci_timeout;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ServerConnectionInformation(String hostname,int port,int timeout)
{
   cci_hostname = hostname;
   cci_port = port;
   cci_timeout = timeout;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getHostname()
{
   return cci_hostname;
}


public int getPort()
{
   return cci_port;
}


public int getTimeout()
{
   return cci_timeout;
}


public void setHostname(String hostname)
{
   this.cci_hostname = hostname;
}


public void setPort(int port)
{
   this.cci_port = port;
}


public void setTimeout(int timeout)
{
   this.cci_timeout = timeout;
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/


@Override public String toString()
{
   return "@" + getHostname() + " on " + getPort() + " [" + getTimeout() + "]";
}




/********************************************************************************/
/*										*/
/*	Equality and hashing methods						*/
/*										*/
/********************************************************************************/

@Override public boolean equals(Object otherObject)
{
   if (otherObject instanceof ServerConnectionInformation) {
      ServerConnectionInformation otherConnectionInformation = (ServerConnectionInformation) otherObject;
      return this.getHostname() == otherConnectionInformation.getHostname()
	       && this.getPort() == otherConnectionInformation.getPort()
	       && this.getTimeout() == otherConnectionInformation.getTimeout();
    }
   else {
      return false;
    }
}

@Override public int hashCode()
{
   return getHostname().hashCode() + Integer.toString(getPort()).hashCode()
	    + Integer.toString(cci_timeout).hashCode();
}



}	// end of class ClientConnectionInformation




/* end of ClientConnectionInformation.java */
