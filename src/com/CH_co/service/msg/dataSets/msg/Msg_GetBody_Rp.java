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

import com.CH_co.cryptx.*;
import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.records.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.stat.*;

import java.io.IOException;
import java.sql.Timestamp;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.12 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Msg_GetBody_Rp extends ProtocolMsgDataSet {

  // <msgLinkId> <status> <dateDelivered> <dateUpdated>   <msgId> <objType> <replyToMsgId> <importance> <encSubject> <encText> <compressed> <encSignedDigest> <encEncDigest> <sendPrivKeyId> <dateExpired> <recordSize> <bodyPassHint> <bodyPassHash>
  // <Stats_Get_Rp>
  public MsgLinkRecord linkRecord;
  public MsgDataRecord dataRecord;
  public Stats_Get_Rp stats_rp;

  /** Creates new Msg_GetBody_Rp */
  public Msg_GetBody_Rp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetBody_Rp.class, "Msg_GetBody_Rp()");
    if (trace != null) trace.exit(Msg_GetBody_Rp.class);
  }

  /** Creates new Msg_GetBody_Rp */
  public Msg_GetBody_Rp(MsgLinkRecord linkRecord, MsgDataRecord dataRecord, StatRecord statRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetBody_Rp.class, "Msg_GetBody_Rp(MsgLinkRecord linkRecord, MsgDataRecord dataRecord, StatRecord statRecord)");
    if (trace != null) trace.args(linkRecord, dataRecord, statRecord);

    this.linkRecord = linkRecord;
    this.dataRecord = dataRecord;
    this.stats_rp = new Stats_Get_Rp(statRecord);

    if (trace != null) trace.exit(Msg_GetBody_Rp.class);
  }

  public boolean isTimeSensitive() {
    return true;
  }

  public boolean isUserSensitive() {
    return true;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetBody_Rp.class, "writeToStream(DataOutputStream2, ProgMonitor)");
    if (trace != null) trace.args(dataOut, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    dataOut.writeLongObj(linkRecord.msgLinkId);
    dataOut.writeSmallint(linkRecord.status);
    dataOut.writeTimestamp(linkRecord.dateDelivered);
    dataOut.writeTimestamp(linkRecord.dateUpdated);
    if (clientBuild >= 750 && serverBuild >= 750)
      dataOut.writeTimestamp(linkRecord.dateUsed);

    dataOut.writeLongObj(dataRecord.msgId);
    if (clientBuild >= 86)
      dataOut.writeSmallint(dataRecord.objType);
    if (clientBuild >= 12)
      dataOut.writeLongObj(dataRecord.replyToMsgId);
    if (clientBuild >= 220 && serverBuild >= 220)
      dataOut.writeSmallint(dataRecord.importance);
    if (clientBuild >= 460 && serverBuild >= 460)
      dataOut.writeLongObj(dataRecord.senderUserId); // must include senderUserId because in engine-to-eingine communications servant will need to check "privilege-for-body-access"
    dataOut.writeBytes(dataRecord.getEncSubject());

    if (dataRecord.getEncText() == null) {
      dataOut.writeBytes((BA) null);
    } else {
      // in engine-engine communication the privilege check is done by the last destination peer
      if (isEngineToEngine()) {
        dataOut.writeBytes(dataRecord.getEncText());
      } else {
        Long userId = getServerSessionUserId();
        Timestamp tStamp = getServerSessionCurrentStamp();
        if (tStamp == null) {
          throw new IllegalStateException("Cannot write message data part because access check requires timestamp to be present.");
        }
        if (dataRecord.isPrivilegedBodyAccess(userId, tStamp)) {
          dataOut.writeBytes(dataRecord.getEncText());
        } else {
          dataOut.writeBytes(new BASymCipherBulk(new byte[0]));
        }
      }
    }

    dataOut.writeSmallint(dataRecord.getFlags());
    dataOut.writeBytes(dataRecord.getEncSignedDigest());
    dataOut.writeBytes(dataRecord.getEncEncDigest());
    dataOut.writeLongObj(dataRecord.getSendPrivKeyId());
    if (clientBuild >= 220 && serverBuild >= 220)
      dataOut.writeTimestamp(dataRecord.dateExpired);
    dataOut.writeInteger(dataRecord.recordSize);
    if (clientBuild >= 310 && serverBuild >= 310)
      dataOut.writeString(dataRecord.bodyPassHint);
    if (clientBuild >= 250 && serverBuild >= 250)
      dataOut.writeLongObj(dataRecord.bodyPassHash);

    stats_rp.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);

    if (trace != null) trace.exit(Msg_GetBody_Rp.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetBody_Rp.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild, serverBuild)");
    if (trace != null) trace.args(dataIn, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    linkRecord = new MsgLinkRecord();
    linkRecord.msgLinkId = dataIn.readLongObj();
    linkRecord.status = dataIn.readSmallint();
    linkRecord.dateDelivered = dataIn.readTimestamp();
    linkRecord.dateUpdated = dataIn.readTimestamp();
    if (clientBuild >= 750 && serverBuild >= 750)
      linkRecord.dateUsed = dataIn.readTimestamp();

    dataRecord = new MsgDataRecord();

    dataRecord.msgId = dataIn.readLongObj();
    dataRecord.objType = dataIn.readSmallint();
    dataRecord.replyToMsgId = dataIn.readLongObj();
    if (clientBuild >= 220 && serverBuild >= 220)
      dataRecord.importance = dataIn.readSmallint();
    if (clientBuild >= 460 && serverBuild >= 460)
      dataRecord.senderUserId = dataIn.readLongObj();
    dataRecord.setEncSubject(dataIn.readSymCipherBulk());
    dataRecord.setEncText(dataIn.readSymCipherBulk());
    dataRecord.setFlags(dataIn.readSmallint());
    dataRecord.setEncSignedDigest(dataIn.readSymCipherBulk());
    dataRecord.setEncEncDigest(dataIn.readSymCipherBulk());
    dataRecord.setSendPrivKeyId(dataIn.readLongObj());
    if (clientBuild >= 220 && serverBuild >= 220)
      dataRecord.dateExpired = dataIn.readTimestamp();
    dataRecord.recordSize = dataIn.readInteger();
    if (clientBuild >= 310 && serverBuild >= 310)
      dataRecord.bodyPassHint = dataIn.readString();
    if (clientBuild >= 250 && serverBuild >= 250)
      dataRecord.bodyPassHash = dataIn.readLongObj();

    stats_rp = new Stats_Get_Rp();
    stats_rp.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);

    if (trace != null) trace.exit(Msg_GetBody_Rp.class);
  } // end initFromStream()


  public String toString() {
    return "[Msg_GetBody_Rp"
        + ": linkRecord=" + linkRecord
        + ", dataRecord=" + dataRecord
        + ", stats_rp="   + stats_rp
        + "]";
  }

}