/********************************************************************************/
/*										*/
/*		Operation.java							*/
/*										*/
/*	Abstract operation definition plus basic operation implementations	*/
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

public abstract class ServerOperation {


/********************************************************************************/
/*										*/
/*     Private storage								*/
/*										*/
/********************************************************************************/

private String operation_name;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/


protected ServerOperation(String name) {
   operation_name = name;
}



/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

abstract public void evaluate(Server server);



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public String toString() {
   return "Operation: " + operation_name;
}



/********************************************************************************/
/*										*/
/*	Particular operations							*/
/*										*/
/********************************************************************************/

static class StopWhenDone extends ServerOperation {

   StopWhenDone() {
      super("StopWhenDone");
    }

   @Override public void evaluate(Server s) {
      try {
         Thread.sleep(5000);
       }
      catch (InterruptedException e) { }
      s.stop();
    }

}	// end of inner class StopWhenDone




}	// end of class Operation




/* end of Operaiton.java */
