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

package com.CH_co.service.msg.dataSets.sys;

import java.io.IOException;

//import com.CH_co.cryptx.*;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Sys_AutoUpdate_Co extends ProtocolMsgDataSet {

  // <numOfAutoUpdates> { <record data> }+
  public AutoUpdateRecord[] updateRecords;
  public Obj_List_Co dataSet;

  /** Creates new Sys_AutoUpdate_Co */
  public Sys_AutoUpdate_Co() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sys_AutoUpdate_Co.class, "Sys_AutoUpdate_Co()");
    if (trace != null) trace.exit(Sys_AutoUpdate_Co.class);
  }

  /** Creates new Sys_AutoUpdate_Co */
  public Sys_AutoUpdate_Co(AutoUpdateRecord[] updateRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sys_AutoUpdate_Co.class, "Sys_AutoUpdate_Co(AutoUpdateRecord[] updateRecords)");
    this.updateRecords = updateRecords;
    if (trace != null) trace.exit(Sys_AutoUpdate_Co.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sys_AutoUpdate_Co.class, "writeToStream(DataOutputStream2, ProgMonitor, clientBuild, serverBuild)");
    // write indicator
    if (updateRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(updateRecords.length);
      for (int i=0; i<updateRecords.length; i++) {
        dataOut.writeLongObj(updateRecords[i].id);
        dataOut.writeString(updateRecords[i].hashStr);
        dataOut.writeString(updateRecords[i].hashAlg);
        dataOut.writeSmallint(updateRecords[i].build);
        dataOut.writeSmallint(updateRecords[i].applyFrom);
        dataOut.writeSmallint(updateRecords[i].applyTo);
        dataOut.writeString(updateRecords[i].oldFile);
        dataOut.writeString(updateRecords[i].newFile);
        dataOut.writeString(updateRecords[i].locFile);
        dataOut.writeInteger(updateRecords[i].size);
        dataOut.writeTimestamp(updateRecords[i].dateExpired);
      }
    }
    if (dataSet == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataSet.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }
    if (trace != null) trace.exit(Sys_AutoUpdate_Co.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sys_AutoUpdate_Co.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild, serverBuild)");
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      updateRecords = new AutoUpdateRecord[0];
    else {
      updateRecords = new AutoUpdateRecord[dataIn.readShort()];
      for (int i=0; i<updateRecords.length; i++) {
        updateRecords[i] = new AutoUpdateRecord();
        updateRecords[i].id = dataIn.readLongObj();
        updateRecords[i].hashStr = dataIn.readString();
        updateRecords[i].hashAlg = dataIn.readString();
        updateRecords[i].build = dataIn.readSmallint();
        updateRecords[i].applyFrom = dataIn.readSmallint();
        updateRecords[i].applyTo = dataIn.readSmallint();
        updateRecords[i].oldFile = dataIn.readString();
        updateRecords[i].newFile = dataIn.readString();
        updateRecords[i].locFile = dataIn.readString();
        updateRecords[i].size = dataIn.readInteger();
        updateRecords[i].dateExpired = dataIn.readTimestamp();
      }
    }
    indicator = dataIn.read();
    if (indicator == 0)
      dataSet = null;
    else {
      dataSet = new Obj_List_Co();
      dataSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    }
    if (trace != null) trace.exit(Sys_AutoUpdate_Co.class);
  } // end initFromStream()

  public String toString() {
    return "[Sys_AutoUpdate_Co"
    + ": updateRecords="  + Misc.objToStr(updateRecords)
    + ", dataSet="        + dataSet
    + "]";
  }

}