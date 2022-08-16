/********************************************************************************/
/*										*/
/*		FileSystemChangeBroadcaster.java				*/
/*										*/
/*	Handler for traversing file system and noting changes			*/
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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.event.EventListenerList;

import edu.brown.cs.ivy.file.IvyDatabase;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import java.sql.PreparedStatement;

import edu.brown.cs.cocker.analysis.*;
import edu.brown.cs.cocker.util.ResourceFinder;


public class ServerFileChangeBroadcaster implements ServerConstants {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Set<File>			black_list;
private EventListenerList		file_change_listeners;
private SynchronizationVisitor		the_synchronizer;
private Set<ServerFileImpl>		top_level_files;
private Connection			db_connection;
private Map<String,Long>		known_synchronized;
private Map<String,Long>		pending_synchronized;

private static ServerFileChangeBroadcaster the_fscb	    = null;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public static synchronized ServerFileChangeBroadcaster getFscb()
{
   if (the_fscb == null) {
      the_fscb = new ServerFileChangeBroadcaster();
      the_fscb.loadData();
    }
   return the_fscb;
}


private ServerFileChangeBroadcaster()
{
   file_change_listeners = new EventListenerList();
   black_list = new LinkedHashSet<File>();
   top_level_files = new LinkedHashSet<ServerFileImpl>();
   known_synchronized = new HashMap<String,Long>();
   pending_synchronized = new HashMap<String,Long>();
   clear();

   String dbname = AnalysisConstants.Factory.getAnalysisType().getDatabaseName();
   ResourceFinder rf = new ResourceFinder("COCKER_HOME");
   rf.setDatabaseProps("Cocker");
   SQLException laste = null;
   boolean setup = false;
   for (int i = 0; i < 2; ++i) {
      try {
	 db_connection = IvyDatabase.openDatabase(dbname);
         if (setup) setupDatabase();
	 break;
       }
      catch (SQLException e) {
	 laste = e;
       }
      try {
         IvyLog.logI("SERVER","Creating initial database");
         Connection c = IvyDatabase.openDefaultDatabase();
         Statement statement = c.createStatement();
         String create = "CREATE DATABASE " + dbname;
         statement.executeUpdate(create);
         c.close();
         setup = true;
       }
      catch (SQLException e) {
         laste = e;
         break;
       }
    }

   if (db_connection == null) {
      IvyLog.logE("SERVER","Problem connecting to database: " + laste,laste);
    }
}



/********************************************************************************/
/*										*/
/*	Event management methods						*/
/*										*/
/********************************************************************************/

public void addFileChangeListener(FileChangeListener listener)
{
   file_change_listeners.add(FileChangeListener.class, listener);
}


public void removeFileChangeListener(FileChangeListener listener)
{
   file_change_listeners.remove(FileChangeListener.class, listener);
}



private void fireFileChangeCreated(ServerFile file)
{
   Object[] listeners = file_change_listeners.getListenerList();
   for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == FileChangeListener.class) {
	 ((FileChangeListener) listeners[i + 1]).fileCreated(new FileChangeEvent(file));
       }
    }
}



private void fireFileChangeDeleted(ServerFile file)
{
   Object[] listeners = file_change_listeners.getListenerList();
   for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == FileChangeListener.class) {
	 ((FileChangeListener) listeners[i + 1]).fileDeleted(new FileChangeEvent(file));
       }
    }
}



private void fireFileChangeModified(ServerFile file)
{
   Object[] listeners = file_change_listeners.getListenerList();
   for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == FileChangeListener.class) {
	 ((FileChangeListener) listeners[i + 1]).fileModified(new FileChangeEvent(file));
       }
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

private boolean isBlacklisted(File f)
{
   return black_list.contains(f);
}


public void addToBlackList(File f)
{
   if (black_list.add(f)) {
      update("BlackList",true,f);
    }
}


public void removeFromBlackList(File f)
{
   if (black_list.remove(f)) {
      update("BlackList",false,f);
    }
}





private File getProperFileHelper(File file)
{
   try {
      file = file.getCanonicalFile();
    }
   catch (IOException e) {
      file = file.getAbsoluteFile();
    }
   return file;
}




/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

public void clear()
{
   top_level_files.clear();
   the_synchronizer = new SynchronizationVisitor();
   update("WhiteList",false,null);
   update("BlackList",false,null);
   clearSynchronizations();
}



public void synchronize()
{
   the_synchronizer.reset();
   for (ServerFileImpl trackedfile : top_level_files)
      trackedfile.accept(the_synchronizer);
}




public void trackFile(File file)
{
   file = getProperFileHelper(file);

   if (isBlacklisted(file)) return;

   ServerFileImpl trackedfile = ServerFileImpl.create(file);
   if (trackedfile == null) return;

   if (top_level_files.add(trackedfile)) {
      update("WhiteList",true,file);
      trackedfile.accept(the_synchronizer);
    }

}



public void untrackFile(File file)
{
   file = getProperFileHelper(file);
   ServerFileImpl sfile = ServerFileImpl.create(file);
   if (sfile == null) return;

   UntrackingVisitor untracker = new UntrackingVisitor(sfile);
   for (Iterator<ServerFileImpl> iter = top_level_files.iterator(); iter.hasNext();) {
      ServerFileImpl trackedfile = iter.next();
      trackedfile.accept(untracker);
      if (trackedfile.getFile().equals(file)) {
	 update("WhiteList",false,file);
	 iter.remove();
       }
    }

}


public void showFiles(IvyXmlWriter xw)
{
   xw.begin("FILEDATA");
   for (ServerFileImpl sfi : top_level_files) {
      xw.begin("INCLUDE");
      xw.field("NAME",sfi.getPath());
      xw.end("INCLUDE");
    }
   for (File f1 : black_list) {
      xw.begin("EXCLUDE");
      xw.field("NAME",f1.getPath());
      xw.end("EXCLUDE");
    }
   xw.end("FILEDATA");
}




/********************************************************************************/
/*										*/
/*	Database Access methods 						*/
/*										*/
/********************************************************************************/

public void setupDatabase() throws SQLException
{
   if (db_connection == null) return;

   Statement st = db_connection.createStatement();
   String c1 = "CREATE TABLE BlackList ( file text PRIMARY KEY )";
   String c2 = "CREATE TABLE WhiteList ( file text PRIMARY KEY )";
   String c3 = "CREATE TABLE Files ( file text PRIMARY KEY, date bigint )";
   st.executeUpdate(c1);
   st.executeUpdate(c2);
   st.executeUpdate(c3);
}



public void clearDatabase() throws SQLException
{
   if (db_connection == null) return;

   Statement st = db_connection.createStatement();
   String c1 = "DELETE FROM TABLE BlackList";
   String c2 = "DELETE FROM TABLE WhiteList";
   String c3 = "DELETE FROM TABLE Files";
   st.executeUpdate(c1);
   st.executeUpdate(c2);
   st.executeUpdate(c3);
}



/********************************************************************************/
/*										*/
/*	Database access methods 						*/
/*										*/
/********************************************************************************/

void loadData()
{
   if (db_connection == null) return;

   try {
      Statement st = db_connection.createStatement();
      ResultSet rs;

      // Set up known_synchronized before tracking files
      String c3 = "SELECT * from Files";
      rs = st.executeQuery(c3);
      while (rs.next()) {
	 String fnm = rs.getString(1);
	 long dlm = rs.getLong(2);
	 known_synchronized.put(fnm,dlm);
       }

      // setup blacklist before looking at whitelisted files
      String c1 = "SELECT * from BlackList";
      rs = st.executeQuery(c1);
      while (rs.next()) {
	 String fnm = rs.getString(1);
	 black_list.add(new File(fnm));
       }

      String c2 = "SELECT * From WhiteList";
      rs = st.executeQuery(c2);
      while (rs.next()) {
	 String fnm = rs.getString(1);
	 File f1 = new File(fnm);
	 ServerFileImpl track = ServerFileImpl.create(f1);
	 if (track != null) top_level_files.add(track);
       }

      st.close();
    }
   catch (SQLException e) { }
}


long getLastSynchronized(String path)
{
   Long dlm = pending_synchronized.get(path);
   if (dlm == null) dlm = known_synchronized.get(path);
   if (dlm != null) return dlm;

   return -1;
}


private void clearSynchronizations()
{
   known_synchronized.clear();
   pending_synchronized.clear();
   try {
      if (db_connection != null) {
	 Statement st = db_connection.createStatement();
	 String c1 = "DELETE * FROM Files";
	 st.executeUpdate(c1);
	 st.close();
       }
    }
   catch (SQLException e) { }
}


void noteSynchronized(ServerFileImpl file)
{
   IvyLog.logD("SERVER","Note synchronized " + file);

   if (!file.isFile()) {
      if (file.getFile().isDirectory()) return;
      // record dlm of zip and tar files to avoid rescanning
    }

   String path = file.getPath();
   Long prevdlm = known_synchronized.get(path);
   long dlm = file.lastModified();
   if (prevdlm != null && dlm == prevdlm) return;
   pending_synchronized.put(path,dlm);
}



public void rollback()
{
   IvyLog.logI("SERVER","Rollback");
   pending_synchronized.clear();
}



public void commit()
{
   IvyLog.logI("SERVER","Begin commit " + pending_synchronized.size());
   if (pending_synchronized.isEmpty()) return;

   if (db_connection != null) {
      try {
	 PreparedStatement ps1 = db_connection.prepareStatement("INSERT INTO Files VALUES ( ?, ?)");
	 PreparedStatement ps2 = db_connection.prepareStatement("UPDATE Files SET date = ? WHERE file = ?");
	 int ct1 = 0;
	 int ct2 = 0;
	 for (Map.Entry<String,Long> ent : pending_synchronized.entrySet()) {
	    long dlm = ent.getValue();
	    String path = ent.getKey();
	    if (known_synchronized.containsKey(path)) {
	       ps2.setString(2,path);
	       ps2.setLong(1,dlm);
	       ps2.addBatch();
	       ++ct2;
	     }
	    else {
	       ps1.setString(1,path);
	       ps1.setLong(2,dlm);
	       ps1.addBatch();
	       ++ct1;
	     }
	  }
	 IvyLog.logD("SERVER","DB UPDATE " + ct1 + " " + ct2);
	 int [] sts = null;
	 if (ct1 > 0) sts = ps1.executeBatch();
	 for (int i = 0; i < ct1; ++i) {
	    IvyLog.logD("SERVER","COMMIT STATUS NEW: " + sts[i]);
	  }
	 if (ct2 > 0) sts = ps2.executeBatch();
	 for (int i = 0; i < ct2; ++i) {
	    IvyLog.logD("SERVER","COMMIT STATUS OLD: " + sts[i]);
	  }
       }
      catch (SQLException e) {
	IvyLog.logE("SERVER","COMMIT SQL EXCEPTION : " + e.getMessage() +
			       ", " + e.getSQLState() + ", " + e.getErrorCode());
	 Iterator<Throwable> iter = e.iterator();
	 while(iter.hasNext()) {
	    SQLException ee = (SQLException) iter.next();
	    IvyLog.logE("SERVER","COMMIT SQL EXCEPTION : " + ee.getMessage() +
				  ", " + ee.getSQLState() + ", " + ee.getErrorCode());
	  }
       }
      catch (Throwable e) {
	 IvyLog.logE("SERVER","Database problem",e);
	 e.printStackTrace();
       }
    }

   known_synchronized.putAll(pending_synchronized);
   pending_synchronized.clear();
}


private void update(String tbl,boolean add,File f)
{
   IvyLog.logI("SERVER","Update " + add + " " + tbl + " " + f + " " + db_connection);

   if (db_connection == null) return;

   String q = null;
   if (add) {
      q = "INSERT INTO " + tbl + " VALUES (" +
      "'" + f.getPath() + "' )";
    }
   else if (f == null) {
      q = "DELETE FROM " + tbl;
    }
   else {
      q = "DELETE FROM " + tbl + " WHERE file = '" + f.getPath() + "'";
    }

   try {
      Statement st = db_connection.createStatement();
      IvyLog.logD("SERVER","Database: " + q);
      int sts = st.executeUpdate(q);
      IvyLog.logD("SERVER","Update status " + sts);
      st.close();
    }
   catch (SQLException e) {
      IvyLog.logE("SERVER","Database problem: " + e,e);
    }
}









/********************************************************************************/
/*										*/
/*	Exploration Tree helper class for exploring the file system		*/
/*										*/
/********************************************************************************/


private static class ExplorationTree {

   private ExplorationTreeNode root_node;

   ExplorationTree() {
      clear();
    }

   boolean add(ServerFile file) {
      String fullpath = file.getPath();
      int idx = fullpath.indexOf(File.pathSeparator);
      String endpath = null;
      if (idx > 0) {
	 endpath = fullpath.substring(idx+1);
	 fullpath = fullpath.substring(0,idx);
       }
      String[] path = fullpath.split(File.separator);
      ExplorationTreeNode current = root_node;
      int i = 0;
      for (; i < path.length - 1; i++) {
	 if (!current.getSubnodes().containsKey(path[i]))
	    current.getSubnodes().put(path[i],new ExplorationTreeNode());
	 current = current.getSubnodes().get(path[i]);
       }
      boolean result = false;
      if (endpath == null) {
	 result = !current.getSubnodes().containsKey(path[i]);
	 if (result) current.getSubnodes().put(path[i], new ExplorationTreeNode());
       }
      else {
	 if (!current.getSubnodes().containsKey(path[i]))
	    current.getSubnodes().put(path[i],new ExplorationTreeNode());
	 current = current.getSubnodes().get(path[i]);
	 result = !current.getSubnodes().containsKey(endpath);
	 if (result) current.getSubnodes().put(endpath, new ExplorationTreeNode());
       }

      return result;
    }

   public void clear() {
      root_node = new ExplorationTreeNode();
    }

}	// end of inner class Explortation Tree



private static class ExplorationTreeNode {

   private Map<String,ExplorationTreeNode> subnodes;

   ExplorationTreeNode() {
      subnodes = new HashMap<String, ExplorationTreeNode>();
    }

   Map<String, ExplorationTreeNode> getSubnodes() {
      return subnodes;
    }

}   // end of inner class ExplorationTreeNode




/********************************************************************************/
/*										*/
/*	Synchronization visitor -- file system visitor to handle synchronization*/
/*										*/
/********************************************************************************/

private class SynchronizationVisitor implements FileSystemVisitor {

   private ExplorationTree   exploration_tracker;

   SynchronizationVisitor() {
      exploration_tracker = new ExplorationTree();
    }

   private boolean findNewFiles(ServerFileImpl dir) {
      boolean result = false;
      Iterable<ServerFileImpl> realfiles = dir.getSubfiles();
      if (realfiles != null) {
	 for (ServerFileImpl file : realfiles) {
	    if (!dir.doesTrack(file) && !isBlacklisted(file.getFile())) {
	       dir.addTrackedFile(file);
	       result = true;
	     }
	  }
       }
      return result;
    }

   void reset() {
      exploration_tracker.clear();

    }

   @Override public void visitFile(ServerFileImpl tf) {
      IvyLog.logD("SERVER","VisitF " + tf.getFile());
      if (!exploration_tracker.add(tf)) return;

      if (!tf.hasBeenSynchronized()) fireFileChangeCreated(tf);
      else if (!tf.isSynchronized()) fireFileChangeModified(tf);

      tf.synchronize();
      if (!tf.exists()) fireFileChangeDeleted(tf);
    }

   @Override public void visitContainer(ServerFileImpl dir) {
      IvyLog.logD("SERVER","VisitD " + dir.getFile());

      if (!exploration_tracker.add(dir)) return;

      if (dir.hasBeenSynchronized() && dir.isSynchronized()) return;

      DirectoryModificationListener modificationlistener = new DirectoryModificationListener();
      addFileChangeListener(modificationlistener);

      boolean foundnewdirectory = findNewFiles(dir);

      for (Iterator<ServerFileImpl> iter = dir.getTrackedFiles().iterator(); iter.hasNext();) {
	 ServerFileImpl trackedfile = iter.next();
	 trackedfile.accept(this);
	 if (!trackedfile.exists()) iter.remove();
       }

      removeFileChangeListener(modificationlistener);

      if (!dir.hasBeenSynchronized()) fireFileChangeCreated(dir);
      else if (foundnewdirectory || modificationlistener.changeEventFired()) fireFileChangeModified(dir);

      dir.synchronize();
      if (!dir.exists()) fireFileChangeDeleted(dir);
    }

}	// end of inner class SynchronizationVisitor




private static class DirectoryModificationListener implements FileChangeListener {

   private boolean	change_event_fired;

   DirectoryModificationListener() {
      change_event_fired = false;
    }

   boolean changeEventFired() {
      return change_event_fired;
    }

   @Override public void fileCreated(FileChangeEvent filechange) {
      change_event_fired = true;
    }

   @Override public void fileDeleted(FileChangeEvent filechange) {
      change_event_fired = true;
    }

   @Override public void fileModified(FileChangeEvent filechange) {
      change_event_fired = true;
    }

}	// end of inner class DirectoryModificationListener




/********************************************************************************/
/*										*/
/*	Visitor to handle untracking of files/directories			*/
/*										*/
/********************************************************************************/

private class UntrackingVisitor implements FileSystemVisitor {

   private boolean	   is_done;
   private ExplorationTree   exploration_tracker;
   private ServerFileImpl	file_to_untrack;

   UntrackingVisitor(ServerFileImpl file) {
      file_to_untrack = file;
      is_done = false;
      exploration_tracker = new ExplorationTree();
    }

   @Override public void visitFile(ServerFileImpl file)  { }

   @Override public void visitContainer(ServerFileImpl dir) {
      if (is_done) return;
      boolean wasnull = file_to_untrack == null;

      for (Iterator<ServerFileImpl> iter = dir.getTrackedFiles().iterator(); iter.hasNext();) {
	 ServerFileImpl trackedfile = iter.next();
	 if (!exploration_tracker.add(trackedfile)) continue;
	 if (trackedfile.equals(file_to_untrack) || file_to_untrack == null) {
	    file_to_untrack = null;
	    iter.remove();
	    fireFileChangeDeleted(trackedfile);
	    trackedfile.accept(this);
	    if (!wasnull) {
	       is_done = true;
	       return;
	     }
	  }
	 else {
	    trackedfile.accept(this);
	  }
       }
    }

}	// end of inner class UntrackingVisitor



}	// end of class FileSystemChangeBroadcasterBase




/* end of FileSystemChangeBroadcaster.java */
