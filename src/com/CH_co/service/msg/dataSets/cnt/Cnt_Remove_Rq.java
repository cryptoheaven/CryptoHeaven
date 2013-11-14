/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.cnt;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.obj.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Cnt_Remove_Rq extends ProtocolMsgDataSet {

  // <numOfContacts> { <contactId> }+ <numOfShares> { <shareId> }+
  public Obj_IDList_Co contactIDs;
  public Obj_IDList_Co shareIDs;
  
  /** Creates new Cnt_Remove_Rq */
  public Cnt_Remove_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_Remove_Rq.class, "Cnt_Remove_Rq()");
    if (trace != null) trace.exit(Cnt_Remove_Rq.class);
  }
 
  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_Remove_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    contactIDs.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    shareIDs.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    
    if (trace != null) trace.exit(Cnt_Remove_Rq.class);
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_Remove_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    contactIDs = new Obj_IDList_Co();
    contactIDs.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    shareIDs = new Obj_IDList_Co();
    shareIDs.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    
    if (trace != null) trace.exit(Cnt_Remove_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Cnt_Remove_Rq"
      + ": contactIDs=" + contactIDs
      + ", shareIDs="   + shareIDs
      + "]";
  }

}