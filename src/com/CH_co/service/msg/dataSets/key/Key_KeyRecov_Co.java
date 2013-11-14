/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.key;

import java.io.IOException;

import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.KeyRecoveryRecord;
import com.CH_co.util.Misc;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * Get Key Recovery Records
 * @author  Marcin Kurzawa
 * @version
 */
public class Key_KeyRecov_Co extends ProtocolMsgDataSet {
  // <numOfRecovKeys> { <keyRecovId> <keyId> <masterUID> <masterKeyId> <encSymKey> <encPvtKey> <created> <updated> }+
  public KeyRecoveryRecord[] recoveryRecords;

  /** Creates new Key_KeyRecov_Co */
  public Key_KeyRecov_Co() {
  }
  /** Creates new Key_KeyRecov_Co */
  public Key_KeyRecov_Co(KeyRecoveryRecord recoveryRecord) {
    this.recoveryRecords = new KeyRecoveryRecord[] { recoveryRecord };
  }
  /** Creates new Key_KeyRecov_Co */
  public Key_KeyRecov_Co(KeyRecoveryRecord[] recoveryRecords) {
    this.recoveryRecords = recoveryRecords;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // write indicator
    if (recoveryRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(recoveryRecords.length);
      for (int i=0; i<recoveryRecords.length; i++) {
        dataOut.writeLongObj(recoveryRecords[i].keyRecovId);
        dataOut.writeLongObj(recoveryRecords[i].keyId);
        dataOut.writeLongObj(recoveryRecords[i].masterUID);
        dataOut.writeLongObj(recoveryRecords[i].masterKeyId);
        dataOut.writeBytes(recoveryRecords[i].getEncSymKey());
        dataOut.writeBytes(recoveryRecords[i].getEncPvtKey());
        dataOut.writeTimestamp(recoveryRecords[i].dateCreated);
        dataOut.writeTimestamp(recoveryRecords[i].dateUpdated);
      }
    }
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      recoveryRecords = null;
    else {
      recoveryRecords = new KeyRecoveryRecord[dataIn.readShort()];
      for (int i=0; i<recoveryRecords.length; i++) {
        recoveryRecords[i] = new KeyRecoveryRecord();
        recoveryRecords[i].keyRecovId = dataIn.readLongObj();
        recoveryRecords[i].keyId = dataIn.readLongObj();
        recoveryRecords[i].masterUID = dataIn.readLongObj();
        recoveryRecords[i].masterKeyId = dataIn.readLongObj();
        recoveryRecords[i].setEncSymKey(dataIn.readAsyCipherBlock());
        recoveryRecords[i].setEncPvtKey(dataIn.readSymCipherBlock());
        recoveryRecords[i].dateCreated = dataIn.readTimestamp();
        recoveryRecords[i].dateUpdated = dataIn.readTimestamp();
      }
    }
  } // end initFromStream()


  public String toString() {
    return "[Key_KeyRecov_Co"
      + ", recoveryRecords=" + Misc.objToStr(recoveryRecords)
      + "]";
  }
}