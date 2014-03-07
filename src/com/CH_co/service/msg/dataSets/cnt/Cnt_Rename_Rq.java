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
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Cnt_Rename_Rq extends ProtocolMsgDataSet {

  // <contactId> <encOwnerNote>
  public ContactRecord contactRecord;
  
  /** Creates new Cnt_Rename_Rq */
  public Cnt_Rename_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_Rename_Rq.class, "Cnt_Rename_Rq()");
    if (trace != null) trace.exit(Cnt_Rename_Rq.class);
  }
  
  /** Creates new Cnt_Rename_Rq */
  public Cnt_Rename_Rq(ContactRecord contactRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_Rename_Rq.class, "Cnt_Rename_Rq()");
    this.contactRecord = contactRecord;
    if (trace != null) trace.exit(Cnt_Rename_Rq.class);
  }
 
  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_Rename_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);
    
    dataOut.writeLongObj(contactRecord.contactId);
    dataOut.writeBytes(contactRecord.getEncOwnerNote());
    
    if (trace != null) trace.exit(Cnt_Rename_Rq.class);
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_Rename_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);
    
    contactRecord = new ContactRecord();
    contactRecord.contactId = dataIn.readLongObj();
    contactRecord.setEncOwnerNote(dataIn.readSymCipherBulk());
    
    if (trace != null) trace.exit(Cnt_Rename_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Cnt_Rename_Rq"
      + ": contactRecord=" + contactRecord
      + "]";
  }

}