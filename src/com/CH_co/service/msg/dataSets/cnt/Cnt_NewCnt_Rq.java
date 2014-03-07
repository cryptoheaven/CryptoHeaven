/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.cnt;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.records.ContactRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 * 
 * New Contact
 * @author  Marcin Kurzawa
 */
public class Cnt_NewCnt_Rq extends ProtocolMsgDataSet {

  // <shareId>    <ownerUserId> <contactWithId> <encOwnerNote> <otherKeyId> <encOtherSymKey> <encOtherNote>
  public Long shareId;
  public ContactRecord contactRecord;

  /** Creates new Cnt_NewCnt_Rq */
  public Cnt_NewCnt_Rq() {
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_NewCnt_Rq.class, "writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor)");
    if (trace != null) trace.args(dataOut, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    if (trace != null) trace.data(10, this);

    dataOut.writeLongObj(shareId);
    dataOut.writeLongObj(contactRecord.ownerUserId);
    dataOut.writeLongObj(contactRecord.contactWithId);
    dataOut.writeBytes(contactRecord.getEncOwnerNote());
    dataOut.writeLongObj(contactRecord.getOtherKeyId());
    dataOut.writeBytes(contactRecord.getEncOtherSymKey());
    dataOut.writeBytes(contactRecord.getEncOtherNote());

    if (trace != null) trace.exit(Cnt_NewCnt_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_NewCnt_Rq.class, "initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor)");
    if (trace != null) trace.args(dataIn, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    shareId = dataIn.readLongObj();
    contactRecord = new ContactRecord();
    if (clientBuild >= 35)
      contactRecord.ownerUserId = dataIn.readLongObj();
    contactRecord.contactWithId = dataIn.readLongObj();
    contactRecord.setEncOwnerNote(dataIn.readSymCipherBulk());
    contactRecord.setOtherKeyId(dataIn.readLongObj());
    contactRecord.setEncOtherSymKey(dataIn.readAsyCipherBlock());
    contactRecord.setEncOtherNote(dataIn.readSymCipherBulk());

    if (trace != null) trace.data(100, this);

    if (trace != null) trace.exit(Cnt_NewCnt_Rq.class);
  } // end initFromStream()

  public String toString() {
    return "[Cnt_NewCnt_Rq"
      + ": shareId="        + shareId
      + ", contactRecord="  + contactRecord
      + "]";
  }
}