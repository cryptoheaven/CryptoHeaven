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

package com.CH_co.service.msg.dataSets.key;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 * 
 * Get Public Keys Request
 * @author  Marcin Kurzawa
 * @version 
 */
public class Key_KeyIDs_Rq extends ProtocolMsgDataSet {
  // <numOfKeyIDs> { <keyId> }+
  public KeyRecord[] keyRecords;
  
  /** Creates new Key_KeyIDs_Rq */
  public Key_KeyIDs_Rq() {
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    dataOut.writeShort(keyRecords.length);

    for (int i=0; i<keyRecords.length; i++) {
      dataOut.writeLongObj(keyRecords[i].keyId);
    }

  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    keyRecords = new KeyRecord[dataIn.readShort()];

    for (int i=0; i<keyRecords.length; i++) {
      keyRecords[i] = new KeyRecord();
      keyRecords[i].keyId = dataIn.readLongObj();
    }
  } // end initFromStream()

}
