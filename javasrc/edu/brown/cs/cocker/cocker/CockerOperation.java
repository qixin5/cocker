/********************************************************************************/
/*										*/
/*		CockerOperation.java						*/
/*										*/
/*	Definitions of cocker-server specific operations			*/
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.cocker.server.ServerOperation;
import edu.brown.cs.cocker.server.Server;

public abstract class CockerOperation extends ServerOperation
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected CockerOperation(String name) {
   super(name);
}



/********************************************************************************/
/*										*/
/*	Synchronize operation							*/
/*										*/
/********************************************************************************/

public static class Synchronize extends CockerOperation {

   Synchronize() {
      super("Synchronize");
    }

   @Override public void evaluate(Server s) {
      try {
         CockerServer cs = (CockerServer) s;
         cs.getEngine().synchronizeIndex();
       }
      catch (Exception e) { }
    }
}	// end of inner class Synchronize



/********************************************************************************/
/*										*/
/*	Optimize operation							*/
/*										*/
/********************************************************************************/

public static class Optimize extends CockerOperation {

   Optimize() {
      super("Optimize");
    }

   @Override public void evaluate(Server s) {
      try {
	 CockerServer cs = (CockerServer) s;
	 cs.getEngine().optimizeIndex();
       }
      catch (Exception e) { }
    }
}	// end of inner class Optimize



/********************************************************************************/
/*										*/
/*	Update operation							*/
/*										*/
/********************************************************************************/

public static class Update extends CockerOperation {

Update() {
   super("Update");
}

@Override public void evaluate(Server s) {
   try {
      CockerServer cs = (CockerServer) s;
      cs.getEngine().synchronizeIndex();
      cs.getEngine().optimizeIndex();
    }
   catch (Exception e) { }
}
}	// end of inner class Update



/********************************************************************************/
/*										*/
/*	Monitor operation							*/
/*										*/
/********************************************************************************/

public static class Monitor extends CockerOperation {

   private List<String> file_names;

   Monitor(List<String> files) {
      super("MonitorFiles");
      if (files != null) file_names = new ArrayList<String>(files);
      else file_names = null;
    }

   @Override public void evaluate(Server s) {
      try {
         CockerServer cs = (CockerServer) s;
         if (file_names != null) {
            for (String file : file_names) {
               cs.getEngine().monitorFile(new File(file));
             }
          }
       }
      catch (Exception e) { }
    }

}	// end of inner class Monitor



/********************************************************************************/
/*										*/
/*	Unmonitor operation							*/
/*										*/
/********************************************************************************/

public static class Unmonitor extends CockerOperation {

   private List<String> file_names;

   Unmonitor(List<String> files) {
      super("UnmonitorFiles");
      if (files != null) file_names = new ArrayList<String>(file_names);
      else file_names = null;
    }

   @Override public void evaluate(Server s) {
      try {
	 CockerServer cs = (CockerServer) s;
	 if (file_names != null) {
	    for (String file : file_names) {
	       cs.getEngine().unmonitorFile(new File(file));
	     }
	  }
       }
      catch (Exception e) { }
    }

}	// end of inner class Unmonitor



/********************************************************************************/
/*										*/
/*	Blacklist operation							*/
/*										*/
/********************************************************************************/

public static class Blacklist extends CockerOperation {

   private List<String> file_names;

   Blacklist(List<String> files) {
      super("BlacklistFiles");
      if (files != null) file_names = new ArrayList<String>(files);
      else file_names = null;
    }

   @Override public void evaluate(Server s) {
      try {
         CockerServer cs = (CockerServer) s;
         if (file_names != null) {
            for (String file : file_names) {
               cs.getEngine().blacklistFile(new File(file));
             }
          }
       }
      catch (Exception e) { }
    }

}	// end of inner class Blacklist



/********************************************************************************/
/*										*/
/*	Whitelist operation							*/
/*										*/
/********************************************************************************/

public static class Whitelist extends CockerOperation {

   private List<String> file_names;

   Whitelist(List<String> files) {
      super("WhitelistFiles");
      if (files != null) file_names = new ArrayList<String>(files);
      else file_names = null;
    }

   @Override public void evaluate(Server s) {
      try {
	 CockerServer cs = (CockerServer) s;
	 if (file_names != null) {
	    for (String file : file_names) {
	       cs.getEngine().unblacklistFile(new File(file));
	     }
	  }
       }
      catch (Exception e) { }
    }

}	// end of inner class Whitelist





}	// end of class CockerOperation




/* end of CockerOperation.java */
