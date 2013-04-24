/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.service.msg.dataSets.sys;

import java.io.*;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/**
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Sys_Check_Co extends ProtocolMsgDataSet {

  // <protocol> <numOfElements> { <data> }+ <visualized>
  public String str;

  /** Creates new Sys_Check_Co */
  public Sys_Check_Co() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sys_Check_Co.class, "Sys_Check_Co()");
    if (trace != null) trace.exit(Sys_Check_Co.class);
  }
  /** Creates new Sys_Check_Co */
  public Sys_Check_Co(String str) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sys_Check_Co.class, "Sys_Check_Co()");
    this.str = str;
    if (trace != null) trace.exit(Sys_Check_Co.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sys_Check_Co.class, "writeToStream(DataOutputStream2, ProgMonitor, clientBuild, serverBuild)");

    PrintWriter pw = new PrintWriter(dataOut);
    pw.println(str);
    pw.flush();

    if (trace != null) trace.exit(Sys_Check_Co.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Sys_Check_Co.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild, serverBuild)");

    StringBuffer sb = new StringBuffer();
    int ch = -1;
    while (true) {
      ch = dataIn.read();
      if (ch < 0)
        break;
      boolean lineBreak = ch == '~';
      if (lineBreak)
        break;
      else
        sb.append((char) ch);
    }
    str = sb.toString();

    if (trace != null) trace.exit(Sys_Check_Co.class);
  } // end initFromStream()


  public String toString() {
    return "[Sys_Check_Co"
    + ": str=" + str
    + "]";
  }

}