/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.service.msg.dataSets.fld;

import java.io.IOException;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Fld_AltPerm_Rq extends ProtocolMsgDataSet {

  // <numberOfShares> { <shareId> <folderId> <canWrite> <canDelete> }+
  public FolderShareRecord[] folderShareRecords;
  
  /** Creates new Fld_AltPerm_Rq */
  public Fld_AltPerm_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AltPerm_Rq.class, "Fld_AltPerm_Rq()");
    if (trace != null) trace.exit(Fld_AltPerm_Rq.class);
  }
  /** Creates new Fld_AltPerm_Rq */
  public Fld_AltPerm_Rq(FolderShareRecord[] folderShareRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AltPerm_Rq.class, "Fld_AltPerm_Rq(FolderShareRecord[] folderShareRecords)");
    if (trace != null) trace.args(folderShareRecords);
    this.folderShareRecords = folderShareRecords;
    if (trace != null) trace.exit(Fld_AltPerm_Rq.class);
  }
 
  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AltPerm_Rq.class, "writeToStream(DataOutputStream2)");

    int length = folderShareRecords.length;
    dataOut.writeShort(length);
    
    for (int i=0; i<length; i++) {
      dataOut.writeLongObj(folderShareRecords[i].shareId);
      dataOut.writeLongObj(folderShareRecords[i].folderId);
      dataOut.writeSmallint(folderShareRecords[i].canWrite);
      dataOut.writeSmallint(folderShareRecords[i].canDelete);
    }

    if (trace != null) trace.exit(Fld_AltPerm_Rq.class);
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AltPerm_Rq.class, "initFromStream(DataInputStream2)");

    int length = dataIn.readShort();
    folderShareRecords = new FolderShareRecord[length];
    
    for (int i=0; i<length; i++) {
      folderShareRecords[i] = new FolderShareRecord();
      folderShareRecords[i].shareId = dataIn.readLongObj();
      folderShareRecords[i].folderId = dataIn.readLongObj();
      folderShareRecords[i].canWrite = dataIn.readSmallint();
      folderShareRecords[i].canDelete = dataIn.readSmallint();
    }
    
    if (trace != null) trace.exit(Fld_AltPerm_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Fld_AltPerm_Rq"
      + ": folderShareRecords=" + Misc.objToStr(folderShareRecords)
      + "]";
  }

}