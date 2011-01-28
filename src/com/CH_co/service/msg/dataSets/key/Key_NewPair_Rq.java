/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
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
import com.CH_co.cryptx.RSAPublicKey;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * New Key Pair Request
 * @author  Marcin Kurzawa
 * @version
 */
public class Key_NewPair_Rq extends ProtocolMsgDataSet {
  // <folderId> <encPrivateKey> <plainPublicKey>
  public KeyRecord keyRecord;

  /** Creates new Key_NewPair_Rq */
  public Key_NewPair_Rq() {
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    dataOut.writeLongObj(keyRecord.folderId);
    dataOut.writeBytes(keyRecord.getEncPrivateKey());
    dataOut.writeBytes(keyRecord.plainPublicKey.objectToBytes());
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    keyRecord = new KeyRecord();
    keyRecord.folderId = dataIn.readLongObj();
    keyRecord.setEncPrivateKey(dataIn.readSymCipherBlock());
    keyRecord.plainPublicKey = RSAPublicKey.bytesToObject(dataIn.readBytes());
  } // end initFromStream()

}