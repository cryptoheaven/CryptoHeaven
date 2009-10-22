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

package com.CH_co.service.msg.dataSets.obj;

import java.io.*;
import java.sql.Timestamp;

import com.CH_co.cryptx.*;
import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.*;

/**
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Obj_EncSet_Co extends ProtocolMsgDataSet {

  // <protocol> <numOfElements> { <data> }+ <visualized>

  private RSAPublicKey publicKeyToSendWith;
  public ProtocolMsgDataSet dataSet;
  private Obj_List_Co wrapperSet;

  /** Creates new Obj_EncSet_Co */
  public Obj_EncSet_Co() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_EncSet_Co.class, "Obj_EncSet_Co()");
    if (trace != null) trace.exit(Obj_EncSet_Co.class);
  }
  public Obj_EncSet_Co(ProtocolMsgDataSet dataSet) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_EncSet_Co.class, "Obj_EncSet_Co(ProtocolMsgDataSet dataSet)");
    this.dataSet = dataSet;
    if (trace != null) trace.exit(Obj_EncSet_Co.class);
  }
  public Obj_EncSet_Co(RSAPublicKey publicKeyToSendWith, ProtocolMsgDataSet dataSet) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_EncSet_Co.class, "Obj_EncSet_Co(RSAPublicKey publicKeyToSendWith, ProtocolMsgDataSet dataSet)");
    this.publicKeyToSendWith = publicKeyToSendWith;
    this.dataSet = dataSet;
    if (trace != null) trace.exit(Obj_EncSet_Co.class);
  }

  public void setPublicKeyToSendWith(RSAPublicKey publicKeyToSendWith) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_EncSet_Co.class, "setPublicKeyToSendWith(RSAPublicKey publicKeyToSendWith)");
    this.publicKeyToSendWith = publicKeyToSendWith;
    if (trace != null) trace.exit(Obj_EncSet_Co.class);
  }

  private void encrypt(RSAPublicKey publicKey, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_EncSet_Co.class, "encrypt(RSAPublicKey publicKey, short clientBuild, short serverBuild)");
    BASymmetricKey symKey = new BASymmetricKey(32);
    ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
    DataOutputStream2 dataBufferOut = new DataOutputStream2(bufferOut);
    Message.writeToStream(dataBufferOut, null, dataSet, clientBuild, serverBuild);
    dataBufferOut.flush();
    bufferOut.flush();
    byte[] dataBytes = bufferOut.toByteArray();
    dataBufferOut.close();
    bufferOut.close();
    BAAsyCipherBlock encSymKey = null;
    byte[] encDataBytes = null;
    try {
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      encSymKey = asyCipher.blockEncrypt(publicKey, symKey.toByteArray());
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symKey);
      encDataBytes = symCipher.bulkEncrypt(dataBytes, 0, dataBytes.length);
    } catch (Throwable t) {
      if (trace != null) trace.exception(Obj_EncSet_Co.class, 100, t);
      throw new IOException(t.getMessage());
    }
    wrapperSet = new Obj_List_Co(new Object[] { encSymKey.toByteArray(), encDataBytes });
    if (trace != null) trace.exit(Obj_EncSet_Co.class);
  }

  public void decrypt(RSAPrivateKey privateKey, short clientBuild, short serverBuild) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_EncSet_Co.class, "decrypt(RSAPrivateKey privateKey, short clientBuild, short serverBuild)");
    try {
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      BAAsyPlainBlock symKey = asyCipher.blockDecrypt(privateKey, (byte[]) wrapperSet.objs[0]);
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(new BASymmetricKey(symKey));
      byte[] encDataBytes = (byte[]) wrapperSet.objs[1];
      byte[] dataBytes = symCipher.bulkDecrypt(encDataBytes, 0, encDataBytes.length);
      ByteArrayInputStream bufferIn = new ByteArrayInputStream(dataBytes);
      DataInputStream2 dataBufferIn = new DataInputStream2(bufferIn);
      dataSet = Message.readFromStream(dataBufferIn, null, clientBuild, serverBuild);
    } catch (Throwable t) {
      if (trace != null) trace.exception(Obj_EncSet_Co.class, 100, t);
      throw new IllegalArgumentException(t.getMessage());
    }
    if (trace != null) trace.exit(Obj_EncSet_Co.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_EncSet_Co.class, "writeToStream(DataOutputStream2, ProgMonitor, clientBuild, serverBuild)");
    encrypt(publicKeyToSendWith, clientBuild, serverBuild);
    Message.writeToStream(dataOut, progressMonitor, wrapperSet, clientBuild, serverBuild);
    if (trace != null) trace.exit(Obj_EncSet_Co.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_EncSet_Co.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild, serverBuild)");
    try {
      wrapperSet = (Obj_List_Co) Message.readFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    } catch (Throwable t) {
      if (trace != null) trace.exception(Obj_EncSet_Co.class, 100, t);
      throw new IOException(t.getMessage());
    }
    if (trace != null) trace.exit(Obj_EncSet_Co.class);
  } // end initFromStream()

  /**
   * Pass call to the wrapped data set.
   * @return
   */
  public boolean isTimeSensitive() {
    return dataSet.isTimeSensitive();
  }

  /**
   * Pass call to the wrapped data set.
   * @return
   */
  public boolean isUserSensitive() {
    return dataSet.isUserSensitive();
  }

  /**
   * Pass setting to the wrapped data set.
   */
  public void setServerSessionCurrentStamp(Timestamp ts) {
    dataSet.setServerSessionCurrentStamp(ts);
  }
  /**
   * Pass setting to the wrapped data set.
   */
  public void setServerSessionUserId(Long userId) {
    dataSet.setServerSessionUserId(userId);
  }

  public String toString() {
    return "[Obj_EncSet_Co"
    + ": dataSet=" + dataSet
    + ": publicKeyToSendWith=" + publicKeyToSendWith
    + ", wrapperSet=" + wrapperSet
    + "]";
  }

}