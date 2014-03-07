/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.fld;

import java.io.IOException;

import com.CH_co.cryptx.*;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Fld_AltStrs_Rq extends ProtocolMsgDataSet {

  // <numberOfShares> { <shareId> <folderId> <encFolderName> <encFolderDesc> <encSymmetricKey> <pubKeyId> }+
  public FolderShareRecord[] folderShareRecords;

  /** Creates new Fld_AltStrs_Rq */
  public Fld_AltStrs_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AltStrs_Rq.class, "Fld_AltStrs_Rq()");
    if (trace != null) trace.exit(Fld_AltStrs_Rq.class);
  }

  /** Creates new Fld_AltStrs_Rq */
  public Fld_AltStrs_Rq(FolderShareRecord folderShareRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AltStrs_Rq.class, "Fld_AltStrs_Rq(FolderShareRecord folderShareRecord)");
    if (trace != null) trace.args(folderShareRecord);
    folderShareRecords = new FolderShareRecord[] { folderShareRecord };
    if (trace != null) trace.exit(Fld_AltStrs_Rq.class);
  }

  /** Creates new Fld_AltStrs_Rq */
  public Fld_AltStrs_Rq(FolderShareRecord[] folderShareRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AltStrs_Rq.class, "Fld_AltStrs_Rq(FolderShareRecord[] folderShareRecords)");
    if (trace != null) trace.args(folderShareRecords);
    this.folderShareRecords = folderShareRecords;
    if (trace != null) trace.exit(Fld_AltStrs_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AltStrs_Rq.class, "writeToStream(DataOutputStream2)");

    int length = 1;
    if (clientBuild >= 224 && serverBuild >= 224) {
      length = folderShareRecords.length;
      dataOut.writeShort(length);
    }

    for (int i=0; i<length; i++) {
      dataOut.writeLongObj(folderShareRecords[i].shareId);
      dataOut.writeLongObj(folderShareRecords[i].folderId);
      dataOut.writeBytes(folderShareRecords[i].getEncFolderName());
      dataOut.writeBytes(folderShareRecords[i].getEncFolderDesc());
      dataOut.writeBytes(folderShareRecords[i].getEncSymmetricKey());
      dataOut.writeLongObj(folderShareRecords[i].getPubKeyId());
    }

    if (trace != null) trace.exit(Fld_AltStrs_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AltStrs_Rq.class, "initFromStream(DataInputStream2)");

    int length = 1;
    if (clientBuild >= 224 && serverBuild >= 224) {
      length = dataIn.readShort();
    }
    folderShareRecords = new FolderShareRecord[length];

    for (int i=0; i<length; i++) {
      folderShareRecords[i] = new FolderShareRecord();
      folderShareRecords[i].shareId = dataIn.readLongObj();
      folderShareRecords[i].folderId = dataIn.readLongObj();
      folderShareRecords[i].setEncFolderName(dataIn.readSymCipherBulk());
      folderShareRecords[i].setEncFolderDesc(dataIn.readSymCipherBulk());
      folderShareRecords[i].setEncSymmetricKey(dataIn.readAsyCipherBlock());
      folderShareRecords[i].setPubKeyId(dataIn.readLongObj());
    }

    if (trace != null) trace.exit(Fld_AltStrs_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Fld_AltStrs_Rq"
      + ": folderShareRecords=" + Misc.objToStr(folderShareRecords)
      + "]";
  }

}