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

import com.CH_co.monitor.ProgMonitor;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.ContactRecord;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Cnt_AcceptDecline_Rq extends ProtocolMsgDataSet {

  // <numOfContacts> { <contactId> <otherKeyId> <encOtherSymKey> <encOtherNote> <permits> }+ <autoCreateReciprocals>
  public ContactRecord[] contactRecords;
  public Boolean autoCreateReciprocals;

  /** Creates new Cnt_AcceptDecline_Rq */
  public Cnt_AcceptDecline_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_AcceptDecline_Rq.class, "Cnt_AcceptDecline_Rq()");
    if (trace != null) trace.exit(Cnt_AcceptDecline_Rq.class);
  }
  /** Creates new Cnt_AcceptDecline_Rq */
  public Cnt_AcceptDecline_Rq(ContactRecord contactRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_AcceptDecline_Rq.class, "Cnt_AcceptDecline_Rq()");
    if (trace != null) trace.args(contactRecord);
    this.contactRecords = new ContactRecord[] { contactRecord };
    if (trace != null) trace.exit(Cnt_AcceptDecline_Rq.class);
  }
  /** Creates new Cnt_AcceptDecline_Rq */
  public Cnt_AcceptDecline_Rq(ContactRecord[] contactRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_AcceptDecline_Rq.class, "Cnt_AcceptDecline_Rq()");
    if (trace != null) trace.args(contactRecords);
    this.contactRecords = contactRecords;
    if (trace != null) trace.exit(Cnt_AcceptDecline_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_AcceptDecline_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    dataOut.writeShort(contactRecords.length);

    for (int i=0; i<contactRecords.length; i++) {
      dataOut.writeLongObj(contactRecords[i].contactId);
      dataOut.writeLongObj(contactRecords[i].getOtherKeyId());
      dataOut.writeBytes(contactRecords[i].getEncOtherSymKey());
      dataOut.writeBytes(contactRecords[i].getEncOtherNote());
      dataOut.writeInteger(contactRecords[i].permits);
    }
    if (clientBuild >= 382 && serverBuild >= 382)
      dataOut.writeBooleanObj(autoCreateReciprocals);

    if (trace != null) trace.exit(Cnt_AcceptDecline_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_AcceptDecline_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    contactRecords = new ContactRecord[dataIn.readShort()];

    for (int i=0; i<contactRecords.length; i++) {
      contactRecords[i] = new ContactRecord();
      contactRecords[i].contactId = dataIn.readLongObj();
      contactRecords[i].setOtherKeyId(dataIn.readLongObj());
      contactRecords[i].setEncOtherSymKey(dataIn.readAsyCipherBlock());
      contactRecords[i].setEncOtherNote(dataIn.readSymCipherBulk());
      if (clientBuild >= 28) 
        contactRecords[i].permits = dataIn.readInteger();
      else
        contactRecords[i].permits = new Integer(0);
    }
    if (clientBuild >= 382 && serverBuild >= 382)
      autoCreateReciprocals = dataIn.readBooleanObj();

    if (trace != null) trace.exit(Cnt_AcceptDecline_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Cnt_AcceptDecline_Rq"
      + ": contactRecords=" + Misc.objToStr(contactRecords)
      + ", autoCreateReciprocals=" + autoCreateReciprocals
      + "]";
  }

}