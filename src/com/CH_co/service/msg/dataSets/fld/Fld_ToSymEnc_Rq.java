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
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Fld_ToSymEnc_Rq extends ProtocolMsgDataSet {

  // <numOfShares> { <shareId> <folderId> <encFolderName> <encFolderDesc> <encSymmetricKey> <pubKeyId> }+
  public FolderShareRecord[] folderShareRecords;

  /** Creates new Fld_ToSymEnc_Rq */
  public Fld_ToSymEnc_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_ToSymEnc_Rq.class, "Fld_ToSymEnc_Rq()");
    if (trace != null) trace.exit(Fld_ToSymEnc_Rq.class);
  }
  public Fld_ToSymEnc_Rq(FolderShareRecord[] shareRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_ToSymEnc_Rq.class, "Fld_ToSymEnc_Rq(FolderShareRecord[] shareRecords)");
    if (trace != null) trace.args(shareRecords);
    this.folderShareRecords = shareRecords;
    if (trace != null) trace.exit(Fld_ToSymEnc_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_ToSymEnc_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    // write indicator
    if (folderShareRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(folderShareRecords.length);

      for (int i=0; i<folderShareRecords.length; i++) {
        FolderShareRecord sRec = folderShareRecords[i];
        dataOut.writeLongObj(sRec.shareId);
        dataOut.writeLongObj(sRec.folderId);
        dataOut.writeBytes(sRec.getEncFolderName());
        dataOut.writeBytes(sRec.getEncFolderDesc());
        dataOut.writeBytes(sRec.getEncSymmetricKey());
        dataOut.writeLongObj(sRec.getPubKeyId());
      }
    }

    if (trace != null) trace.exit(Fld_ToSymEnc_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_ToSymEnc_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      folderShareRecords = new FolderShareRecord[0];
    else {
      folderShareRecords = new FolderShareRecord[dataIn.readShort()];

      for (int i=0; i<folderShareRecords.length; i++) {
        FolderShareRecord sRec = new FolderShareRecord();
        folderShareRecords[i] = sRec;
        sRec.shareId = dataIn.readLongObj();
        sRec.folderId = dataIn.readLongObj();
        sRec.setEncFolderName(dataIn.readSymCipherBulk());
        sRec.setEncFolderDesc(dataIn.readSymCipherBulk());
        sRec.setEncSymmetricKey(dataIn.readSymCipherBulk());
        sRec.setPubKeyId(dataIn.readLongObj());
      }
    }

    if (trace != null) trace.exit(Fld_ToSymEnc_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Fld_ToSymEnc_Rq"
      + ": folderShareRecords=" + Misc.objToStr(folderShareRecords)
      + "]";
  }

}