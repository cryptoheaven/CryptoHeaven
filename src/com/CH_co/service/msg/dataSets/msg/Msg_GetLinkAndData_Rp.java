/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
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
import com.CH_co.util.Misc;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.records.MsgDataRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.stat.*;

import java.io.IOException;
import java.sql.Timestamp;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Msg_GetLinkAndData_Rp extends ProtocolMsgDataSet {

  // <ownerObjType> <ownerObjId> <fetchNumMax> <fetchNumNew> <timestamp> 
  // <numOfLinks> { <msgLinkId> <msgId> <ownerObjId> <ownerObjType> <encSymmetricKey> <recPubKeyId> <status> <dateCreated> <dateDelivered> <dateUpdated> }*
  // <numOfDatas> { <msgId> <objType> <replyToMsgId> <importance> <attachedFiles> <attachedMsgs> <senderUserId> <recipients> 
  // <encSubject> <encText> <compressed> <encSignedDigest> <encEncDigest> <sendPrivKeyId> <dateCreated> <dateExpired> <recordSize> <bodyPassHint> <bodyPassHash> }*
  // <Stats_Get_Rp>

  public Short ownerObjType;
  public Long ownerObjId;
  public Short fetchNumMax;
  public Short fetchNumNew;
  public Timestamp timestamp;
  public boolean anySkippedOver;
  public MsgLinkRecord[] linkRecords;
  public MsgDataRecord[] dataRecords;
  public Stats_Get_Rp stats_rp;

  /** Creates new Msg_GetLinkAndData_Rp */
  public Msg_GetLinkAndData_Rp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetLinkAndData_Rp.class, "Msg_GetLinkAndData_Rp()");
    if (trace != null) trace.exit(Msg_GetLinkAndData_Rp.class);
  }

  /** Creates new Msg_GetLinkAndData_Rp */
  public Msg_GetLinkAndData_Rp(MsgLinkRecord[] linkRecords, MsgDataRecord dataRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetLinkAndData_Rp.class, "Msg_GetLinkAndData_Rp(MsgLinkRecord[] linkRecords, MsgDataRecord dataRecord)");
    if (trace != null) trace.args(linkRecords, dataRecord);
    this.linkRecords = linkRecords;
    if (dataRecord != null) this.dataRecords = new MsgDataRecord[] { dataRecord };
    if (trace != null) trace.exit(Msg_GetLinkAndData_Rp.class);
  }

  /** Creates new Msg_GetLinkAndData_Rp */
  public Msg_GetLinkAndData_Rp(MsgLinkRecord[] linkRecords, MsgDataRecord[] dataRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetLinkAndData_Rp.class, "Msg_GetLinkAndData_Rp(MsgLinkRecord[] linkRecords, MsgDataRecord[] dataRecords)");
    if (trace != null) trace.args(linkRecords, dataRecords);
    this.linkRecords = linkRecords;
    this.dataRecords = dataRecords;
    if (trace != null) trace.exit(Msg_GetLinkAndData_Rp.class);
  }

  /** Creates new Msg_GetLinkAndData_Rp */
  public Msg_GetLinkAndData_Rp(Short ownerObjType, Long ownerObjId, Short fetchNumMax, Short fetchNumNew, Timestamp timestamp, boolean anySkippedOver, MsgLinkRecord[] linkRecords,MsgDataRecord[] dataRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetLinkAndData_Rp.class, "Msg_GetLinkAndData_Rp(Short fetchNumMax, Short fetchNumNew, Timestamp timestamp, boolean anySkippedOver, MsgLinkRecord[] linkRecords, MsgDataRecord[] dataRecords)");
    if (trace != null) trace.args(ownerObjType, ownerObjId, fetchNumMax, fetchNumNew, timestamp, linkRecords, dataRecords);
    this.ownerObjType = ownerObjType;
    this.ownerObjId = ownerObjId;
    this.fetchNumMax = fetchNumMax;
    this.fetchNumNew = fetchNumNew;
    this.timestamp = timestamp;
    this.anySkippedOver = anySkippedOver;
    this.linkRecords = linkRecords;
    this.dataRecords = dataRecords;
    if (trace != null) trace.exit(Msg_GetLinkAndData_Rp.class);
  }

  /** Creates new Msg_GetLinkAndData_Rp */
  public Msg_GetLinkAndData_Rp(MsgLinkRecord linkRecord,MsgDataRecord dataRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetLinkAndData_Rp.class, "Msg_GetLinkAndData_Rp(MsgLinkRecord linkRecord, MsgDataRecord dataRecord)");
    if (trace != null) trace.args(linkRecord, dataRecord);
    if (linkRecord != null) this.linkRecords = new MsgLinkRecord[] { linkRecord };
    if (dataRecord != null) this.dataRecords = new MsgDataRecord[] { dataRecord };
    if (trace != null) trace.exit(Msg_GetLinkAndData_Rp.class);
  }

  public boolean isTimeSensitive() {
    return true;
  }

  public boolean isUserSensitive() {
    return true;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetLinkAndData_Rp.class, "writeToStream(DataOutputStream2, ProgMonitor, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(dataOut, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    if (clientBuild >= 388 && serverBuild >= 388) {
      dataOut.writeSmallint(ownerObjType);
      dataOut.writeLongObj(ownerObjId);
    }
    dataOut.writeSmallint(fetchNumMax);
    dataOut.writeSmallint(fetchNumNew);
    dataOut.writeTimestamp(timestamp);
    if (clientBuild >= 638 && serverBuild >= 638)
      dataOut.writeBoolean(anySkippedOver);

    // write MsgLinkRecords first
    // write indicator
    if (linkRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(linkRecords.length);

      for (int i=0; i<linkRecords.length; i++) {
        dataOut.writeLongObj(linkRecords[i].msgLinkId);
        dataOut.writeLongObj(linkRecords[i].msgId);
        dataOut.writeLongObj(linkRecords[i].ownerObjId);
        dataOut.writeSmallint(linkRecords[i].ownerObjType);
        dataOut.writeBytes(linkRecords[i].getEncSymmetricKey());
        dataOut.writeLongObj(linkRecords[i].getRecPubKeyId());
        dataOut.writeSmallint(linkRecords[i].status);
        dataOut.writeTimestamp(linkRecords[i].dateCreated);
        if (clientBuild >= 220 && serverBuild >= 220)
          dataOut.writeTimestamp(linkRecords[i].dateDelivered);
        dataOut.writeTimestamp(linkRecords[i].dateUpdated);
      }
    }
    // write MsgDataRecords second
    // write indicator
    if (dataRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(dataRecords.length);

      for (int i=0; i<dataRecords.length; i++) {
        dataOut.writeLongObj(dataRecords[i].msgId);
        if (clientBuild >= 86)
          dataOut.writeSmallint(dataRecords[i].objType);
        if (clientBuild >= 12) 
          dataOut.writeLongObj(dataRecords[i].replyToMsgId);
        dataOut.writeSmallint(dataRecords[i].importance);
        dataOut.writeSmallint(dataRecords[i].attachedFiles);
        dataOut.writeSmallint(dataRecords[i].attachedMsgs);
        dataOut.writeLongObj(dataRecords[i].senderUserId);
        dataOut.writeBytes(dataRecords[i].getRawRecipients());
        dataOut.writeBytes(dataRecords[i].getEncSubject());

        if (dataRecords[i].getEncText() == null)
          dataOut.writeBytes((BA) null);
        else {
          // in engine-engine communication the privilege check is done by the last destination peer
          if (isEngineToEngine()) {
            dataOut.writeBytes(dataRecords[i].getEncText());
          } else {
            Long userId = getServerSessionUserId();
            Timestamp tStamp = getServerSessionCurrentStamp();
            if (tStamp == null)
              throw new IllegalStateException("Cannot write message data part because access check requires timestamp to be present.");
            if (dataRecords[i].isPrivilegedBodyAccess(userId, tStamp))
              dataOut.writeBytes(dataRecords[i].getEncText());
            else
              dataOut.writeBytes(new BASymCipherBulk(new byte[0]));
          }
        }

        dataOut.writeSmallint(dataRecords[i].getFlags());
        dataOut.writeBytes(dataRecords[i].getEncSignedDigest());
        dataOut.writeBytes(dataRecords[i].getEncEncDigest());
        dataOut.writeLongObj(dataRecords[i].getSendPrivKeyId());
        dataOut.writeTimestamp(dataRecords[i].dateCreated);
        if (clientBuild >= 220 && serverBuild >= 220)
          dataOut.writeTimestamp(dataRecords[i].dateExpired);
        dataOut.writeInteger(dataRecords[i].recordSize);
        if (clientBuild >= 310 && serverBuild >= 310)
          dataOut.writeString(dataRecords[i].bodyPassHint);
        if (clientBuild >= 250 && serverBuild >= 250)
          dataOut.writeLongObj(dataRecords[i].bodyPassHash);
      }
    }
    if (stats_rp == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      stats_rp.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }

    if (trace != null) trace.exit(Msg_GetLinkAndData_Rp.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetLinkAndData_Rp.class, "initFromStream(DataInputStream2, ProgMonitor, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(dataIn, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    if (clientBuild >= 388 && serverBuild >= 388) {
      ownerObjType = dataIn.readSmallint();
      ownerObjId = dataIn.readLongObj();
    }
    fetchNumMax = dataIn.readSmallint();
    fetchNumNew = dataIn.readSmallint();
    timestamp = dataIn.readTimestamp();
    if (clientBuild >= 638 && serverBuild >= 638)
      anySkippedOver = dataIn.readBoolean();

    // read MsgLinkRecords first
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      linkRecords = new MsgLinkRecord[0];
    else {
      linkRecords = new MsgLinkRecord[dataIn.readShort()];

      for (int i=0; i<linkRecords.length; i++) {
        linkRecords[i] = new MsgLinkRecord();
        linkRecords[i].msgLinkId = dataIn.readLongObj();
        linkRecords[i].msgId = dataIn.readLongObj();
        linkRecords[i].ownerObjId = dataIn.readLongObj();
        linkRecords[i].ownerObjType = dataIn.readSmallint();
        linkRecords[i].setEncSymmetricKey(dataIn.readSymCipherBulk());
        linkRecords[i].setRecPubKeyId(dataIn.readLongObj());
        linkRecords[i].status = dataIn.readSmallint();
        linkRecords[i].dateCreated = dataIn.readTimestamp();
        if (clientBuild >= 220 && serverBuild >= 220)
          linkRecords[i].dateDelivered = dataIn.readTimestamp();
        linkRecords[i].dateUpdated = dataIn.readTimestamp();
      }
    }

    // read MsgDataRecords secord
    // read indicator
    indicator = dataIn.read();
    if (indicator == 0)
      dataRecords = new MsgDataRecord[0];
    else {
      dataRecords = new MsgDataRecord[dataIn.readShort()];

      for (int i=0; i<dataRecords.length; i++) {
        dataRecords[i] = new MsgDataRecord();
        dataRecords[i].msgId = dataIn.readLongObj();
        dataRecords[i].objType = dataIn.readSmallint();
        dataRecords[i].replyToMsgId = dataIn.readLongObj();
        dataRecords[i].importance = dataIn.readSmallint();
        dataRecords[i].attachedFiles = dataIn.readSmallint();
        dataRecords[i].attachedMsgs = dataIn.readSmallint();
        dataRecords[i].senderUserId = dataIn.readLongObj();
        dataRecords[i].setRawRecipients(dataIn.readBytes());
        dataRecords[i].setEncSubject(dataIn.readSymCipherBulk());
        dataRecords[i].setEncText(dataIn.readSymCipherBulk());
        dataRecords[i].setFlags(dataIn.readSmallint());
        dataRecords[i].setEncSignedDigest(dataIn.readSymCipherBulk());
        dataRecords[i].setEncEncDigest(dataIn.readSymCipherBulk());
        dataRecords[i].setSendPrivKeyId(dataIn.readLongObj());
        dataRecords[i].dateCreated = dataIn.readTimestamp();
        if (clientBuild >= 220 && serverBuild >= 220)
          dataRecords[i].dateExpired = dataIn.readTimestamp();
        dataRecords[i].recordSize = dataIn.readInteger();
        if (clientBuild >= 310 && serverBuild >= 310)
          dataRecords[i].bodyPassHint = dataIn.readString();
        if (clientBuild >= 250 && serverBuild >= 250)
          dataRecords[i].bodyPassHash = dataIn.readLongObj();
      }
    }
    indicator = dataIn.read();
    if (indicator == 0)
      stats_rp = null;
    else {
      stats_rp = new Stats_Get_Rp();
      stats_rp.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    }

    // for compatibility when connecting to older engines that build 388
    if (serverBuild < 388) {
      if (ownerObjType == null && ownerObjId == null && linkRecords != null && linkRecords.length > 0) {
        ownerObjType = linkRecords[0].ownerObjType;
        ownerObjId = linkRecords[0].ownerObjId;
      }
    }

    if (trace != null) trace.exit(Msg_GetLinkAndData_Rp.class);
  } // end initFromStream()


  public String toString() {
    return "[Msg_GetLinkAndData_Rp"
        + ": ownerObjType="   + ownerObjType
        + ", ownerObjId="     + ownerObjId
        + ", fetchNumMax="    + fetchNumMax
        + ", fetchNumNew="    + fetchNumNew
        + ", timestamp="      + timestamp
        + ", anySkippedOver=" + anySkippedOver
        + ", linkRecords="    + Misc.objToStr(linkRecords)
        + ", dataRecords="    + Misc.objToStr(dataRecords)
        + ", stats_rp="       + stats_rp
        + "]";
  }

}