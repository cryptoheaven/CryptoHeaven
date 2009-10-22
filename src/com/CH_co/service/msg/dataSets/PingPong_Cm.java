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

package com.CH_co.service.msg.dataSets;

import java.io.IOException;
import java.util.Date;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class PingPong_Cm extends ProtocolMsgDataSet {

  // <clientDate>
  public Date date;
  
  /** Creates new PingPong_Cm */
  public PingPong_Cm() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PingPong_Cm.class, "PingPong_Cm()");
    if (trace != null) trace.exit(PingPong_Cm.class);
  }
 
  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PingPong_Cm.class, "writeToStream(DataOutputStream2, ProgMonitor)");
    
    dataOut.writeDate(date != null ? date : new Date());
    
    if (trace != null) trace.exit(PingPong_Cm.class);
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PingPong_Cm.class, "initFromStream(DataInputStream2, ProgMonitor)");
    
    date = dataIn.readDate();
    
    if (trace != null) trace.exit(PingPong_Cm.class);
  } // end initFromStream()


  public String toString() {
    return "[PingPong_Cm"
      + ": date=" + date
      + "]";
  }

}