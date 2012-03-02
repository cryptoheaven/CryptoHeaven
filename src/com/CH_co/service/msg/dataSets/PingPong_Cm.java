/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.service.msg.dataSets;

import java.io.IOException;
import java.util.Date;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.Stats;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
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
  public Long lastPingMS;

  /** Creates new PingPong_Cm */
  public PingPong_Cm() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PingPong_Cm.class, "PingPong_Cm()");
    if (trace != null) trace.exit(PingPong_Cm.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PingPong_Cm.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    dataOut.writeDate(date != null ? date : new Date());
    if (clientBuild >= 574 && serverBuild >= 574) {
      dataOut.writeLongObj(lastPingMS != null ? lastPingMS : Stats.getPingMS());
    }

    if (trace != null) trace.exit(PingPong_Cm.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PingPong_Cm.class, "initFromStream(DataInputStream2, ProgMonitor)");

    date = dataIn.readDate();
    if (clientBuild >= 574 && serverBuild >= 574) {
      lastPingMS = dataIn.readLongObj();
    }

    if (trace != null) trace.exit(PingPong_Cm.class);
  } // end initFromStream()


  public String toString() {
    return "[PingPong_Cm"
      + ": date=" + date
      + ", lastPingMS=" + lastPingMS
      + "]";
  }

}