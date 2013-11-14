/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.obj;

import java.io.IOException;
import java.util.List;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 * 
 * User: Get User Handles: <numOfIDs> { <userId> }+
 *
 *
 * @author  Marcin Kurzawa
 */
public class Obj_IDList_Co extends ProtocolMsgDataSet {

  // <numOfIDs> { <objId> }+
  public Long[] IDs;

  /** Creates new Obj_IDList_Co */
  public Obj_IDList_Co() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDList_Co.class, "Obj_IDList_Co()");
    if (trace != null) trace.exit(Obj_IDList_Co.class);
  }
  /** Creates new Obj_IDList_Co */
  public Obj_IDList_Co(Long ID) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDList_Co.class, "Obj_IDList_Co(Long ID)");
    if (trace != null) trace.args(ID);
    this.IDs = new Long[] { ID };
    if (trace != null) trace.exit(Obj_IDList_Co.class);
  }
  /** Creates new Obj_IDList_Co */
  public Obj_IDList_Co(Long[] IDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDList_Co.class, "Obj_IDList_Co(Long[] IDs)");
    if (trace != null) trace.args(IDs);
    this.IDs = IDs;
    if (trace != null) trace.exit(Obj_IDList_Co.class);
  }
  /** Creates new Obj_IDList_Co */
  public Obj_IDList_Co(List IDsL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDList_Co.class, "Obj_IDList_Co(List IDsL)");
    if (trace != null) trace.args(IDsL);
    this.IDs = (Long[]) ArrayUtils.toArray(IDsL, Long.class);
    if (trace != null) trace.exit(Obj_IDList_Co.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDList_Co.class, "writeToStream(DataOutputStream2)");
    // write indicator
    if (IDs == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(IDs.length);
      for (int i=0; i<IDs.length; i++)
        dataOut.writeLongObj(IDs[i]);
    }

    if (trace != null) trace.exit(Obj_IDList_Co.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDList_Co.class, "initFromStream(DataInputStream2)");
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      IDs = new Long[0];
    else {
      IDs = new Long[dataIn.readShort()];

      for (int i=0; i<IDs.length; i++)
        IDs[i] = dataIn.readLongObj();
    }
    if (trace != null) trace.exit(Obj_IDList_Co.class);
  } // end initFromStream()


  public String toString() {
    return "[Obj_IDList_Co"
      + ": IDs=" + Misc.objToStr(IDs)
      + "]";
  }
}