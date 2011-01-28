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

import com.CH_co.cryptx.BAAsyCipherBlock;
import com.CH_co.cryptx.BASymCipherBlock;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.trace.*;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 * 
 * Secure Session Login Reply
 * @author  Marcin Kurzawa
 * @version 
 */
public class Usr_LoginSecSess_Rp extends ProtocolMsgDataSet {

  // <keyId> <encPrivateKey> <encSessionKeys> <serverVersion> <serverReleaseAndBuild> 
  // Note: encSessionKeys is a composite of server-send-key and client-send-key
  public Long keyId;
  public BASymCipherBlock encPrivateKey;
  public BAAsyCipherBlock encSessionKeys;
  public float serverVersion;
  public short serverRelease;
  public short serverBuild;

  /** Creates new Usr_LoginSecSess_Rp */
  public Usr_LoginSecSess_Rp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_LoginSecSess_Rp.class, "Usr_LoginSecSess_Rp()");
    if (trace != null) trace.exit(Usr_LoginSecSess_Rp.class, this);
  }

  /** 
   * Creates new Usr_LoginSecSess_Rp 
   * @param keyId Id of the private key, in case it needs to be retrieved from global properties
   * @param encPrivateKey is the private portion of the user's key encrypted (by the user) with the password
   * @param encSessionKeys are symmetric keys encrypted with the public part of the user's key 
   */
  public Usr_LoginSecSess_Rp(Long keyId, BASymCipherBlock encPrivateKey, BAAsyCipherBlock encSessionKeys, float serverVersion, short serverRelease) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_LoginSecSess_Rp.class, "Usr_LoginSecSess_Rp(Long keyId, BASymCipherBlock encPrivateKey, BAAsyCipherBlock encSessionKeys, float serverVersion, short serverRelease)");
    if (trace != null) trace.args(keyId);
    if (trace != null) trace.args(encPrivateKey);
    if (trace != null) trace.args(encSessionKeys);
    if (trace != null) trace.args(serverVersion);
    if (trace != null) trace.args(serverRelease);
    this.keyId = keyId;
    this.encPrivateKey = encPrivateKey;
    this.encSessionKeys = encSessionKeys;
    this.serverVersion = serverVersion;
    this.serverRelease = serverRelease;
    if (trace != null) trace.exit(Usr_LoginSecSess_Rp.class, this);
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_LoginSecSess_Rp.class, "writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(dataOut, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    if (trace != null) trace.data(10, this);

    dataOut.writeLongObj(keyId);
    dataOut.writeBytes(encPrivateKey);
    dataOut.writeBytes(encSessionKeys);
    dataOut.writeFloat(serverVersion);
    if (clientBuild >= 76) {
      short serverReleaseAndBuild = (short) (serverRelease | (serverBuild << 3));
      dataOut.writeShort(serverReleaseAndBuild);
    } else {
      dataOut.writeShort(serverRelease);
    }

    if (trace != null) trace.exit(Usr_LoginSecSess_Rp.class);
  } // end writeToStream()


  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_LoginSecSess_Rp.class, "initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(dataIn, progressMonitor);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    keyId = dataIn.readLongObj();
    encPrivateKey = dataIn.readSymCipherBlock();
    encSessionKeys = dataIn.readAsyCipherBlock();
    serverVersion = dataIn.readFloat();
    short serverReleaseAndBuild = dataIn.readShort();
    this.serverRelease = (short) (serverReleaseAndBuild & 0x0007); // 3 bits
    this.serverBuild = (short) (serverReleaseAndBuild >> 3);

    if (trace != null) trace.data(10, this);

    if (trace != null) trace.exit(Usr_LoginSecSess_Rp.class);
  } // end initFromStream()


  public String toString() {
    return "[Usr_LoginSecSess_Rp"
      + ": keyId="          + keyId
      + ", encPrivateKey="  + encPrivateKey
      + ", encSessionKeys=" + encSessionKeys
      + ", serverVersion="  + serverVersion
      + ", serverRelease="  + serverRelease
      + ", serverBuild="    + serverBuild
      + "]";
  }

}