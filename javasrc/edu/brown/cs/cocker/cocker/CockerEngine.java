/********************************************************************************/
/*										*/
/*		Engine.java							*/
/*										*/
/*	Engine to handle execution tasks for indexing and searching		*/
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
import java.util.List;

import edu.brown.cs.cocker.search.SearchProvider;
import edu.brown.cs.cocker.search.SearchResult;
import edu.brown.cs.cocker.server.ServerFileChangeBroadcaster;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import org.apache.lucene.search.Query;
/**
 * This is a very simplistic implementation of an execution engine for labrador.
 * This will carry out all the tasks associated with searching and indexing in a
 * consistent manner. In a very basic sense this class is transactional merge of
 * {@link ServerFileChangeBroadcaster}s and {@link SearchProvider}s. It ensures
 * that operations performed using either will be consistent with the other.
 * <p>
 * Engines have {@link CockerSession} based operations and non-{@link CockerSession} based
 * operations. {@link CockerSession}s are transactional. Most operations that require
 * a {@link CockerSession} are operations that involve multiple sub operations and
 * complex interlinking between the {@link ServerFileChangeBroadcaster} and the
 * {@link SearchProvider}. Common {@link CockerSession} operations have helpful
 * shortcuts that hide the internal {@link CockerSession} management; though it is
 * completely possible and in some cases preferable to perform your own session
 * management. Additionally {@link CockerEngine}s provide helper functions for
 * executing {@link CockerSession} based operations via callbacks. This allows
 * developers to have the {@link CockerEngine} manage {@link CockerSession}s for them but
 * implement their own operations.
 *
 * @author jtwebb
 *
 */
class CockerEngine {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private SearchProvider	   search_provider;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CockerEngine() throws IOException
{
   search_provider = SearchProvider.getProvider();
}



/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/
void blacklistFile(File file) throws IOException
{
   BlacklistFileCallback fcb = new BlacklistFileCallback(file);
   executeOperationInSession(fcb);
}


void unblacklistFile(File file) throws IOException
{
   UnblacklistFileCallback ucb = new UnblacklistFileCallback(file);
   executeOperationInSession(ucb);
}


void monitorFile(File file) throws IOException
{
   MonitorFileCallback mcb = new MonitorFileCallback(file);
   executeOperationInSession(mcb);
}


void unmonitorFile(File file) throws IOException
{
   UnmonitorFileCallback ufcb = new UnmonitorFileCallback(file);
   executeOperationInSession(ufcb);
}



void createIndex() throws IOException
{
   ServerFileChangeBroadcaster fscb = ServerFileChangeBroadcaster.getFscb();
   fscb.clear();
   getSearchProvider().createIndex();
}



void deleteIndex() throws IOException
{
   ServerFileChangeBroadcaster fscb = ServerFileChangeBroadcaster.getFscb();
   fscb.clear();
   getSearchProvider().deleteIndex();
}


void synchronizeIndex() throws IOException
{
   SynchronizeIndexCallback scb = new SynchronizeIndexCallback();
   executeOperationInSession(scb);
}



void optimizeIndex() throws IOException
{
   getSearchProvider().optimizeIndex();
}



List<SearchResult> search(String searchString,int max) throws IOException
{
   return getSearchProvider().search(searchString,max);
}


List<SearchResult> search(Query query,int max) throws IOException
{
   return getSearchProvider().search(query,max);
}
   


void showFiles(IvyXmlWriter xw) throws IOException
{
   ShowFilesCallback fcb = new ShowFilesCallback(xw);
   executeOperationInSession(fcb);
}




/********************************************************************************/
/*										*/
/*	Action related methods							*/
/*										*/
/********************************************************************************/

private CockerSession createSession()
{
   return new CockerSession(getSearchProvider());
}



private void executeOperationInSession(EngineOperationCallback callback)
	throws IOException
{
   boolean succeeded = false;
   CockerSession session = createSession();
   try {
      session.open();
      callback.executeInSession(session);
      succeeded = true;
    }
   catch (IOException ioe) {
      throw ioe;
    }
   catch (Throwable t) {
      IvyLog.logE("COCKER","Problem with operation",t);
    }
   finally {
      if (succeeded) {
	 session.commit();
       }
      else {
	 session.rollback();
       }
    }
}


SearchProvider getSearchProvider()
{
   return search_provider;
}



/********************************************************************************/
/*										*/
/*	Operations to perform asynchronously					*/
/*										*/
/********************************************************************************/

private interface EngineOperationCallback {

   public abstract void executeInSession(CockerSession session) throws IOException;

}	// end of interface EngineOperationCallback




private static abstract class BasicFileOperationCallback implements EngineOperationCallback {

   private File the_file;

/**
 * Creates a new {@link BasicFileOperationCallback} with the specified
 * {@link File}.
 *
 * @param file
 *	      The {@link File} used by this {@link File} based
 *	      {@link CockerSession} operation.
 */
   BasicFileOperationCallback(File file) {
      the_file = file;
    }

/**
 * Perform a {@link CockerSession} based operation using an open and
 * functional {@link CockerSession}.
 *
 * @param session
 *	      The {@link CockerSession} to use for the operation.
 * @throws IOException
 *	       This method allows for {@link IOException}s to be thrown
 *	       in case there is an error with the performed operation.
 * @throws EngineException
 *	       This method allows for {@link EngineException}s to be
 *	       thrown in case there is an error with the performed
 *	       operation.
 */
   @Override public abstract void executeInSession(CockerSession session) throws IOException;

   protected File getFile()			{ return the_file; }

}


private class BlacklistFileCallback extends BasicFileOperationCallback {

   BlacklistFileCallback(File f) {
      super(f);
    }

   @Override public void executeInSession(CockerSession session) throws IOException {
      session.blacklistFile(getFile());
    }

}	// end of inner class BlacklistFileCallback


private class MonitorFileCallback extends BasicFileOperationCallback {

   MonitorFileCallback(File f) {
      super(f);
    }

   @Override public void executeInSession(CockerSession session) throws IOException {
      session.monitorFile(getFile());
    }

}	// end of inner class MonitorFileCallbaqck

/**
 * This class provides a shortcut for synchronizing the index inside a
 * {@link CockerSession}.
 *
 * @author jtwebb
 *
 */
private class SynchronizeIndexCallback implements EngineOperationCallback {

   @Override public void executeInSession(CockerSession session) throws IOException {
      session.synchronizeIndex();
    }

}	// end of inner calss SynchronizeIndexCallback



private class UnblacklistFileCallback extends BasicFileOperationCallback {

   UnblacklistFileCallback(File f) {
      super(f);
    }

   @Override public void executeInSession(CockerSession session) throws IOException {
      session.unblacklistFile(getFile());
    }

}	// end of inner class UnblacklistFileCallback


private class UnmonitorFileCallback extends BasicFileOperationCallback {

   UnmonitorFileCallback(File f) {
      super(f);
    }

   @Override public void executeInSession(CockerSession session) throws IOException {
      session.unmonitorFile(getFile());
    }

}	// end of inner class UnmonitorFileCallback



private class ShowFilesCallback implements EngineOperationCallback {

   private IvyXmlWriter xml_writer; 
   
   ShowFilesCallback(IvyXmlWriter xw) {
      xml_writer = xw;
    }
   
   @Override public void executeInSession(CockerSession session) throws IOException {
      session.showFiles(xml_writer);
    }
   
}       // end of inner class ShowFilesCallback

}	// end of class Engine



/* end of Engine.java */
