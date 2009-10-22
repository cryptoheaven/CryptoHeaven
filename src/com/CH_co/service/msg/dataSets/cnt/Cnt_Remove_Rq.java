/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.service.msg.dataSets.cnt;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.obj.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Cnt_Remove_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    contactIDs.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    shareIDs.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    
    if (trace != null) trace.exit(Cnt_Remove_Rq.class);
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
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