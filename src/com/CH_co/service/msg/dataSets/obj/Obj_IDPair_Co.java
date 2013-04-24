/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.service.msg.dataSets.obj;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p> 
 * 
 * Folder: Move Folder: <folderId> <newParentFolderId>
 * 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class Obj_IDPair_Co extends ProtocolMsgDataSet {
  
  // <objId_1> <objId_2>
  public Long objId_1;
  public Long objId_2;

  /** Creates new Obj_IDPair_Co */
  public Obj_IDPair_Co() {
  }
  /** Creates new Obj_IDPair_Co */
  public Obj_IDPair_Co(Long objId_1,Long objId_2) {
    this.objId_1 = objId_1;
    this.objId_2 = objId_2;
  }
  
  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    dataOut.writeLongObj(objId_1);
    dataOut.writeLongObj(objId_2);

  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    objId_1 = dataIn.readLongObj();
    objId_2 = dataIn.readLongObj();
  } // end initFromStream()

  public String toString() {
    return "[Obj_IDPair_Co"
      + ": objId_1=" + objId_1
      + ", objId_2=" + objId_2
      + "]";
  }

}