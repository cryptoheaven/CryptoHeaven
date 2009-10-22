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

package com.CH_co.service.msg.dataSets.addr;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.*;
import com.CH_co.util.Misc;

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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Addr_GetHash_Rp extends ProtocolMsgDataSet {

  // <numOfHashes> { <addrHashId> <msgId> <hash> }+
  public AddrHashRecord[] addrHashRecs;

  /** Creates new Addr_GetHash_Rp */
  public Addr_GetHash_Rp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Addr_GetHash_Rp.class, "Addr_GetHash_Rp()");
    if (trace != null) trace.exit(Addr_GetHash_Rp.class);
  }
  public Addr_GetHash_Rp(AddrHashRecord[] addrHashRecs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Addr_GetHash_Rp.class, "Addr_GetHash_Rp()");
    this.addrHashRecs = addrHashRecs;
    if (trace != null) trace.exit(Addr_GetHash_Rp.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Addr_GetHash_Rp.class, "writeToStream(DataOutputStream2, ProgMonitor, clientBuild, serverBuild)");

    // write indicator
    if (addrHashRecs == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(addrHashRecs.length);

      for (int i=0; i<addrHashRecs.length; i++) {
        dataOut.writeLongObj(addrHashRecs[i].addrHashId);
        dataOut.writeLongObj(addrHashRecs[i].msgId);
        dataOut.writeBytes(addrHashRecs[i].hash);
      }
    }

    if (trace != null) trace.exit(Addr_GetHash_Rp.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Addr_GetHash_Rp.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild, serverBuild)");

    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      addrHashRecs = new AddrHashRecord[0];
    else {

      addrHashRecs = new AddrHashRecord[dataIn.readShort()];

      for (int i=0; i<addrHashRecs.length; i++) {
        addrHashRecs[i] = new AddrHashRecord();
        addrHashRecs[i].addrHashId = dataIn.readLongObj();
        addrHashRecs[i].msgId = dataIn.readLongObj();
        addrHashRecs[i].hash = dataIn.readDigestBlock();
      }
    }

    if (trace != null) trace.exit(Addr_GetHash_Rp.class);
  } // end initFromStream()


  public String toString() {
    return "[Addr_GetHash_Rp"
    + ": addrHashRecs=" + Misc.objToStr(addrHashRecs)
    + "]";
  }

}