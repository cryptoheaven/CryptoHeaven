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

package com.CH_co.service.msg.dataSets.obj;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.trace.Trace;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

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
public class Obj_IDAndIDList_Rq extends ProtocolMsgDataSet {

  // <id> <numberOfIDs> { <id> }+
  public Long id;
  public Obj_IDList_Co IDs = new Obj_IDList_Co();
  
  
  /** Creates new File_CopyMove_Rq */
  public Obj_IDAndIDList_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDAndIDList_Rq.class, "Obj_IDAndIDList_Rq()");
    if (trace != null) trace.exit(Obj_IDAndIDList_Rq.class);
  }
 
  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDAndIDList_Rq.class, "writeToStream(DataOutputStream2)");
    dataOut.writeLongObj(id);
    // write the ID list
    IDs.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    
    if (trace != null) trace.exit(Obj_IDAndIDList_Rq.class);
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDAndIDList_Rq.class, "initFromStream(DataInputStream2)");
    id = dataIn.readLongObj();
    // read the ID list
    IDs.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    
    if (trace != null) trace.exit(Obj_IDAndIDList_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Obj_IDAndIDList_Rq"
      + ": Id=" + id
      + ", IDs=" + IDs
      + "]";
  }

}