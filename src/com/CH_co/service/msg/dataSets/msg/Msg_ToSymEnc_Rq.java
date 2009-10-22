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

package com.CH_co.service.msg.dataSets.msg;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Msg_ToSymEnc_Rq extends ProtocolMsgDataSet {

  // <numOfShares> { <shareId> }* <numOfMsgs> { <msgLinkId> <encSymmetricKey> }*
  public Long[] shareIDs;
  public MsgLinkRecord[] linkRecords;

  /** Creates new Msg_ToSymEnc_Rq */
  public Msg_ToSymEnc_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_ToSymEnc_Rq.class, "Msg_ToSymEnc_Rq()");
    if (trace != null) trace.exit(Msg_ToSymEnc_Rq.class);
  }

  /** Creates new Msg_ToSymEnc_Rq */
  public Msg_ToSymEnc_Rq(Long[] shareIDs, MsgLinkRecord[] linkRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_ToSymEnc_Rq.class, "Msg_ToSymEnc_Rq(Long[] shareIDs, MsgLinkRecord[] linkRecords)");
    this.shareIDs = shareIDs;
    this.linkRecords = linkRecords;
    if (trace != null) trace.exit(Msg_ToSymEnc_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_ToSymEnc_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    // write indicator
    if (shareIDs == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(shareIDs.length);
      for (int i=0; i<shareIDs.length; i++) {
        dataOut.writeLongObj(shareIDs[i]);
      }
    }

    // write indicator
    if (linkRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(linkRecords.length);
      for (int i=0; i<linkRecords.length; i++) {
        dataOut.writeLongObj(linkRecords[i].msgLinkId);
        dataOut.writeBytes(linkRecords[i].getEncSymmetricKey());
      }
    }

    if (trace != null) trace.exit(Msg_ToSymEnc_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_ToSymEnc_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      shareIDs = null;
    else {
      shareIDs = new Long[dataIn.readShort()];

      for (int i=0; i<shareIDs.length; i++) {
        shareIDs[i] = dataIn.readLongObj();
      }
    }

    indicator = dataIn.read();
    if (indicator == 0)
      linkRecords = null;
    else {
      linkRecords = new MsgLinkRecord[dataIn.readShort()];

      for (int i=0; i<linkRecords.length; i++) {
        linkRecords[i] = new MsgLinkRecord();
        linkRecords[i].msgLinkId = dataIn.readLongObj();
        linkRecords[i].setEncSymmetricKey(dataIn.readSymCipherBulk());
      }
    }

    if (trace != null) trace.exit(Msg_ToSymEnc_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Msg_ToSymEnc_Rq"
      + ": shareIDs="     + Misc.objToStr(shareIDs)
      + ", linkRecords="  + Misc.objToStr(linkRecords)
      + "]";
  }

}