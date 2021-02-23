/********************************************************************************/
/*										*/
/*		EngineSession.java						*/
/*										*/
/*	Session manager for index/search engine tasks				*/
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
import java.io.IOException;

import edu.brown.cs.cocker.search.SearchContext;
import edu.brown.cs.cocker.search.SearchProvider;
import edu.brown.cs.cocker.server.ServerConstants;
import edu.brown.cs.cocker.server.ServerFileChangeBroadcaster;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

class CockerSession implements ServerConstants {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private boolean 	     auto_checkpoint;
private ServerFileChangeBroadcaster fscb_broadcaster;
private int			 checkpoint_interval;
private boolean 	     is_open;
private FileChangeListener	  search_listener;
private int			 modifications_since_checkpoint;
private SearchContext	       search_context;
private SearchProvider	      search_provider;



/********************************************************************************/
/*										*/
/*	Consructors								*/
/*										*/
/********************************************************************************/

CockerSession(SearchProvider searchProvider)
{
   search_listener = new MonitoredFileChangedListener();
   search_provider = searchProvider;
   is_open = false;
   auto_checkpoint = false;
   checkpoint_interval = 1000;
}



/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

private void addFileToIndex(ServerFile file)
{
   try {
      search_context.addFileToIndex(file);
    }
   catch (IOException ioe) { }
}

void blacklistFile(File file) throws IOException
{
   validateSession();
   fscb_broadcaster.addToBlackList(file);

   if (auto_checkpoint) incrementModificationsSinceCheckpoint();
}


private void checkpoint()
{
   try {
      System.out.println("CHECKPOINT");
      commit();
      modifications_since_checkpoint = 0;
      open();
    }
   catch (IOException ioe) { }
}

void commit() throws IOException
{
   if (is_open) {
      commitSearchContext();
      commitHibernate(); // TODO: we need a 2 phase commit...
      is_open = false;
    }
}


private void commitHibernate() throws IOException
{
   fscb_broadcaster.commit();
   fscb_broadcaster.removeFileChangeListener(search_listener);
   fscb_broadcaster = null;
}

private void commitSearchContext()
{
   try {
      search_context.commitContext();
      search_context = null;
    }
   catch (IOException e) {
      System.err.println("COCKER: Problem doing commit: " + e);
    }
}

private void incrementModificationsSinceCheckpoint()
{
   modifications_since_checkpoint++;
   if (modifications_since_checkpoint > checkpoint_interval) checkpoint();
}

void monitorFile(File file) throws IOException
{
   validateSession();
   fscb_broadcaster.trackFile(file);
}


void open() throws IOException
{
   if (!is_open) {
      openHibernate();
      search_context = search_provider.openContext();
      is_open = true;
    }
}

private void openHibernate()
{
   fscb_broadcaster = ServerFileChangeBroadcaster.getFscb();
   fscb_broadcaster.addFileChangeListener(search_listener);
}

private void removeFileFromIndex(ServerFile file)
{
   try {
      search_context.removeFileFromIndex(file);
    }
   catch (IOException e) {
      System.err.println("COCKER: Problem removing file: " + e);
    }
}

void rollback() throws IOException
{
   if (is_open) {
      rollbackSearchContext();
      rollbackHibernate();
      is_open = false;
    }
}

private void rollbackHibernate()
{
   fscb_broadcaster.rollback();
   fscb_broadcaster.removeFileChangeListener(search_listener);
   fscb_broadcaster = null;
}

private void rollbackSearchContext()
{
   try {
      search_context.rollbackContext();
      search_context = null;
    }
   catch (IOException e) {
      System.err.println("COCKER: Problem with rollback: " + e);
    }
}



void synchronizeIndex() throws IOException
{
   validateSession();
   fscb_broadcaster.synchronize();
}

void unblacklistFile(File file) throws IOException
{
   validateSession();
   fscb_broadcaster.removeFromBlackList(file);

   if (auto_checkpoint) incrementModificationsSinceCheckpoint();
}

void unmonitorFile(File file) throws IOException
{
   validateSession();
   fscb_broadcaster.untrackFile(file);
}


void showFiles(IvyXmlWriter xw) throws IOException
{
   validateSession();
   fscb_broadcaster.showFiles(xw);
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private void updateFileInIndex(ServerFile file)
{
   try {
      search_context.updateFileInIndex(file);
    }
   catch (IOException ioe) { }
}


private void validateSession() throws IOException
{
   if (!is_open) throw new IOException("Session is closed.");
}



private class MonitoredFileChangedListener implements FileChangeListener {

   @Override public void fileCreated(FileChangeEvent filechange) {
      if (filechange.getFile().isFile()) addFileToIndex(filechange.getFile());
      if (auto_checkpoint) incrementModificationsSinceCheckpoint();
    }

   @Override public void fileDeleted(FileChangeEvent fileChange) {
      //if (filechange.getFile().isFile()) removeFileFromIndex(fileChange.getFile());
      removeFileFromIndex(fileChange.getFile());
      if (auto_checkpoint) incrementModificationsSinceCheckpoint();
    }

   @Override public void fileModified(FileChangeEvent fileChange) {
      if (fileChange.getFile().isFile()) updateFileInIndex(fileChange.getFile());
      if (auto_checkpoint) incrementModificationsSinceCheckpoint();
    }

}	// end of inner class MonitoredFileChangeListener


}	// end of class EngineSession




/* end of EngineSession.java */
