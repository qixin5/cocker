/********************************************************************************/
/*										*/
/*		OperationsManager.java						*/
/*										*/
/*	Controller to run operations in backgrounnd				*/
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


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerOperationsManager {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private boolean 	is_running;
private Server		owning_server;
private ExecutorService thread_queue;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ServerOperationsManager(Server server)
{
   owning_server = server;
   is_running = false;
}


/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

private void start() {
   if (!is_running) {
      thread_queue = Executors.newSingleThreadExecutor();
      is_running = true;
    }
}



public void synchronousOperation(ServerOperation operation)
{
   if (!is_running) start();
   OperationsHandler handler = new OperationsHandler(operation);
   thread_queue.execute(handler);
}



/********************************************************************************/
/*										*/
/*	Class to run the operation						*/
/*										*/
/********************************************************************************/

class OperationsHandler implements Runnable {


   private ServerOperation		the_operation;

   OperationsHandler(ServerOperation operation) {
      the_operation = operation;
    }

   @Override public void run() {
      the_operation.evaluate(owning_server);
    }

}	// end of inner class OperationsHandler



}	// end of class OperationsManager





/* end of OperationsManager.java */
