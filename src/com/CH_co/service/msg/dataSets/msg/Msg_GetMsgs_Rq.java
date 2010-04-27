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

package com.CH_co.service.msg.dataSets.msg;

import java.io.IOException;
import java.sql.Timestamp;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Get Message Summaries
 * @author  Marcin Kurzawa
 * @version
 */
public class Msg_GetMsgs_Rq extends ProtocolMsgDataSet {

  public static final short FETCH_NUM_LIST__INITIAL_SIZE = 15;
  public static final short FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT = 1000;

  public static final short FETCH_NUM_NEW__INITIAL_SIZE = 5;
  public static final short FETCH_NUM_NEW__MAX_SIZE__HARD_LIMIT = 100;

  // <shareId> <ownerObjType> <ownerObjId> <fetchNumMax> <fetchNumNew> <timestamp> { <exceptLinkIDs }*
  public Long shareId;
  public Short ownerObjType;
  public Long ownerObjId;
  public Short fetchNumMax;
  public Short fetchNumNew;
  public Timestamp timestamp;
  public Long[] exceptLinkIDs;

  /** Creates new Msg_GetMsgs_Rq */
  public Msg_GetMsgs_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetMsgs_Rq.class, "Msg_GetMsgs_Rq()");
    if (trace != null) trace.exit(Msg_GetMsgs_Rq.class);
  }

  public Msg_GetMsgs_Rq(Long shareId, short ownerObjType, Long ownerObjId, short fetchNumMax, short fetchNumNew, Timestamp timestamp) {
    this(shareId, ownerObjType, ownerObjId, new Short(fetchNumMax), fetchNumNew, timestamp);
  }
  public Msg_GetMsgs_Rq(Long shareId, short ownerObjType, Long ownerObjId, Short fetchNumMax, short fetchNumNew, Timestamp timestamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetMsgs_Rq.class, "Msg_GetMsgs_Rq(Long shareId, short ownerObjType, Long ownerObjId, Short fetchNumMax, short fetchNumNew, Timestamp timestamp)");
    if (trace != null) trace.args(shareId);
    if (trace != null) trace.args(ownerObjType);
    if (trace != null) trace.args(ownerObjId);
    if (trace != null) trace.args(fetchNumMax);
    if (trace != null) trace.args(fetchNumNew);
    if (trace != null) trace.args(timestamp);

    this.shareId = shareId;
    this.ownerObjType = new Short(ownerObjType);
    this.ownerObjId = ownerObjId;
    this.fetchNumMax = fetchNumMax;
    this.fetchNumNew = new Short(fetchNumNew);
    this.timestamp = timestamp;

    if (trace != null) trace.exit(Msg_GetMsgs_Rq.class);
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetMsgs_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    dataOut.writeLongObj(shareId);
    dataOut.writeSmallint(ownerObjType);
    dataOut.writeLongObj(ownerObjId);
    dataOut.writeSmallint(fetchNumMax);
    if (serverBuild <= 330)
      fetchNumNew = new Short(FETCH_NUM_NEW__INITIAL_SIZE);
    dataOut.writeSmallint(fetchNumNew);
    dataOut.writeTimestamp(timestamp);
    if (clientBuild >= 364 && serverBuild >= 364) {
      new Obj_IDList_Co(exceptLinkIDs).writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }

    if (trace != null) trace.exit(Msg_GetMsgs_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_GetMsgs_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    shareId = dataIn.readLongObj();
    ownerObjType = dataIn.readSmallint();
    ownerObjId = dataIn.readLongObj();
    fetchNumMax = dataIn.readSmallint();
    fetchNumNew = dataIn.readSmallint();
    timestamp = dataIn.readTimestamp();
    if (clientBuild >= 364 && serverBuild >= 364) {
      Obj_IDList_Co exceptLinkIDsSet = new Obj_IDList_Co();
      exceptLinkIDsSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
      exceptLinkIDs = exceptLinkIDsSet.IDs;
    }

    if (trace != null) trace.exit(Msg_GetMsgs_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Msg_GetMsgs_Rq"
        + ": shareId="      + shareId
        + ", ownerObjType=" + ownerObjType
        + ", ownerObjId="   + ownerObjId
        + ", fetchNumMax="  + fetchNumMax
        + ", fetchNumNew="  + fetchNumNew
        + ", timestamp="    + timestamp
        + ", exceptLinkIDs=" + Misc.objToStr(exceptLinkIDs)
        + "]";
  }

}