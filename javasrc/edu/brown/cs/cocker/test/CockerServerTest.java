/********************************************************************************/
/*                                                                              */
/*              CockerServerTest.java                                           */
/*                                                                              */
/*      Test cases for Cocker server                                            */
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



package edu.brown.cs.cocker.test;

import org.junit.Assert;
import org.junit.Test;

import edu.brown.cs.cocker.application.ApplicationChunkQuery;
import edu.brown.cs.cocker.cocker.CockerServer;

public class CockerServerTest
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public CockerServerTest()
{ }



/********************************************************************************/
/*                                                                              */
/*      Basic server test                                                       */
/*                                                                              */
/********************************************************************************/

@Test
public void basicServerTest()
{
   // Start cocker server
   String [] args = { "-analysis", "SHARPFIXLOCAL", 
         "-dir", "/Users/spr/Eclipse/quixspr/.bubbles/CockerIndex" };
   CockerServer.main(args);
   
   String [] qargs = {  "-analysis", "SHARPFIXLOCAL", 
         "-dir", "/Users/spr/Eclipse/quixspr/.bubbles/CockerIndex",
         "-m", "128",
         "-data", "slc:37,9", "/Users/spr/Eclipse/quixspr/quixbugs/src/edu/brown/cs/quixbugs/LcsLength.java" };
   ApplicationChunkQuery acq = new ApplicationChunkQuery(qargs);
   String rslt = acq.execute();
   Assert.assertNotNull(rslt);
   
// String [] xargs = { "-analysis", "SHARPFIXLOCAL", 
//       "-dir", "/Users/spr/Eclipse/quixspr/.bubbles/CockerIndex",
//       "-stop" };;
// ApplicationServerRegulation.main(xargs);
// try {
//    Thread.sleep(10000);              // give it time to terminate
//  }
// catch (InterruptedException e) { }
// 
// Assert.fail();
}



}       // end of class CockerServerTest




/* end of CockerServerTest.java */

