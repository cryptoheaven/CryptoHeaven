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

package com.CH_co.service.msg.dataSets.key;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitor;
import com.CH_co.util.*;
import com.CH_co.cryptx.RSAPublicKey;

import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Get Public Keys Reply
 * @author  Marcin Kurzawa
 * @version
 */
public class Key_PubKeys_Rp extends ProtocolMsgDataSet {
  // <numOfKeys> { <keyId> <ownerUserId> <plainPublicKey> }*
  public KeyRecord[] keyRecords;

  /** Creates new Key_PubKeys_Rp */
  public Key_PubKeys_Rp() {
  }

  /** Creates new Key_PubKeys_Rp */
  public Key_PubKeys_Rp(KeyRecord[] keyRecords) {
    this.keyRecords = keyRecords;
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // write null indicator
    if (keyRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(keyRecords.length);

      for (int i=0; i<keyRecords.length; i++) {
        dataOut.writeLongObj(keyRecords[i].keyId);
        dataOut.writeLongObj(keyRecords[i].ownerUserId);
        dataOut.writeBytes(keyRecords[i].plainPublicKey.objectToBytes());
      }
    }

  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // read null indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      keyRecords = new KeyRecord[0];
    else {
      keyRecords = new KeyRecord[dataIn.readShort()];

      for (int i=0; i<keyRecords.length; i++) {
        keyRecords[i] = new KeyRecord();
        keyRecords[i].keyId = dataIn.readLongObj();
        keyRecords[i].ownerUserId = dataIn.readLongObj();
        keyRecords[i].plainPublicKey = RSAPublicKey.bytesToObject(dataIn.readBytes());
      }
    }
  } // end initFromStream()


  public String toString() {
    return "[Key_PubKeys_Rp"
      + ": keyRecords=" + Misc.objToStr(keyRecords)
      + "]";
  }

}