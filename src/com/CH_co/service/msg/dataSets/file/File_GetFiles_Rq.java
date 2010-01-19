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

package com.CH_co.service.msg.dataSets.file;

import com.CH_co.io.*;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

import java.io.IOException;
import java.sql.Timestamp;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class File_GetFiles_Rq extends ProtocolMsgDataSet {

  public static final short FETCH_NUM_LIST__INITIAL_SIZE = 500;
  public static final short FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT = 10000;

  // <shareId> <ownerObjType> <ownerObjId> <fetchNumMax> <timestamp> { <exceptLinkIDs }*
  public Long shareId;
  public Short ownerObjType;
  public Long ownerObjId;
  public Short fetchNumMax;
  public Timestamp timestamp;
  public Long[] exceptLinkIDs;

  /** Creates new File_GetFiles_Rq */
  public File_GetFiles_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetFiles_Rq.class, "File_GetFiles_Rq()");
    if (trace != null) trace.exit(File_GetFiles_Rq.class);
  }

  public File_GetFiles_Rq(Long shareId, short ownerObjType, Long ownerObjId, short fetchNumMax, Timestamp timestamp) {
    this(shareId, ownerObjType, ownerObjId, new Short(fetchNumMax), timestamp);
  }
  public File_GetFiles_Rq(Long shareId, short ownerObjType, Long ownerObjId, Short fetchNumMax, Timestamp timestamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetFiles_Rq.class, "File_GetFiles_Rq(Long shareId, short ownerObjType, Long ownerObjId, Short fetchNumMax, Timestamp timestamp)");
    if (trace != null) trace.args(shareId);
    if (trace != null) trace.args(ownerObjType);
    if (trace != null) trace.args(ownerObjId);
    if (trace != null) trace.args(fetchNumMax);
    if (trace != null) trace.args(timestamp);

    this.shareId = shareId;
    this.ownerObjType = new Short(ownerObjType);
    this.ownerObjId = ownerObjId;
    this.fetchNumMax = fetchNumMax;
    this.timestamp = timestamp;

    if (trace != null) trace.exit(File_GetFiles_Rq.class);
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetFiles_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    dataOut.writeLongObj(shareId);
    dataOut.writeSmallint(ownerObjType);
    dataOut.writeLongObj(ownerObjId);
    dataOut.writeSmallint(fetchNumMax);
    dataOut.writeTimestamp(timestamp);
    new Obj_IDList_Co(exceptLinkIDs).writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);

    if (trace != null) trace.exit(File_GetFiles_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetFiles_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    shareId = dataIn.readLongObj();
    ownerObjType = dataIn.readSmallint();
    ownerObjId = dataIn.readLongObj();
    fetchNumMax = dataIn.readSmallint();
    timestamp = dataIn.readTimestamp();
    Obj_IDList_Co exceptLinkIDsSet = new Obj_IDList_Co();
    exceptLinkIDsSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    exceptLinkIDs = exceptLinkIDsSet.IDs;

    if (trace != null) trace.exit(File_GetFiles_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[File_GetFiles_Rq"
        + ": shareId="      + shareId
        + ", ownerObjType=" + ownerObjType
        + ", ownerObjId="   + ownerObjId
        + ", fetchNumMax="  + fetchNumMax
        + ", timestamp="    + timestamp
        + ", exceptLinkIDs=" + Misc.objToStr(exceptLinkIDs)
        + "]";
  }

}