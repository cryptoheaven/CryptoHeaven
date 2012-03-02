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
import java.sql.Timestamp;

import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p> 
 * 
 * Get My Info Reply
 * @author  Marcin Kurzawa
 * @version 
 */
public class Usr_GetMyInfo_Rp extends ProtocolMsgDataSet {

  //  <userId> <handle> <dateCreated> <dateExpired> <dateLastLogin> <dateLastLogout> 
  //  <status> <acceptingSpam> <emailAddress> <passwordHash> 
  //  <encSymKeys> <pubKeyId> <currentKeyId>  
  //  <fileFolderId> <addrFolderId> <draftFolderId> <msgFolderId> <junkFolderId> <sentFolderId> <contactFolderId> <keyFolderId> <recycleFolderId>
  //  <storageLimit> <storageUsed> <checkStorageDate> <transferLimit> <transferUsed>
  //  <maxSubAccounts> <parentId> <masterId> <flags> <defaultEmlId>
  //
  //  <userId> <pubKeyId> <encSymKey> <encText>
  public UserRecord userRecord;
  public UserSettingsRecord userSettingsRecord;

  /** Creates new Usr_GetMyInfo_Rp */
  public Usr_GetMyInfo_Rp() {
  }

  /** Creates new Usr_GetMyInfo_Rp */
  public Usr_GetMyInfo_Rp(UserRecord userRecord) {
    this.userRecord = userRecord;
  }

  /** Creates new Usr_GetMyInfo_Rp */
  public Usr_GetMyInfo_Rp(UserSettingsRecord userSettingsRecord) {
    this.userSettingsRecord = userSettingsRecord;
  }

  /** Creates new Usr_GetMyInfo_Rp */
  public Usr_GetMyInfo_Rp(UserRecord userRecord, UserSettingsRecord userSettingsRecord) {
    this.userRecord = userRecord;
    this.userSettingsRecord = userSettingsRecord;
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    if (userRecord == null) {
      dataOut.writeLongObj(new Long(-1));
    } else {
      dataOut.writeLongObj(userRecord.userId);
      dataOut.writeString(userRecord.handle);
      dataOut.writeTimestamp(userRecord.dateCreated);
      dataOut.writeTimestamp(userRecord.dateExpired);
      dataOut.writeTimestamp(userRecord.dateLastLogin);
      dataOut.writeTimestamp(userRecord.dateLastLogout);
      dataOut.writeSmallint(userRecord.status);
      if (clientBuild < 35) {
        // migrate new options to old client
        short oldAccSpam = 0;
        oldAccSpam |= (userRecord.acceptingSpam.shortValue() & UserRecord.ACC_SPAM_YES_INTER) != 0 ? 2 : 0;
        oldAccSpam |= (userRecord.acceptingSpam.shortValue() & UserRecord.ACC_SPAM_YES_REG_EMAIL) != 0 ? 4 : 0;
        dataOut.writeSmallint(new Short(oldAccSpam));
      } else {
        dataOut.writeSmallint(userRecord.acceptingSpam);
      }
      dataOut.writeString(userRecord.emailAddress);
      if (clientBuild >= 10) {
        if (clientBuild >= 35) {
          dataOut.writeSmallint(userRecord.notifyByEmail);
        } else {
          // migrate new options to old client
          short oldNotify = 0;
          oldNotify |= (userRecord.notifyByEmail.shortValue() & UserRecord.EMAIL_NOTIFY_YES) != 0 ? 2 : 1;
          oldNotify |= (userRecord.notifyByEmail.shortValue() & UserRecord.EMAIL_WARN_EXTERNAL) == 0 ? 4 : 0;
          dataOut.writeSmallint(new Short(oldNotify));
        } 
        dataOut.writeTimestamp(userRecord.dateNotified);
      }
      dataOut.writeLongObj(userRecord.passwordHash);
      dataOut.writeBytes(userRecord.getEncSymKeys());
      dataOut.writeLongObj(userRecord.pubKeyId);
      dataOut.writeLongObj(userRecord.currentKeyId);
      dataOut.writeLongObj(null);

      dataOut.writeLongObj(userRecord.fileFolderId);
      if (clientBuild >= 86)
        dataOut.writeLongObj(userRecord.addrFolderId);
      if (clientBuild >= 82)
        dataOut.writeLongObj(userRecord.draftFolderId);
      dataOut.writeLongObj(userRecord.msgFolderId);
      if (clientBuild >= 316)
        dataOut.writeLongObj(userRecord.junkFolderId);
      dataOut.writeLongObj(userRecord.sentFolderId);
      dataOut.writeLongObj(userRecord.contactFolderId);
      dataOut.writeLongObj(userRecord.keyFolderId);
      if (clientBuild >= 358 && serverBuild >= 358)
        dataOut.writeLongObj(userRecord.recycleFolderId);

      dataOut.writeLongObj(userRecord.storageLimit);
      dataOut.writeLongObj(userRecord.storageUsed);
      dataOut.writeTimestamp(userRecord.checkStorageDate);
      dataOut.writeLongObj(userRecord.transferLimit);
      dataOut.writeLongObj(userRecord.transferUsed);

      if (clientBuild >= 35) {
        dataOut.writeSmallint(userRecord.maxSubAccounts);
        dataOut.writeLongObj(userRecord.parentId);
        dataOut.writeLongObj(userRecord.masterId);
        dataOut.writeLongObj(userRecord.flags);
        dataOut.writeLongObj(userRecord.defaultEmlId);
      }

      if (clientBuild >= 260 && serverBuild >= 260)
        dataOut.writeCharByte(userRecord.online);
    }

    // write UserSettingsRecord
    if (clientBuild >= 130) {
      Usr_Settings_Co userSettingsSet = new Usr_Settings_Co(userSettingsRecord);
      userSettingsSet.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Long userId = dataIn.readLongObj();
    if (userId.longValue() != -1) {
      userRecord                  = new UserRecord();
      userRecord.userId           = userId;
      userRecord.handle           = dataIn.readString();
      userRecord.dateCreated      = dataIn.readTimestamp();
      userRecord.dateExpired      = dataIn.readTimestamp();
      userRecord.dateLastLogin    = dataIn.readTimestamp();
      userRecord.dateLastLogout   = dataIn.readTimestamp();
      userRecord.status           = dataIn.readSmallint();
      userRecord.acceptingSpam    = dataIn.readSmallint();
      userRecord.emailAddress     = dataIn.readString();
      userRecord.notifyByEmail    = dataIn.readSmallint();
      userRecord.dateNotified     = dataIn.readTimestamp();
      userRecord.passwordHash     = dataIn.readLongObj();
      userRecord.setEncSymKeys(dataIn.readAsyCipherBlock());
      userRecord.pubKeyId         = dataIn.readLongObj();
      userRecord.currentKeyId     = dataIn.readLongObj();
      // paymentVehicleId
      dataIn.readLongObj();

      userRecord.fileFolderId     = dataIn.readLongObj();
      userRecord.addrFolderId     = dataIn.readLongObj();
      userRecord.draftFolderId    = dataIn.readLongObj();
      userRecord.msgFolderId      = dataIn.readLongObj();
      userRecord.junkFolderId     = dataIn.readLongObj();
      userRecord.sentFolderId     = dataIn.readLongObj();
      userRecord.contactFolderId  = dataIn.readLongObj();
      userRecord.keyFolderId      = dataIn.readLongObj();
      if (clientBuild >= 358 && serverBuild >= 358)
        userRecord.recycleFolderId = dataIn.readLongObj();

      userRecord.storageLimit     = dataIn.readLongObj();
      userRecord.storageUsed      = dataIn.readLongObj();
      userRecord.checkStorageDate = dataIn.readTimestamp();
      userRecord.transferLimit    = dataIn.readLongObj();
      userRecord.transferUsed     = dataIn.readLongObj();

      userRecord.maxSubAccounts   = dataIn.readSmallint();
      userRecord.parentId         = dataIn.readLongObj();
      userRecord.masterId         = dataIn.readLongObj();
      userRecord.flags            = dataIn.readLongObj();
      userRecord.defaultEmlId     = dataIn.readLongObj();

      if (clientBuild >= 260 && serverBuild >= 260)
        userRecord.online = dataIn.readCharByte();
    }

    // read UserSettingsRecord
    Usr_Settings_Co userSettingsSet = new Usr_Settings_Co();
    userSettingsSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    userSettingsRecord = userSettingsSet.userSettingsRecord;

  } // end initFromStream()

  public String toString() {
    return "[Usr_GetMyInfo_Rp"
      + ": userRecord=" + userRecord
      + ", userSettingsRecord=" + userSettingsRecord
      + "]";
  }

}