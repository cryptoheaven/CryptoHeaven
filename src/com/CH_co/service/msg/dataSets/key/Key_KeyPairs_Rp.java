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

import com.CH_co.cryptx.RSAPublicKey;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.KeyRecoveryRecord;
import com.CH_co.util.Misc;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * Get My Key Pairs Reply
 * @author  Marcin Kurzawa
 */
public class Key_KeyPairs_Rp extends ProtocolMsgDataSet {
  // <numOfKeys> { <keyId> <ownerUserId> <encPrivateKey> <plainPublicKey> <dateCreated> <dateUpdated> }+
  // <numOfRecovKeys> { <keyRecovId> <keyId> <masterUID> <masterKeyId> }+
  public KeyRecord[] keyRecords;
  public KeyRecoveryRecord[] recoveryRecords;

  /** Creates new Key_KeyPairs_Rp */
  public Key_KeyPairs_Rp() {
  }
  /** Creates new Key_KeyPairs_Rp */
  public Key_KeyPairs_Rp(KeyRecord keyRecord) {
    this.keyRecords = new KeyRecord[] { keyRecord };
  }
  /** Creates new Key_KeyPairs_Rp */
  public Key_KeyPairs_Rp(KeyRecord[] keyRecords) {
    this.keyRecords = keyRecords;
  }
  /** Creates new Key_KeyPairs_Rp */
  public Key_KeyPairs_Rp(KeyRecord[] keyRecords, KeyRecoveryRecord[] recoveryRecords) {
    this.keyRecords = keyRecords;
    this.recoveryRecords = recoveryRecords;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // write indicator
    if (keyRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(keyRecords.length);
      for (int i=0; i<keyRecords.length; i++) {
        dataOut.writeLongObj(keyRecords[i].keyId);
        dataOut.writeLongObj(keyRecords[i].folderId);
        dataOut.writeLongObj(keyRecords[i].ownerUserId);
        dataOut.writeBytes(keyRecords[i].getEncPrivateKey());
        dataOut.writeBytes(keyRecords[i].plainPublicKey.objectToBytes());
        dataOut.writeTimestamp(keyRecords[i].dateCreated);
        dataOut.writeTimestamp(keyRecords[i].dateUpdated);
      }
    }
    if (clientBuild >= 452 && serverBuild >= 452) {
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
        }
      }
    }

  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      keyRecords = new KeyRecord[0];
    else {
      keyRecords = new KeyRecord[dataIn.readShort()];
      for (int i=0; i<keyRecords.length; i++) {
        keyRecords[i] = new KeyRecord();
        keyRecords[i].keyId = dataIn.readLongObj();
        keyRecords[i].folderId= dataIn.readLongObj();
        keyRecords[i].ownerUserId = dataIn.readLongObj();
        keyRecords[i].setEncPrivateKey(dataIn.readSymCipherBlock());
        keyRecords[i].plainPublicKey = RSAPublicKey.bytesToObject(dataIn.readBytes());
        keyRecords[i].dateCreated = dataIn.readTimestamp();
        keyRecords[i].dateUpdated = dataIn.readTimestamp();
      }
    }
    if (clientBuild >= 452 && serverBuild >= 452) {
      indicator = dataIn.read();
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
        }
      }
    }
  } // end initFromStream()


  public String toString() {
    return "[Key_KeyPairs_Rp"
      + ": keyRecords=" + Misc.objToStr(keyRecords)
      + ", recoveryRecords=" + Misc.objToStr(recoveryRecords)
      + "]";
  }
}