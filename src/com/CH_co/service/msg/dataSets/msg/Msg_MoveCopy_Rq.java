/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.msg;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

import java.io.IOException;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Msg_MoveCopy_Rq extends ProtocolMsgDataSet {

  // <toShareId> <numberOfMsgs> { <fromMsgLinkId> }+ <numberOfShares> { <fromShareId> }+ <numOfAttachments> { <msgLinkId> <encSymmetricKey> }
  public Long toShareId;
  public Long[] fromMsgLinkIDs;
  public Long[] fromShareIDs;
  public MsgLinkRecord[] msgLinkRecords;


  /** Creates new Msg_MoveCopy_Rq */
  public Msg_MoveCopy_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_MoveCopy_Rq.class, "Msg_MoveCopy_Rq()");
    if (trace != null) trace.exit(Msg_MoveCopy_Rq.class);
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_MoveCopy_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    dataOut.writeLongObj(toShareId);

    // write fromMsgLinkIDs
    // write indicator
    if (fromMsgLinkIDs == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(fromMsgLinkIDs.length);
      for (int i=0; i<fromMsgLinkIDs.length; i++)
        dataOut.writeLongObj(fromMsgLinkIDs[i]);
    }

    // write fromShareIDs
    // write indicator
    if (fromShareIDs == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(fromShareIDs.length);
      for (int i=0; i<fromShareIDs.length; i++)
        dataOut.writeLongObj(fromShareIDs[i]);
    }

    // write msgLinkRecords
    // write indicator
    if (msgLinkRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(msgLinkRecords.length);
      for (int i=0; i<msgLinkRecords.length; i++) {
        dataOut.writeLongObj(msgLinkRecords[i].msgLinkId);
        dataOut.writeBytes(msgLinkRecords[i].getEncSymmetricKey());
      }
    }

    if (trace != null) trace.exit(Msg_MoveCopy_Rq.class);
  } // end writeToStream()


  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_MoveCopy_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    toShareId = dataIn.readLongObj();

    // read fromMsgLinkIDs
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      fromMsgLinkIDs = new Long[0];
    else {
      fromMsgLinkIDs = new Long[dataIn.readShort()];

      for (int i=0; i<fromMsgLinkIDs.length; i++)
        fromMsgLinkIDs[i] = dataIn.readLongObj();
    }

    // read fromShareIDs
    // read indicator
    indicator = dataIn.read();
    if (indicator == 0)
      fromShareIDs = new Long[0];
    else {
      fromShareIDs = new Long[dataIn.readShort()];

      for (int i=0; i<fromShareIDs.length; i++)
        fromShareIDs[i] = dataIn.readLongObj();
    }

    // read msgLinkRecords
    // read indicator
    indicator = dataIn.read();
    if (indicator == 0)
      msgLinkRecords = new MsgLinkRecord[0];
    else {
      msgLinkRecords = new MsgLinkRecord[dataIn.readShort()];

      for (int i=0; i<msgLinkRecords.length; i++) {
        msgLinkRecords[i] = new MsgLinkRecord();
        msgLinkRecords[i].msgLinkId = dataIn.readLongObj();
        msgLinkRecords[i].setEncSymmetricKey(dataIn.readSymCipherBulk());
      }
    }


    if (trace != null) trace.exit(Msg_MoveCopy_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Msg_MoveCopy_Rq"
      + ": toShareId="      + toShareId
      + ", fromMsgLinkIDs=" + Misc.objToStr(fromMsgLinkIDs)
      + ", fromShareIDs="   + Misc.objToStr(fromShareIDs)
      + ", msgLinkRecords=" + Misc.objToStr(msgLinkRecords)
      + "]";
  }

}