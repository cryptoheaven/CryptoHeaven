/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.stat;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Stats_Get_Rq extends ProtocolMsgDataSet {

  // <statsForObjType> <ownerObjType> <numberOfOwners> { <ownerObjId> }+ <numberOfLinks> { <objLinkId> }+
  public Short statsForObjType;
  public Short ownerObjType;
  public Long[] ownerObjIDs;
  public Long[] objLinkIDs;

  /** Creates new Stats_Get_Rq */
  public Stats_Get_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Get_Rq.class, "Stats_Get_Rq()");
    if (trace != null) trace.exit(Stats_Get_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Get_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    dataOut.writeSmallint(statsForObjType);
    dataOut.writeSmallint(ownerObjType);

    dataOut.writeShort(ownerObjIDs.length);
    for (int i=0; i<ownerObjIDs.length; i++)
      dataOut.writeLongObj(ownerObjIDs[i]);

    dataOut.writeShort(objLinkIDs.length);
    for (int i=0; i<objLinkIDs.length; i++)
      dataOut.writeLongObj(objLinkIDs[i]);

    if (trace != null) trace.exit(Stats_Get_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Get_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    statsForObjType = dataIn.readSmallint();
    ownerObjType = dataIn.readSmallint();

    ownerObjIDs = new Long[dataIn.readShort()];
    for (int i=0; i<ownerObjIDs.length; i++)
      ownerObjIDs[i] = dataIn.readLongObj();

    objLinkIDs = new Long [dataIn.readShort()];
    for (int i=0; i<objLinkIDs.length; i++)
      objLinkIDs[i] = dataIn.readLongObj();

    if (trace != null) trace.exit(Stats_Get_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Stats_Get_Rq"
      + ": ownerObjType=" + ownerObjType
      + ", ownerObjIDs="  + Misc.objToStr(ownerObjIDs)
      + ", objLinkIDs="   + Misc.objToStr(objLinkIDs)
      + "]";
  }

}