/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
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

import java.io.IOException;

import com.CH_co.cryptx.*;
import com.CH_co.io.*;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p> 
 * 
 * Create New User Request
 * @author  Marcin Kurzawa
 * @version 
 */
public class Usr_NewUsr_Rq extends ProtocolMsgDataSet {
  // <clientBuild> <sponsorId> <requestedEmailAddress> 
  // <handle> <acceptingSpam> <emailAddress> <passwordHash> <encSymKeys>   <maxSubAccounts> <flags> <notifyByEmail> 
  // <encPrivateKey> <plainPublicKey> 
  // <encFileFolderName> <encFileFolderDesc> <encSymmetricKey>
  // <encAddrFolderName> <encAddrFolderDesc> <encSymmetricKey>
  // <encWhiteFolderName> <encWhiteFolderDesc> <encSymmetricKey>
  // <encDraftFolderName> <encDraftFolderDesc> <encSymmetricKey>
  // <encMsgFolderName> <encMsgFolderDesc> <encSymmetricKey>
  // <encJunkFolderName> <encJunkFolderDesc> <encSymmetricKey>
  // <encSentFolderName> <encSentFolderDesc> <encSymmetricKey>
  // <encContactFolderName> <encContactFolderDesc> <encSymmetricKey>
  // <encKeyFolderName> <encKeyFolderDesc> <encSymmetricKey>
  // <encRecycleFolderName> <encRecycleFolderDesc> <encSymmetricKey>
  private Long clientBuildObj; // don't set this manually when writing a request, it will be set when writeToStream() is called...
  public Long sponsorId;
  public String requestedEmailAddress;
  public UserRecord userRecord;
  public KeyRecord keyRecord;
  public FolderShareRecord fileShareRecord;
  public FolderShareRecord addrShareRecord;
  public FolderShareRecord whiteShareRecord;
  public FolderShareRecord draftShareRecord;
  public FolderShareRecord msgShareRecord;
  public FolderShareRecord junkShareRecord;
  public FolderShareRecord sentShareRecord;
  public FolderShareRecord contactShareRecord;
  public FolderShareRecord keyShareRecord;
  public FolderShareRecord recycleShareRecord;

  public Obj_List_Co welcomeEmailSet;

  /** Creates new Usr_NewUsr_Rq */
  public Usr_NewUsr_Rq() {
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    if (clientBuildObj == null)
      clientBuildObj = new Long(clientBuild);
    dataOut.writeLongObj(clientBuildObj);
    dataOut.writeLongObj(sponsorId);
    dataOut.writeString(requestedEmailAddress);

    dataOut.writeString(userRecord.handle);
    dataOut.writeSmallint(userRecord.acceptingSpam);
    dataOut.writeString(userRecord.emailAddress);
    dataOut.writeLongObj(userRecord.passwordHash);
    dataOut.writeBytes(userRecord.getEncSymKeys());
    dataOut.writeSmallint(userRecord.maxSubAccounts);
    dataOut.writeLongObj(userRecord.flags);
    dataOut.writeSmallint(userRecord.notifyByEmail);

    BASymCipherBlock ba = keyRecord.getEncPrivateKey();
    byte[] baBytes = ba != null ? ba.toByteArray() : null;
    dataOut.writeBytes(baBytes);
    dataOut.writeBytes(keyRecord.plainPublicKey.objectToBytes());

    dataOut.writeBytes(fileShareRecord.getEncFolderName());
    dataOut.writeBytes(fileShareRecord.getEncFolderDesc());
    dataOut.writeBytes(fileShareRecord.getEncSymmetricKey());

    dataOut.writeBytes(addrShareRecord.getEncFolderName());
    dataOut.writeBytes(addrShareRecord.getEncFolderDesc());
    dataOut.writeBytes(addrShareRecord.getEncSymmetricKey());

    dataOut.writeBytes(whiteShareRecord.getEncFolderName());
    dataOut.writeBytes(whiteShareRecord.getEncFolderDesc());
    dataOut.writeBytes(whiteShareRecord.getEncSymmetricKey());

    dataOut.writeBytes(draftShareRecord.getEncFolderName());
    dataOut.writeBytes(draftShareRecord.getEncFolderDesc());
    dataOut.writeBytes(draftShareRecord.getEncSymmetricKey());

    dataOut.writeBytes(msgShareRecord.getEncFolderName());
    dataOut.writeBytes(msgShareRecord.getEncFolderDesc());
    dataOut.writeBytes(msgShareRecord.getEncSymmetricKey());

    dataOut.writeBytes(junkShareRecord.getEncFolderName());
    dataOut.writeBytes(junkShareRecord.getEncFolderDesc());
    dataOut.writeBytes(junkShareRecord.getEncSymmetricKey());

    dataOut.writeBytes(sentShareRecord.getEncFolderName());
    dataOut.writeBytes(sentShareRecord.getEncFolderDesc());
    dataOut.writeBytes(sentShareRecord.getEncSymmetricKey());

    dataOut.writeBytes(contactShareRecord.getEncFolderName());
    dataOut.writeBytes(contactShareRecord.getEncFolderDesc());
    dataOut.writeBytes(contactShareRecord.getEncSymmetricKey());

    dataOut.writeBytes(keyShareRecord.getEncFolderName());
    dataOut.writeBytes(keyShareRecord.getEncFolderDesc());
    dataOut.writeBytes(keyShareRecord.getEncSymmetricKey());

    if (clientBuild >= 358) {
      dataOut.writeBytes(recycleShareRecord.getEncFolderName());
      dataOut.writeBytes(recycleShareRecord.getEncFolderDesc());
      dataOut.writeBytes(recycleShareRecord.getEncSymmetricKey());
    }

    if (clientBuild >= 216) {
      if (welcomeEmailSet == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        welcomeEmailSet.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
      }
    }
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_NewUsr_Rq.class, "initFromStream(DataInputStream2)");
    userRecord = new UserRecord();
    keyRecord = new KeyRecord();

    fileShareRecord = new FolderShareRecord();
    msgShareRecord = new FolderShareRecord();
    sentShareRecord = new FolderShareRecord();
    contactShareRecord = new FolderShareRecord();
    keyShareRecord = new FolderShareRecord();

    clientBuildObj = dataIn.readLongObj();

    if (clientBuildObj == null)
      clientBuildObj = new Long(clientBuild);
    else
      clientBuild = clientBuildObj.shortValue();

    if (clientBuild >= 35) {
      sponsorId = dataIn.readLongObj();
      requestedEmailAddress = dataIn.readString();
    }
    userRecord.handle = dataIn.readString();
    userRecord.acceptingSpam = dataIn.readSmallint();
    userRecord.emailAddress = dataIn.readString();
    userRecord.passwordHash = dataIn.readLongObj();
    userRecord.setEncSymKeys(dataIn.readAsyCipherBlock());
    if (clientBuild >= 35) {
      userRecord.maxSubAccounts = dataIn.readSmallint();
      userRecord.flags = dataIn.readLongObj();
      userRecord.notifyByEmail = dataIn.readSmallint();
    }

    if (clientBuild >= 35) {
      byte[] baBytes = dataIn.readBytes();
      BASymCipherBlock ba = baBytes != null ? new BASymCipherBlock(baBytes) : null;
      keyRecord.setEncPrivateKey(ba);
    }
    keyRecord.plainPublicKey = RSAPublicKey.bytesToObject(dataIn.readBytes());

    fileShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
    fileShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
    fileShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());

    if (clientBuild >= 88 && serverBuild >= 88) {
      addrShareRecord = new FolderShareRecord();
      addrShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
      addrShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
      addrShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());
    }

    if (clientBuild >= 318 && serverBuild >= 318) {
      whiteShareRecord = new FolderShareRecord();
      whiteShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
      whiteShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
      whiteShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());
    }

    if (clientBuild >= 88 && serverBuild >= 88) {
      draftShareRecord = new FolderShareRecord();
      draftShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
      draftShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
      draftShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());
    }

    msgShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
    msgShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
    msgShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());

    if (clientBuild >= 316 && serverBuild >= 316) {
      junkShareRecord = new FolderShareRecord();
      junkShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
      junkShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
      junkShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());
    }

    sentShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
    sentShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
    sentShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());

    contactShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
    contactShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
    contactShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());

    keyShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
    keyShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
    keyShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());

    if (clientBuild >= 358 && serverBuild >= 358) {
      recycleShareRecord = new FolderShareRecord();
      recycleShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
      recycleShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
      recycleShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());
    }

    if (clientBuild >= 216 && serverBuild >= 216) {
      int indicator = dataIn.read();
      if (indicator == 0)
        welcomeEmailSet = null;
      else {
        welcomeEmailSet = new Obj_List_Co();
        welcomeEmailSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
      }
    }

    if (trace != null) trace.exit(Usr_NewUsr_Rq.class);
  } // end initFromStream()

  public Long getClientBuildObj() {
    return clientBuildObj;
  }

  public String toString() {
    return "[Usr_NewUsr_Rq"
      + ": clientBuildObj="         + clientBuildObj
      + ", sponsorId="              + sponsorId
      + ", requestedEmailAddress="  + requestedEmailAddress
      + ", userRecord="             + userRecord
      + ", keyRecord="              + keyRecord
      + ", fileShareRecord="        + fileShareRecord
      + ", msgShareRecord="         + msgShareRecord
      + ", junkShareRecord="        + junkShareRecord
      + ", sentShareRecord="        + sentShareRecord
      + ", contactShareRecord="     + contactShareRecord
      + ", keyShareRecord="         + keyShareRecord
      + ", recycleShareRecord="     + recycleShareRecord
      + "]";
  }

}