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

package com.CH_co.service.msg.dataSets.usr;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/**
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Usr_PassReset_Rq extends ProtocolMsgDataSet {

  // <numUser> { <userId> <handle> <passwordHash> }+ <numKeys> { <keyId> <encPrivateKey> }+
  public UserRecord[] userRecords;
  public KeyRecord[] keyRecords;

  /** Creates new Usr_PassReset_Rq */
  public Usr_PassReset_Rq() {
  }

  /** Creates new Usr_PassReset_Rq */
  public Usr_PassReset_Rq(UserRecord[] userRecs, KeyRecord[] keyRecs) {
    if (userRecs.length != keyRecs.length)
      throw new IllegalArgumentException("User records must match key records.");
    this.userRecords = userRecs;
    this.keyRecords = keyRecs;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_PassReset_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor, clientBuild, serverBuild)");

    // write UserRecords
    if (userRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(userRecords.length);
      for (int i=0; i<userRecords.length; i++) {
        dataOut.writeLongObj(userRecords[i].userId);
        dataOut.writeString(userRecords[i].handle);
        dataOut.writeLongObj(userRecords[i].passwordHash);
      }
    }
    // write KeyRecords
    if (keyRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(keyRecords.length);
      for (int i=0; i<keyRecords.length; i++) {
        dataOut.writeLongObj(keyRecords[i].keyId);
        dataOut.writeBytes(keyRecords[i].getEncPrivateKey());
      }
    }

    if (trace != null) trace.exit(Usr_PassReset_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_PassReset_Rq.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild, serverBuild)");

    // read PassRecoveryRecord
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      userRecords = null;
    else {
      userRecords = new UserRecord[dataIn.readShort()];
      for (int i=0; i<userRecords.length; i++) {
        userRecords[i] = new UserRecord();
        userRecords[i].userId = dataIn.readLongObj();
        userRecords[i].handle = dataIn.readString();
        userRecords[i].passwordHash = dataIn.readLongObj();
      }
    }
    indicator = dataIn.read();
    if (indicator == 0)
      keyRecords = null;
    else {
      keyRecords = new KeyRecord[dataIn.readShort()];
      for (int i=0; i<keyRecords.length; i++) {
        keyRecords[i] = new KeyRecord();
        keyRecords[i].keyId = dataIn.readLongObj();
        keyRecords[i].setEncPrivateKey(dataIn.readSymCipherBlock());
      }
    }

    if (trace != null) trace.exit(Usr_PassReset_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Usr_PassReset_Rq"
    + ": userRecords=" + Misc.objToStr(userRecords)
    + ", keyRecords=" + Misc.objToStr(keyRecords)
    + "]";
  }

}