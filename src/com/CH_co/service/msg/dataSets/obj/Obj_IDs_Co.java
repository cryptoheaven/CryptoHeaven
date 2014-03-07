/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.obj;

import java.io.IOException;

import com.CH_co.trace.Trace;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Obj_IDs_Co extends ProtocolMsgDataSet {

  // matrix of IDs
  // <numOfLongs> { <Long> <Long> ... }+
  public Long[][] IDs;

  /** Creates new Obj_IDs_Co */
  public Obj_IDs_Co() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDs_Co.class, "Obj_IDs_Co()");
    if (trace != null) trace.exit(Obj_IDs_Co.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDs_Co.class, "writeToStream(DataOutputStream2)");

    // write indicator
    if (IDs == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(IDs.length);
      for (int i=0; i<IDs.length; i++ ) {
        if (IDs[i] == null)
          dataOut.writeShort((short) -1);
        else {
          dataOut.writeShort(IDs[i].length);
          for (int k=0; k<IDs[i].length; k++) {
            dataOut.writeLongObj(IDs[i][k]);
          }
        }
      }
    }

    if (trace != null) trace.exit(Obj_IDs_Co.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_IDs_Co.class, "initFromStream(DataInputStream2)");

    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      IDs = new Long[0][0];
    else {
      IDs = new Long[dataIn.readShort()][];

      for (int i=0; i<IDs.length; i++) {
        short len = dataIn.readShort();
        if (len == -1)
          IDs[i] = null;
        else {
          IDs[i] = new Long[len];
          for (int k=0; k<IDs[i].length; k++) {
            IDs[i][k] = dataIn.readLongObj();
          }
        }
      }
    }
    if (trace != null) trace.exit(Obj_IDs_Co.class);
  } // end initFromStream()


  public String toString() {
    return "[Obj_IDs_Co"
      + ": IDs=" + Misc.objToStr(IDs)
      + "]";
  }

}