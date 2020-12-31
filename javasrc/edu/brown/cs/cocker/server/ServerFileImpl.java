/********************************************************************************/
/*										*/
/*		ServerFileImpl.java -- Represntation of a file being tracked 	*/
/*										*/
/*	Representaiton of a file being tracked by the system			*/
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

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.xeustechnologies.jtar.*;



abstract public class ServerFileImpl implements ServerConstants, ServerConstants.ServerFile {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private File	internal_file;
private boolean has_been_synchronized;
private long	last_synchronization;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected ServerFileImpl(File file)
{
   internal_file = getBestFilePath(file);
   last_synchronization = -1;
   has_been_synchronized = false;
}



/********************************************************************************/
/*										*/
/*	Setup methods   							*/
/*										*/
/********************************************************************************/

protected static File getBestFilePath(File file)
{
   if (file == null) return null;
   try {
      return file.getCanonicalFile();
    }
   catch (IOException ioe) { }
   return file;
}


protected void setupSynchronization()
{
   last_synchronization = ServerFileChangeBroadcaster.getFscb().getLastSynchronized(getPath());
   if (last_synchronization == -1) has_been_synchronized = false;
   else has_been_synchronized = true;
}



/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

abstract void accept(FileSystemVisitor visitor);



static ServerFileImpl create(File f)
{
   if (!f.exists() || !f.canRead()) return null;
   if (f.length() == 0) return null;
   
   try {
      if (f.isDirectory()) return new Directory(f);
      else if (f.getName().endsWith(".java")) return new SourceFile(f);
      else if (f.getName().endsWith(".tar")) return new TrackedTarFile(f);
      else if (f.getName().endsWith(".jar")) return new TrackedZipFile(f);
      else if (f.getName().endsWith(".zip")) return new TrackedZipFile(f);
    }
   catch (Exception e) { }

   return null;
}



/********************************************************************************/
/*										*/
/*	External interface							*/
/*										*/
/********************************************************************************/

public long lastModified()
{
   return getFile().lastModified();
}


public String getPath()
{
   return getFile().getAbsolutePath();
}


@Override public boolean isFile()
{
   return getFile().isFile();
}


public Reader getReader() throws IOException
{
    return new FileReader(getFile());
}


/********************************************************************************/
/*										*/
/*	Equality testing							*/
/*										*/
/********************************************************************************/

@Override public boolean equals(Object object)
{
   if (object instanceof File) {
      return getPath().equals(((File) object).getAbsolutePath());
    }
   else if (object instanceof ServerFileImpl) {
      return getPath().equals(((ServerFileImpl) object).getPath());
    }
   else return false;
}



@Override public int hashCode()
{
   return getPath().hashCode();
}


/********************************************************************************/
/*										*/
/*	Local access methods							*/
/*										*/
/********************************************************************************/

boolean exists()
{
   return getFile().exists();
}



File getFile()
{
   return internal_file;
}



boolean hasBeenSynchronized()
{
   return has_been_synchronized;
}



boolean isSynchronized()
{
   return last_synchronization == lastModified();
}


void synchronize()
{
   last_synchronization = lastModified();
   has_been_synchronized = true;
   ServerFileChangeBroadcaster.getFscb().noteSynchronized(this);
}


boolean doesTrack(ServerFileImpl f)
{
   return false;
}


void addTrackedFile(ServerFileImpl f)				    { }


Iterable<ServerFileImpl> getSubfiles()		
{
   return new ArrayList<ServerFileImpl>();
}


Iterable<ServerFileImpl> getTrackedFiles()
{
   return new ArrayList<ServerFileImpl>();
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return getPath();
}


/********************************************************************************/
/*										*/
/*	Implementation for a simple file					*/
/*										*/
/********************************************************************************/

static class SourceFile extends ServerFileImpl {

   SourceFile(File f) {
      super(f);
      setupSynchronization();
    }

   void accept(FileSystemVisitor visitor) {
      visitor.visitFile(this);
    }

}	// end of inner class SourceFile



/********************************************************************************/
/*										*/
/*	Generic container implementation					*/
/*										*/
/********************************************************************************/

abstract static class Container extends ServerFileImpl {

   private Set<ServerFileImpl> tracked_files;

   protected Container(File file) {
      super(file);
      tracked_files = new LinkedHashSet<ServerFileImpl>();
    }

   @Override void accept(FileSystemVisitor visitor) {
      visitor.visitContainer(this);
    }

   

   @Override boolean doesTrack(ServerFileImpl f) {
      return tracked_files.contains(f);
    }

   @Override void addTrackedFile(ServerFileImpl f) {
      if (f != null) tracked_files.add(f);
    }

   @Override Iterable<ServerFileImpl> getTrackedFiles()  {
      return tracked_files;
    }

   @Override public boolean isFile()            { return false; }
   
}	// end of inner class Directoryy



/********************************************************************************/
/*										*/
/*	Implementation for a directory						*/
/*										*/
/********************************************************************************/

static class Directory extends Container {

   Directory(File file) {
      super(file);
    }

   @Override Iterable<ServerFileImpl> getSubfiles() {
      List<ServerFileImpl> rslt = new ArrayList<ServerFileImpl>();
      File [] files = getFile().listFiles();
      if (files != null) {
         for (File f : getFile().listFiles()) {
            ServerFileImpl subfile = ServerFileImpl.create(f);
            if (subfile !=  null) rslt.add(subfile);
          }
       }
      return rslt;
    }
   
   @Override public boolean isSynchronized() {
      return false;
    }
   
}	// end of inner class Directoryy



/********************************************************************************/
/*										*/
/*	Implementation for a Tar file						*/
/*										*/
/********************************************************************************/

static class TrackedTarFile extends Container {

   TrackedTarFile(File file) {
      super(file);
      setupSynchronization();
    }

   @Override Iterable<ServerFileImpl> getSubfiles() {
      List<ServerFileImpl> rslt = new ArrayList<ServerFileImpl>();
      TarInputStream ins = null;
      try {
         ins = new TarInputStream(new BufferedInputStream(new FileInputStream(getFile())));
         TarEntry entry;
         while ((entry = ins.getNextEntry()) != null) {
            String nm = entry.getName();
            if (!entry.isDirectory() && nm.endsWith(".java")) {
               TrackedTarEntry ent = new TrackedTarEntry(entry,this);
               rslt.add(ent);
             }
          }
       }
      catch (Exception e) {
         System.err.println("COCKER: Problem with tar file " + getFile());
       }
      finally {
         try {
            if (ins != null) ins.close();
          }
         catch (IOException e) { }
       }
      return rslt;
    }

}	// end of inner class TrackedTarFile




static class TrackedTarEntry extends ServerFileImpl {

   private String entry_name;
   private long   last_modified;

   TrackedTarEntry(TarEntry ent,TrackedTarFile base) {
      super(base.getFile());
      entry_name = ent.getName();
      last_modified = ent.getModTime().getTime();
      setupSynchronization();
    }

   @Override void accept(FileSystemVisitor visitor) {
      visitor.visitFile(this);
    }

   @Override public String getPath() {
      return getFile().getPath() + File.pathSeparator + entry_name;
    }

   @Override public long lastModified() 		{ return last_modified; }

   @Override public boolean isFile()			{ return true; }

   @Override public Reader getReader() throws IOException {
      TarInputStream ins = new TarInputStream(new BufferedInputStream(new FileInputStream(getFile())));
      TarEntry entry;
      while ((entry = ins.getNextEntry()) != null) {
	 String nm = entry.getName();
	 if (nm.equals(entry_name)) {
	    return new InputStreamReader(ins);
	  }
       }
      ins.close();

      return null;
    }

}	// end of inner class TrackedTarEntry



/********************************************************************************/
/*										*/
/*	Implementation for a Zip file						*/
/*										*/
/********************************************************************************/

static class TrackedZipFile extends Container {

   TrackedZipFile(File file) {
      super(file);
      setupSynchronization();
    }

   @Override Iterable<ServerFileImpl> getSubfiles() {
      List<ServerFileImpl> rslt = new ArrayList<ServerFileImpl>();
      ZipFile zip = null;
      try {
         zip = new ZipFile(getFile());
         for (Enumeration<? extends ZipEntry> enm = zip.entries(); enm.hasMoreElements(); ) {
            ZipEntry ent = null;
            try {
               ent = enm.nextElement();
               if (ent.getName().endsWith(".java")) {
                  TrackedZipEntry zent = new TrackedZipEntry(ent,this);
                  rslt.add(zent);
                }
             }
            catch (Exception e) { 
               // System.err.println("COCKER: Problem with zip file entry " + getFile());       
             }
          }
       }
      catch (Exception e) { 
         System.err.println("COCKER: Problem with zip file " + getFile());
       }
      finally {
         try {
            if (zip != null) zip.close();
          }
         catch (IOException e) { }
       }
      return rslt;
    }

}	// end of inner class TrackedTarFile




static class TrackedZipEntry extends ServerFileImpl {

   private ZipEntry	  zip_entry;

   TrackedZipEntry(ZipEntry ent,TrackedZipFile base) {
      super(base.getFile());
      zip_entry = ent;
      setupSynchronization();
    }

   @Override void accept(FileSystemVisitor visitor) {
      visitor.visitFile(this);
    }

   @Override public String getPath() {
      return getFile().getPath() + File.pathSeparator + zip_entry.getName();
    }

   @Override public long lastModified() 		{ return zip_entry.getTime(); }

   @Override public boolean isFile()			{ return true; }

   @SuppressWarnings("resource")
   @Override public Reader getReader() throws IOException {
      ZipFile zf = new ZipFile(getFile());
      ZipEntry ent = zf.getEntry(zip_entry.getName());
      return new InputStreamReader(zf.getInputStream(ent));
    }

}	// end of inner class TrackedTarEntry




}	// end of class ServerFileImpl




/* end of ServerFile.java */
