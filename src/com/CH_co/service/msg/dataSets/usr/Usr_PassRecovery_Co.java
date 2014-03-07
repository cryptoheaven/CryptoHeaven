/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.usr;

import java.io.IOException;

import com.CH_co.cryptx.*;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.1 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Usr_PassRecovery_Co extends ProtocolMsgDataSet {

  // <userId> <lastUpdated> <enabledRecovery> <enabledHint> <hint> <enabledQA> <numQs> <minAs> <questions> <answersHashMD5> <encPassList> <lastFetched> <lastFailed> <lastRecovered>
  public PassRecoveryRecord passRecoveryRecord;
  // server updates require a proof that user has the private key
  public byte[] signed32ByteProof;

  /** Creates new Usr_PassRecovery_Co */
  public Usr_PassRecovery_Co() {
  }

  /** Creates new Usr_PassRecovery_Co */
  public Usr_PassRecovery_Co(PassRecoveryRecord passRecoveryRecord) {
    this.passRecoveryRecord = passRecoveryRecord;
  }

  /** Creates new Usr_PassRecovery_Co */
  public Usr_PassRecovery_Co(PassRecoveryRecord passRecoveryRecord, byte[] signed32ByteProof) {
    this.passRecoveryRecord = passRecoveryRecord;
    this.signed32ByteProof = signed32ByteProof;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_PassRecovery_Co.class, "writeToStream(DataOutputStream2, ProgMonitor, clientBuild, serverBuild)");

    // write PassRecoveryRecord
    if (passRecoveryRecord == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeLongObj(passRecoveryRecord.userId);
      dataOut.writeTimestamp(passRecoveryRecord.lastUpdated);
      dataOut.writeCharByte(passRecoveryRecord.enabledRecovery);
      dataOut.writeString(passRecoveryRecord.hint);
      dataOut.writeCharByte(passRecoveryRecord.enabledQA);
      dataOut.writeSmallint(passRecoveryRecord.numQs);
      dataOut.writeSmallint(passRecoveryRecord.minAs);

      if (passRecoveryRecord.questions == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        dataOut.writeShort(passRecoveryRecord.questions.length);
        for (int i=0; i<passRecoveryRecord.questions.length; i++) {
          dataOut.writeString(passRecoveryRecord.questions[i]);
        }
      }

      if (passRecoveryRecord.answersHashMD5 == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        dataOut.writeShort(passRecoveryRecord.answersHashMD5.length);
        for (int i=0; i<passRecoveryRecord.answersHashMD5.length; i++) {
          dataOut.writeBytes(passRecoveryRecord.answersHashMD5[i]);
        }
      }

      if (passRecoveryRecord.encPassList == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        dataOut.writeShort(passRecoveryRecord.encPassList.length);
        for (int i=0; i<passRecoveryRecord.encPassList.length; i++) {
          dataOut.writeBytes(passRecoveryRecord.encPassList[i]);
        }
      }

      dataOut.writeTimestamp(passRecoveryRecord.lastFetched);
      dataOut.writeTimestamp(passRecoveryRecord.lastFailed);
      dataOut.writeTimestamp(passRecoveryRecord.lastRecovered);
    }

    dataOut.writeBytes(signed32ByteProof);

    if (trace != null) trace.exit(Usr_PassRecovery_Co.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_PassRecovery_Co.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild, serverBuild)");

    // read PassRecoveryRecord
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      passRecoveryRecord = null;
    else {
      passRecoveryRecord = new PassRecoveryRecord();
      passRecoveryRecord.userId = dataIn.readLongObj();
      passRecoveryRecord.lastUpdated = dataIn.readTimestamp();
      passRecoveryRecord.enabledRecovery = dataIn.readCharByte();
      passRecoveryRecord.hint = dataIn.readString();
      passRecoveryRecord.enabledQA = dataIn.readCharByte();
      passRecoveryRecord.numQs = dataIn.readSmallint();
      passRecoveryRecord.minAs = dataIn.readSmallint();

      // read Questions
      // read indicator
      indicator = dataIn.read();
      if (indicator == 0)
        passRecoveryRecord.questions = null;
      else {
        passRecoveryRecord.questions = new String[dataIn.readShort()];
        for (int i=0; i<passRecoveryRecord.questions.length; i++) {
          passRecoveryRecord.questions[i] = dataIn.readString();
        }
      }

      // read Answer Hashes
      indicator = dataIn.read();
      if (indicator == 0)
        passRecoveryRecord.answersHashMD5 = null;
      else {
        passRecoveryRecord.answersHashMD5 = new BADigestBlock[dataIn.readShort()];
        for (int i=0; i<passRecoveryRecord.answersHashMD5.length; i++) {
          passRecoveryRecord.answersHashMD5[i] = dataIn.readDigestBlock();
        }
      }

      // read Encrypted Password List
      indicator = dataIn.read();
      if (indicator == 0)
        passRecoveryRecord.encPassList = null;
      else {
        passRecoveryRecord.encPassList = new BASymCipherBulk[dataIn.readShort()];
        for (int i=0; i<passRecoveryRecord.encPassList.length; i++) {
          passRecoveryRecord.encPassList[i] = dataIn.readSymCipherBulk();
        }
      }

      passRecoveryRecord.lastFetched = dataIn.readTimestamp();
      passRecoveryRecord.lastFailed = dataIn.readTimestamp();
      passRecoveryRecord.lastRecovered = dataIn.readTimestamp();
    }

    signed32ByteProof = dataIn.readBytes();

    if (trace != null) trace.exit(Usr_PassRecovery_Co.class);
  } // end initFromStream()


  public String toString() {
    return "[Usr_PassRecovery_Co"
    + ": passRecoveryRecord=" + passRecoveryRecord
    + ", signed32ByteProof=" + Misc.objToStr(signed32ByteProof)
    + "]";
  }

}