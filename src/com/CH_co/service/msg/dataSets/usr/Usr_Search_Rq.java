/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.usr;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Usr_Search_Rq extends ProtocolMsgDataSet {


  // <handleMode> <handle> <idMode> <userId> <includeEmailRecords>
  // Notes:  handle modes: null/0 = ignore, 1 = exact, 2 = partial, 3 = phonetic, 4 = ignore case,
  //         id modes: null/0 = ignore, 1 = exact, 2 = partial

  public int handleMode;
  public String handle;
  public int idMode;
  public Long userId;
  public boolean includeEmailRecords;

  /** Creates new Usr_Search_Rq */
  public Usr_Search_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_Search_Rq.class, "Usr_Search_Rq()");
    if (trace != null) trace.exit(Usr_Search_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_Search_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    dataOut.write(handleMode);
    dataOut.writeString(handle);
    dataOut.write(idMode);
    dataOut.writeLongObj(userId);
    if (clientBuild >= 182 && serverBuild >= 182)
      dataOut.writeBoolean(includeEmailRecords);

    if (trace != null) trace.exit(Usr_Search_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_Search_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    handleMode = dataIn.read();
    handle = dataIn.readString();
    idMode = dataIn.read();
    userId = dataIn.readLongObj();
    if (clientBuild >= 182 && serverBuild >= 182)
      includeEmailRecords = dataIn.readBoolean();

    if (trace != null) trace.exit(Usr_Search_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Usr_Search_Rq"
      + ": handleMode=" + handleMode
      + ", handle="     + handle
      + ", idMode="     + idMode
      + ", userId="     + userId
      + "]";
  }

}