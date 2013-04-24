/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.service.msg.dataSets.usr;

import java.io.*;
import java.util.Locale;

import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.*; 
import com.CH_co.service.records.UserRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.trace.*;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p> 
 * 
 * Secure Seccion Login Request
 * @author  Marcin Kurzawa
 * @version 
 */
public class Usr_LoginSecSess_Rq extends ProtocolMsgDataSet {
  // <handle> <passwordHash> <sessionId> <clientVersion> <clientReleaseAndBuild> <sendPrivKey> [<locale-strings>]*
  public UserRecord userRecord;

  // DO NOT set this value manually, it will be set just before a login message
  // is written to the wire by the Writer worker.  The value is taken from 
  // the ClientSessionContext.
  public long sessionId;

  public float clientVersion;
  public short clientRelease;
  public short clientBuild;
  public boolean sendPrivKey;
  public String invitedEmailAddress;
  public String[] clientLocale;


  /** Creates new Usr_LoginSecSess_Rq */
  public Usr_LoginSecSess_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_LoginSecSess_Rq.class, "Usr_LoginSecSess_Rq()");
    if (trace != null) trace.exit(Usr_LoginSecSess_Rq.class, this);
  }

  /** Creates new Usr_LoginSecSess_Rq */
  public Usr_LoginSecSess_Rq(UserRecord userRecord, long sessionId, float clientVersion, short clientRelease, boolean sendPrivKey) {
    this(userRecord, sessionId, clientVersion, clientRelease, sendPrivKey, null);
  }
  public Usr_LoginSecSess_Rq(UserRecord userRecord, long sessionId, float clientVersion, short clientRelease, boolean sendPrivKey, String invitedEmailAddress) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_LoginSecSess_Rq.class, "Usr_LoginSecSess_Rq(UserRecord userRecord, long sessionId, float clientVersion, short clientRelease, boolean sendPrivKey)");
    if (trace != null) trace.args(userRecord);
    if (trace != null) trace.args(sessionId);
    if (trace != null) trace.args(clientVersion);
    if (trace != null) trace.args(clientRelease);
    if (trace != null) trace.args(sendPrivKey);
    if (trace != null) trace.args(invitedEmailAddress);
    this.userRecord = userRecord;
    this.sessionId = sessionId;
    this.clientVersion = clientVersion;
    this.clientRelease = clientRelease;
    this.sendPrivKey = sendPrivKey;
    this.invitedEmailAddress = invitedEmailAddress;
    if (trace != null) trace.exit(Usr_LoginSecSess_Rq.class, this);
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_LoginSecSess_Rq.class, "writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(dataOut, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    if (trace != null) trace.data(10, this);

    dataOut.writeString(userRecord.handle);
    dataOut.writeLongObj(userRecord.passwordHash);
    dataOut.writeLong(sessionId);
    dataOut.writeFloat(clientVersion);
    short clientReleaseAndBuild = (short) (clientRelease | (clientBuild << 3));
    dataOut.writeShort(clientReleaseAndBuild);
    dataOut.writeBoolean(sendPrivKey);
    if (clientBuild >= 18) {
      Locale myLocale = Locale.getDefault();
      dataOut.writeString(myLocale.getLanguage());
      dataOut.writeString(myLocale.getCountry());
      dataOut.writeString(myLocale.getVariant());
    }
    if (clientBuild >= 378) {
      dataOut.writeString(invitedEmailAddress);
    }
    dataOut.write('\r');dataOut.write('\n');

    if (trace != null) trace.exit(Usr_LoginSecSess_Rq.class);
  } // end writeToStream()


  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_LoginSecSess_Rq.class, "initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(dataIn, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    userRecord = new UserRecord();
    userRecord.handle = dataIn.readString();
    userRecord.passwordHash = dataIn.readLongObj();
    sessionId = dataIn.readLong();
    clientVersion = dataIn.readFloat();
    short clientReleaseAndBuild = dataIn.readShort();
    this.clientRelease = (short) (clientReleaseAndBuild & 0x0007); // 3 bits
    this.clientBuild = (short) (clientReleaseAndBuild >> 3);
    sendPrivKey = dataIn.readBoolean();
    if (this.clientBuild >= 18) {
      clientLocale = new String[3];
      clientLocale[0] = dataIn.readString();
      clientLocale[1] = dataIn.readString();
      clientLocale[2] = dataIn.readString();
    }
    if (this.clientBuild >= 378) {
      invitedEmailAddress = dataIn.readString();
    }
    // if \r\n was sent, read it too.
    if (dataIn.available() == 2) {
      dataIn.read();
      dataIn.read();
    }

    if (trace != null) trace.data(10, this);

    if (trace != null) trace.exit(Usr_LoginSecSess_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Usr_LoginSecSess_Rq"
      + ": userRecord="     + userRecord
      + ", sessionId="      + sessionId
      + ", clientVersion="  + clientVersion
      + ", clientRelease="  + clientRelease
      + ", clientBuild="    + clientBuild
      + ", sendPrivKey="    + sendPrivKey
      + ", clientLocale="   + Misc.objToStr(clientLocale)
      + "]";
  }

}