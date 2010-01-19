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

package com.CH_co.service.msg.dataSets.usr;

import java.io.IOException;

import com.CH_co.util.Misc;
import com.CH_co.monitor.ProgMonitor;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 * 
 * Alter User Password (and recrypt key pairs)
 * @author  Marcin Kurzawa
 * @version 
 */
public class Usr_AltUsrPass_Rq extends ProtocolMsgDataSet {

  // <handle> <passwordHash> <numberOfKeys> {<privKeyId> <encPrivKey> <32-byte-signed-proof> }*
  public UserRecord userRecord;
  public KeyRecord[] keyRecords;
  public byte[][] signed32ByteProofs;
  
  /** Creates new Usr_AltUsrPass_Rq */
  public Usr_AltUsrPass_Rq() {
  }
  
  /** Creates new Usr_AltUsrPass_Rq */
  public Usr_AltUsrPass_Rq(UserRecord userRecord, KeyRecord[] keyRecords, byte[][] signed32ByteProofs) {
    this.userRecord = userRecord;
    this.keyRecords = keyRecords;
    this.signed32ByteProofs = signed32ByteProofs;
  }

  /** Creates new Usr_AltUsrPass_Rq */
  public Usr_AltUsrPass_Rq(UserRecord userRecord, KeyRecord keyRecord, byte[] signed32ByteProof) {
    this.userRecord = userRecord;
    this.keyRecords = new KeyRecord[] { keyRecord };
    this.signed32ByteProofs = new byte[1][];
    this.signed32ByteProofs[0] = signed32ByteProof;
  }
  
  
  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    dataOut.writeString(userRecord.handle);
    dataOut.writeLongObj(userRecord.passwordHash);

    dataOut.writeShort(keyRecords.length);
    for (int i=0; i<keyRecords.length; i++ ) {
      dataOut.writeLongObj(keyRecords[i].keyId);
      dataOut.writeBytes(keyRecords[i].getEncPrivateKey());
      dataOut.writeBytes(signed32ByteProofs[i]);
    }
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    userRecord = new UserRecord();
    
    if (clientBuild >= 18) 
      userRecord.handle = dataIn.readString();
    userRecord.passwordHash = dataIn.readLongObj();
    
    short length = dataIn.readShort();
    keyRecords = new KeyRecord[length];
    signed32ByteProofs = new byte[length][];
    
    for (int i=0; i<length; i++) {
      keyRecords[i] = new KeyRecord();
      keyRecords[i].keyId = dataIn.readLongObj();
      keyRecords[i].setEncPrivateKey(dataIn.readSymCipherBlock());
      signed32ByteProofs[i] = dataIn.readBytes();
    }
  } // end initFromStream()


  public String toString() {
    return "[Usr_AltUsrPass_Rq"
      + ": userRecord=" + userRecord
      + ", keyRecords=" + Misc.objToStr(keyRecords)
      + "]";
  }
}
