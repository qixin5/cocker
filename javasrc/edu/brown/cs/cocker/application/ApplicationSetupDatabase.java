/********************************************************************************/
/*										*/
/*		ApplicationSetupDatabase.java					*/
/*										*/
/*	Command line application to do database setup				*/
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

package edu.brown.cs.cocker.application;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.*;
import java.nio.file.*;

import edu.brown.cs.cocker.search.SearchProvider;
import edu.brown.cs.cocker.server.ServerFileChangeBroadcaster;
import edu.brown.cs.cocker.analysis.AnalysisConstants;
import edu.brown.cs.ivy.file.*;


public class ApplicationSetupDatabase implements AnalysisConstants {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   ApplicationSetupDatabase cli = new ApplicationSetupDatabase(args);
   cli.execute();
}



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private boolean 	new_database;
private boolean 	clean_database;
private boolean 	drop_database;
private String		cocker_home;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private ApplicationSetupDatabase(String [] args)
{
   new_database = false;
   clean_database = false;

   cocker_home = System.getenv("COCKER_HOME");

   //==========
   System.err.println("Check cocker home: " + cocker_home);
   //==========   

   scanArgs(args);

   if (cocker_home != null) {
      File p1 = new File(cocker_home);
      File p2 = new File(p1,"Database.props");
      try {
	 IvyDatabase.setProperties(p2.getPath());
       }
      catch (SQLException e) {
	 System.err.println("Problem setting up database access: " + e);
	 System.exit(1);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Handle arguments							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-n")) {                                // -newdb
	    new_database = true;
	  }
	 else if (args[i].startsWith("-c")) {                           // -clean
	    clean_database = true;
	  }
	 else if (args[i].startsWith("-h") && i+1 < args.length) {      // -H <Home>
	    cocker_home = args[++i];
	  }
	 else if (args[i].startsWith("-a") && i+1 < args.length) {      // -analysis <type>
	    String type = args[++i];
	    AnalysisConstants.Factory.setAnalysisType(type);
	  }
	 else if (args[i].startsWith("-d")) {
	    clean_database = true;
	    drop_database = true;
	  }
	 else badArgs();
       }
      else if (args[i].equals("new")) {
	 new_database = true;
       }
      else if (args[i].equals("clean")) {
	 clean_database = true;
       }
      else badArgs();
    }
}


private void badArgs()
{
   System.err.println("COCKERDB: [-newdb|-clean]");
   System.exit(1);

}



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void execute()
{
   if (drop_database) {
      cleanDatabase();
      removeDatabase();
      return;
    }

   if (new_database) {
      createNewDatabase();
    }

   if (clean_database || new_database) {
      cleanDatabase();
      if (!new_database) {
	 ServerFileChangeBroadcaster fscb = ServerFileChangeBroadcaster.getFscb();
	 try {
	    fscb.clearDatabase();
	  }
	 catch (SQLException e) {
	    System.err.println("COCKER: Problem cleaning user database: " + e);
	  }
       }
    }
}



private void cleanDatabase()
{
   try {
      try {
	 URI pathuri = null;
	 String uriname = AnalysisConstants.Factory.getAnalysisType().getIndexPath();
	 pathuri = new URI(uriname);
	 Path path = Paths.get(pathuri);
	 IvyFile.remove(path.toFile());
       }
      catch (Exception e) {  }

      SearchProvider sp = SearchProvider.getProvider();
      sp.createIndex();
      sp.optimizeIndex();
    }
   catch (Exception e) {
      System.err.println("COCKER: Problem cleaning database: " + e);
    }
}


private void createNewDatabase()
{
   removeDatabase();

   String dbname = AnalysisConstants.Factory.getAnalysisType().getDatabaseName();

   try {
      //=============
      System.err.println("To make a connection.");
      //=============      
      Connection connection = IvyDatabase.openDefaultDatabase();
      //=============
      System.err.println("Established the connection.");
      //=============      
      Statement statement = connection.createStatement();
      String create = "CREATE DATABASE " + dbname;
      //=============
      System.err.println("Upon executing " + create);
      //=============      
      statement.executeUpdate(create);
      ServerFileChangeBroadcaster fscb = ServerFileChangeBroadcaster.getFscb();
      fscb.setupDatabase();
    }
   catch (SQLException e) {
      System.err.println("COCKER: Problem creating user database: " + e);
      System.exit(1);
    }
}


private void removeDatabase()
{
   String dbname = AnalysisConstants.Factory.getAnalysisType().getDatabaseName();
   Connection connection = null;
   try {
      connection = IvyDatabase.openDefaultDatabase();
      Statement statement = connection.createStatement();
      String drop = "DROP DATABASE " + dbname;
      statement.executeUpdate(drop);
      connection.close();
    }
   catch (SQLException e) { }
   finally {
      try {
	 if (connection != null) connection.close();
       }
      catch (SQLException e) { }
      connection = null;
    }
}



}	// end of class ApplicationSetupDatabase




/* end of ApplicationSetupDatabase.java */
