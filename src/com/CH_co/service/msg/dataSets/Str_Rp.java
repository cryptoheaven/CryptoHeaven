/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 * 
 * String Reply
 * @author  Marcin Kurzawa
 */
public class Str_Rp extends ProtocolMsgDataSet {

  public String message;

  /** Creates new Str_Rp */
  public Str_Rp() {
  }

  /** Creates new Str_Rp */
  public Str_Rp(String message) {
    this.message = message;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    dataOut.writeString(message);
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    message = dataIn.readString();
  } // end initFromStream()
  
  public String toString() {
    return "[Str_Rp"
      + ": message=" + message
      + "]";
  }
}
