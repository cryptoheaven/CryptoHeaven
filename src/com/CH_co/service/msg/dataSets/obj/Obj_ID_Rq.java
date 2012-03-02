/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
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
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p> 
 * 
 * Remove Folder, 
 * Get Shared Folder Sharees,
 * Get Files for this ownerObjId
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class Obj_ID_Rq extends ProtocolMsgDataSet {
  // <objId> 
  public Long objId;

  /** Creates new Obj_ID_Rq */
  public Obj_ID_Rq() {
  }
  /** Creates new Obj_ID_Rq */
  public Obj_ID_Rq(Long id) {
    this.objId = id;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    dataOut.writeLongObj(objId);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    objId = dataIn.readLongObj();
  } // end initFromStream()


  public String toString() {
    return "[Obj_ID_Rq"
      + ": objId=" + objId
      + "]";
  }

}