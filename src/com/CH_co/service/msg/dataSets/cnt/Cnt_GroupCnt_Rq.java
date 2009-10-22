/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
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

import com.CH_co.service.records.ContactRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 * 
 * New Contact
 * @author  Marcin Kurzawa
 * @version 
 */
public class Cnt_GroupCnt_Rq extends ProtocolMsgDataSet {

  // <numOfContacts> { <contactId> <ownerUserId> <contactWithId> <permits> }+
  public ContactRecord[] contactRecords;

  /** Creates new Cnt_GroupCnt_Rq */
  public Cnt_GroupCnt_Rq() {
  }
  public Cnt_GroupCnt_Rq(ContactRecord contactRecord) {
    this.contactRecords = new ContactRecord[] { contactRecord };
  }
  public Cnt_GroupCnt_Rq(ContactRecord[] contactRecords) {
    this.contactRecords = contactRecords;
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_GroupCnt_Rq.class, "writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor)");
    if (trace != null) trace.args(dataOut, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    if (trace != null) trace.data(10, this);

    // write indicator
    if (contactRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(contactRecords.length);
      for (int i=0; i<contactRecords.length; i++) {
        ContactRecord contactRecord = contactRecords[i];
        dataOut.writeLongObj(contactRecord.contactId);
        dataOut.writeLongObj(contactRecord.ownerUserId);
        dataOut.writeLongObj(contactRecord.contactWithId);
        dataOut.writeInteger(contactRecord.permits);
      }
    }

    if (trace != null) trace.exit(Cnt_GroupCnt_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_GroupCnt_Rq.class, "initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor)");
    if (trace != null) trace.args(dataIn, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      contactRecords = new ContactRecord[0];
    else {
      contactRecords = new ContactRecord[dataIn.readShort()];

      for (int i=0; i<contactRecords.length; i++) {
        ContactRecord contactRecord = new ContactRecord();
        contactRecords[i] = contactRecord;
        contactRecord.contactId = dataIn.readLongObj();
        contactRecord.ownerUserId = dataIn.readLongObj();
        contactRecord.contactWithId = dataIn.readLongObj();
        contactRecord.permits = dataIn.readInteger();
      }
    }

    if (trace != null) trace.data(100, this);

    if (trace != null) trace.exit(Cnt_GroupCnt_Rq.class);
  } // end initFromStream()

  public String toString() {
    return "[Cnt_GroupCnt_Rq"
      + ": contactRecords="  + Misc.objToStr(contactRecords)
      + "]";
  }

}