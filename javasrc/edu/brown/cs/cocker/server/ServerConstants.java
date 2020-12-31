/********************************************************************************/
/*										*/
/*		ServerConstants.java						*/
/*										*/
/*	Constants and interfaces for use with the server			*/
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
import java.io.FileFilter;
import java.io.IOException;
import java.util.EventListener;
import java.io.Reader;

public interface ServerConstants
{



/********************************************************************************/
/*										*/
/*	Server Defaults 							*/
/*										*/
/********************************************************************************/

int DEFAULT_THREAD_POOL_SIZE = 5;
int DEFAULT_TIMEOUT = 0;
String DEFAULT_HOSTNAME = "localhost";

String	WEB_HOST_NAME = "bdognom";
int WEB_HOST_PORT_DELTA = 1000;
int NUM_WEB_CLIENTS = 10;




/********************************************************************************/
/*										*/
/*	ServerFile -- abstract tracked file					*/
/*										*/
/********************************************************************************/

interface ServerFile {

   long lastModified();
   String getPath();
   boolean isFile();
   Reader getReader() throws IOException;

}	// end of interface ServerFile



/********************************************************************************/
/*										*/
/*	FileChangeEvent -- event for file change notifications			*/
/*										*/
/********************************************************************************/

class FileChangeEvent {

   private ServerFile event_file;

   protected FileChangeEvent(ServerFile file)	{ event_file = file; }

   public ServerFile getFile()			{ return event_file; }

}	// end of inner class FileChangeEvent



/********************************************************************************/
/*										*/
/*	FileChangeListener -- callback for file change notifications		*/
/*										*/
/********************************************************************************/

public interface FileChangeListener extends EventListener {

   void fileCreated(FileChangeEvent fileChange);

   void fileDeleted(FileChangeEvent fileChange);

   void fileModified(FileChangeEvent fileChange);

}	// end of inner interface FileChangeListener



/********************************************************************************/
/*										*/
/*	FileSystemVisitor -- interface for visiting file system changes 							*/
/*										*/
/********************************************************************************/

public interface FileSystemVisitor {

   void visitFile(ServerFileImpl file);
   void visitContainer(ServerFileImpl file);

}	// end of inner interface FileSystemVisitor



/********************************************************************************/
/*										*/
/*	File Filter for Java code files 					*/
/*										*/
/********************************************************************************/

public class JavaCodeFileFilter implements FileFilter {

   public boolean accept(File file) {
      if (file.getName().toLowerCase().endsWith(".java")) return true;
      if (file.isDirectory()) {
	 if (file.getName().startsWith(".snapshot")) return false;
	 return true;
       }
      if (file.getName().toLowerCase().endsWith(".jar")) return true;
      if (file.getName().toLowerCase().endsWith(".tar")) return true;
      if (file.getName().toLowerCase().endsWith(".zip")) return true;
      return false;
    }

}	// end of inner class JavaCodeFileFilter



/********************************************************************************/
/*										*/
/*	Bad Message Exception							*/
/*										*/
/********************************************************************************/

public class MalformedMessageException extends Exception {

   private static final long serialVersionUID = 199891205762994032L;

   public MalformedMessageException(String message)	{ super(message); }


   public MalformedMessageException(String message,Throwable nestedException) {
      super(message,nestedException);
    }

}	// end of class MalformedMessageException




}	// end of interface ServerConstants




/* end of ServerConstants.java */
