/********************************************************************************/
/*                                                                              */
/*              ResourceFinder.java                                             */
/*                                                                              */
/*      Find resource files                                                     */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2011 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 * This program and the accompanying materials are made available under the      *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at                                                           *
 *      http://www.eclipse.org/legal/epl-v10.html                                *
 *                                                                               *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.cocker.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import edu.brown.cs.ivy.file.IvyDatabase;

public class ResourceFinder
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private File            base_directory;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public ResourceFinder()
{ 
   base_directory = null;
}


public ResourceFinder(String env)
{ 
   base_directory = null;
   String ev = System.getenv(env);
   if (ev != null) {
      File f = new File(ev);
      if (f.exists() && f.isDirectory()) base_directory = f;
    }
}


public ResourceFinder(File base)
{
   if (base != null && base.exists() && base.isDirectory()) base_directory = base;
   else base_directory = null;
}



/********************************************************************************/
/*                                                                              */
/*      Get InputStream for a resource                                          */
/*                                                                              */
/********************************************************************************/

public InputStream getInputStream(String name)
{
   InputStream ins = getStreamForDirectory(base_directory,name);
   if (ins != null) return ins;
   
   ClassLoader cl = ResourceFinder.class.getClassLoader();
   ins = cl.getResourceAsStream(name);
   if (ins != null) return ins;
   ins = cl.getResourceAsStream("resources/" + name);
   if (ins != null) return ins;
   
   File root = getRootDirectory();
   if (root != null) {
      ins = getStreamForDirectory(root,name);
      if (ins != null) return ins;
    }
   
   return null;
}



public void setDatabaseProps(String pfx)
{
   InputStream ins = getInputStream("Database.props");
   
   if (ins == null) {
      File f1 = new File(System.getProperty("user.home"));
      File f2 = new File(f1,".ivy");
      String nm = pfx + ".Database.props";
      File f3 = new File(f2,nm);
      if (f3.exists()) {
         try {
            ins = new FileInputStream(f3);
          }
         catch (IOException e) { }
       }
    }
   
   if (ins != null) {
      try { 
         IvyDatabase.setProperties(ins);
       }
      catch (Exception e) {
         System.err.println("Problem setting up database access: " + e);
	 System.exit(1);
       }
    }
}




public File getDirectory(String name)
{
   File f1 = getActualFile(base_directory,name);
   if (f1 != null) return f1;
   File root = getRootDirectory();
   File f2 = getActualFile(root,name);
   if (f2 != null) return f2;
   
   if (root != null && root.canWrite()) {
      File f3 = new File(root,name);
      f3.mkdir();
      if (f3.exists() && f3.isDirectory()) return f3;
    }
   
   File home = new File(System.getProperty("user.home"));
   File f4 = getActualFile(home,name);
   if (f4 != null) return f4;
   File f5 = new File(root,name);
   f5.mkdir();
   if (f5.exists() && f5.isDirectory()) return f5;
   
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Prioperty worker                                                        */
/*                                                                              */
/********************************************************************************/

private InputStream getStreamForDirectory(File dir,String name)
{
   if (dir == null) return null;
   if (name == null) return null; 
   
   String name1 = name.replace("/",File.separator);
   File f1 = new File(dir,name1);
   if (f1.exists() && f1.canRead()) {
      try {
         return new FileInputStream(f1);
       }
      catch (IOException e) { }
    }
   
   return null;
}



private File getRootDirectory()
{
   ClassLoader cl = ResourceFinder.class.getClassLoader();  String cnm = ResourceFinder.class.getName();
   cnm = cnm.replace(".","/");
   cnm = cnm + ".class";
   URL url = cl.getResource(cnm);
   if (url != null) {
      String file = url.toString();
      if (file.startsWith("jar:file:/")) file = file.substring(9);
      if (file.length() >= 3 && file.charAt(0) == '/' &&
            Character.isLetter(file.charAt(1)) && file.charAt(2) == ':' &&
            File.separatorChar == '\\')
         file = file.substring(1);
      int idx = file.lastIndexOf('!');
      if (idx >= 0) file = file.substring(0,idx);
      if (File.separatorChar != '/') file = file.replace('/',File.separatorChar);
      file = file.replace("%20"," ");
      File f = new File(file);
      if (f.exists() && f.isDirectory()) return f;
    }
   
   return null;
}



private File getActualFile(File dir,String name)
{
   if (dir == null || name == null) return null;
   
   File f2 = new File(dir,name);
   if (f2.exists()) {
      try {
         f2 = f2.getCanonicalFile();
       }
      catch (IOException e) { }
      if (f2.exists() && f2.isDirectory()) return f2;
    }
   
   return null;
}



}       // end of class ResourceFinder




/* end of ResourceFinder.java */

