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

package com.CH_co.service.msg.dataSets.stat;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.StatRecord;

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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Stats_Update_Rq extends ProtocolMsgDataSet {

  // <numberOfStats> { <objType> <objLinkId> <mark> }+
  public StatRecord[] stats;

  /** Creates new Stats_Update_Rq */
  public Stats_Update_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Update_Rq.class, "Stats_Update_Rq()");
    if (trace != null) trace.exit(Stats_Update_Rq.class);
  }
  /** Creates new Stats_Update_Rq */
  public Stats_Update_Rq(StatRecord[] statRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Update_Rq.class, "Stats_Update_Rq(StatRecord[] statRecords)");
    if (trace != null) trace.args(statRecords);
    stats = statRecords;
    if (trace != null) trace.exit(Stats_Update_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Update_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    dataOut.writeShort(stats.length);
    for (int i=0; i<stats.length; i++ ) {
      dataOut.writeByteObj(stats[i].objType);
      dataOut.writeLongObj(stats[i].objLinkId);
      dataOut.writeSmallint(stats[i].mark);
    }

    if (trace != null) trace.exit(Stats_Update_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Update_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    stats = new StatRecord[dataIn.readShort()];

    for (int i=0; i<stats.length; i++) {
      stats[i] = new StatRecord();
      if (clientBuild >= 54) 
        stats[i].objType = dataIn.readByteObj();
      stats[i].objLinkId = dataIn.readLongObj();
      stats[i].mark = dataIn.readSmallint();
    }

    if (trace != null) trace.exit(Stats_Update_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Stats_Update_Rq"
      + ": stats=" + Misc.objToStr(stats)
      + "]";
  }

}