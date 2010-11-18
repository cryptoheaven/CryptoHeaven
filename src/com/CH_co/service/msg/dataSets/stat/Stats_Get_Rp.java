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

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.Misc;

import com.CH_co.trace.Trace;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.*;

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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Stats_Get_Rp extends ProtocolMsgDataSet {

  // <numOfStats> { <ownerUserId> <objType> <objId> <objLinkId> <mark> <firstSeen> <firstDelivered> }*
  public StatRecord[] stats;

  // Only by special request we will pass the statId, otherwise the objLinkId will become the statId.
  private boolean passStatId = false;
  private boolean isTraceRecord = false;

  // When fetching trace records this flag will tell us if any records are hidden due to BCC
  public boolean isAnyBCCskipped = false;

  /** Creates new Stats_Get_Rp */
  public Stats_Get_Rp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Get_Rp.class, "Stats_Get_Rp()");
    if (trace != null) trace.exit(Stats_Get_Rp.class);
  }
  /** Creates new Stats_Get_Rp */
  public Stats_Get_Rp(StatRecord statRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Get_Rp.class, "Stats_Get_Rp(StatRecord statRecord)");
    if (statRecord != null)
      stats = new StatRecord[] { statRecord };
    if (trace != null) trace.exit(Stats_Get_Rp.class);
  }
  /** Creates new Stats_Get_Rp */
  public Stats_Get_Rp(StatRecord[] statRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Get_Rp.class, "Stats_Get_Rp(StatRecord[] statRecords)");
    stats = statRecords;
    if (trace != null) trace.exit(Stats_Get_Rp.class);
  }

public void setIsAnyBCCskipped(boolean isBCCskipped) {
    isAnyBCCskipped = isBCCskipped;
  }
public void setIsTraceRecord(boolean isTrace) {
    isTraceRecord = isTrace;
  }
  public void setPassStatId(boolean pass) {
    passStatId = pass;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Get_Rp.class, "writeToStream(DataOutputStream2, ProgMonitor, clientBuild)");
    if (trace != null) trace.args(clientBuild);
    // write indicator
    if (stats == null)
      dataOut.write(0);
    else {
      dataOut.write(isTraceRecord ? 2 : 1);

      dataOut.writeShort(stats.length);
      for (int i=0; i<stats.length; i++ ) {
        if (passStatId)
          dataOut.writeLongObj(stats[i].statId);
        else
          dataOut.writeLongObj(stats[i].objLinkId);
        dataOut.writeLongObj(stats[i].ownerUserId);
        if (clientBuild >= 54)
          dataOut.writeByteObj(stats[i].objType);
        dataOut.writeLongObj(stats[i].objId);
        dataOut.writeLongObj(stats[i].objLinkId);
        // hide the BCC bit from the client
        Short mark = stats[i].mark;
        if (mark != null) {
          mark = (Short) Misc.setBitObj(false, mark, StatRecord.FLAG_BCC);
          // clients prior to build 584 don't know how to handle FLAG_MARKED_NEW so overload it to FLAG_NEW
          if (clientBuild < 584) {
            if (mark.equals(StatRecord.FLAG_MARKED_NEW))
              mark = StatRecord.FLAG_NEW;
          }
        }
        dataOut.writeSmallint(mark);
        dataOut.writeTimestamp(stats[i].firstSeen);
        dataOut.writeTimestamp(stats[i].firstDelivered);
        if (isTraceRecord) {
          TraceRecord tRec = (TraceRecord) stats[i];
          dataOut.writeBoolean(tRec.hasReadAccess);
          dataOut.writeBoolean(tRec.hasHistoryRecord);
        }
      }
    }
    if (clientBuild >= 380 && serverBuild >= 380)
      dataOut.writeBoolean(isAnyBCCskipped);

    if (trace != null) trace.exit(Stats_Get_Rp.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats_Get_Rp.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild)");
    if (trace != null) trace.args(clientBuild);
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      stats = new StatRecord[0];
    else {
      isTraceRecord = indicator == 2;
      short arrLen = dataIn.readShort();
      stats = isTraceRecord ? new TraceRecord[arrLen] : new StatRecord[arrLen];

      for (int i=0; i<stats.length; i++) {
        stats[i] = isTraceRecord ? new TraceRecord() : new StatRecord();
        stats[i].statId = dataIn.readLongObj();
        stats[i].ownerUserId = dataIn.readLongObj();
        stats[i].objType = dataIn.readByteObj();
        stats[i].objId = dataIn.readLongObj();
        stats[i].objLinkId = dataIn.readLongObj();
        stats[i].mark = dataIn.readSmallint();
        stats[i].firstSeen = dataIn.readTimestamp();
        stats[i].firstDelivered = dataIn.readTimestamp();
        if (isTraceRecord) {
          TraceRecord tRec = (TraceRecord) stats[i];
          tRec.hasReadAccess = dataIn.readBoolean();
          tRec.hasHistoryRecord = dataIn.readBoolean();
        }
      }
    }
    if (clientBuild >= 380 && serverBuild >= 380)
      isAnyBCCskipped = dataIn.readBoolean();

    if (trace != null) trace.exit(Stats_Get_Rp.class);
  } // end initFromStream()


  public String toString() {
    return "[Stats_Get_Rp"
      + ": stats=" + Misc.objToStr(stats)
      + "]";
  }

}