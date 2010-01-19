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

package com.CH_co.service.msg.dataSets.cnt;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.*;

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
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_Rename_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);
    
    dataOut.writeLongObj(contactRecord.contactId);
    dataOut.writeBytes(contactRecord.getEncOwnerNote());
    
    if (trace != null) trace.exit(Cnt_Rename_Rq.class);
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
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