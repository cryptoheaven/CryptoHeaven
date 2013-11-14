/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.usr;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.records.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 * 
 * Alter User Data and Attributes Request
 * @author  Marcin Kurzawa
 */
public class Usr_AltUsrData_Rq extends ProtocolMsgDataSet {

  // <userId> <acceptingSpam> <emailAddress> <notifyByEmail> <currentKeyId> <storageLimit> <bandwidthLimit> <maxSubAccounts> <flags> <autoResp>
  // <userId> <pubKeyId> <encSymKey> <encText>
  // <userId> <dateStart> <dateEnd> <compText>

  public UserRecord userRecord;
  public UserSettingsRecord userSettingsRecord;
  public AutoResponderRecord autoResponderRecord;

  /** Creates new Usr_AltUsrData_Rq */
  public Usr_AltUsrData_Rq() {
  }

  /** Creates new Usr_AltUsrData_Rq */
  public Usr_AltUsrData_Rq(UserRecord userRecord) {
    this.userRecord = userRecord;
  }

  /** Creates new Usr_AltUsrData_Rq */
  public Usr_AltUsrData_Rq(UserSettingsRecord userSettingsRecord) {
    this.userSettingsRecord = userSettingsRecord;
  }

  /** Creates new Usr_AltUsrData_Rq */
  public Usr_AltUsrData_Rq(UserRecord userRecord, UserSettingsRecord userSettingsRecord) {
    this.userRecord = userRecord;
    this.userSettingsRecord = userSettingsRecord;
  }

  /** Creates new Usr_AltUsrData_Rq */
  public Usr_AltUsrData_Rq(UserRecord userRecord, UserSettingsRecord userSettingsRecord, AutoResponderRecord autoResponderRecord) {
    this.userRecord = userRecord;
    this.userSettingsRecord = userSettingsRecord;
    this.autoResponderRecord = autoResponderRecord;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // write UserRecord
    if (userRecord == null) 
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeLongObj(userRecord.userId);
      dataOut.writeSmallint(userRecord.acceptingSpam);
      dataOut.writeString(userRecord.emailAddress);
      dataOut.writeSmallint(userRecord.notifyByEmail);
      dataOut.writeLongObj(userRecord.currentKeyId);
      dataOut.writeLongObj(userRecord.storageLimit);
      dataOut.writeLongObj(userRecord.transferLimit);
      dataOut.writeSmallint(userRecord.maxSubAccounts);
      dataOut.writeLongObj(userRecord.flags);
      if (clientBuild >= 142 && serverBuild >= 142)
        dataOut.writeCharByte(userRecord.autoResp);
    }

    // write UserSettingsRecord
    Usr_Settings_Co userSettingsSet = new Usr_Settings_Co(userSettingsRecord);
    userSettingsSet.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);

    // write AutoResponderRecord
    if (clientBuild >= 142 && serverBuild >= 142) {
      Usr_AutoResponder_Co autoResponderSet = new Usr_AutoResponder_Co(autoResponderRecord);
      autoResponderSet.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // read UserRecord -- prior to b130 it was always available, with b130 and onward, it maybe null
    int indicator = 1; // default - always available
    if (clientBuild >= 130) { // prior to b130 indicator was not written and user data was always available
      indicator = dataIn.read();
    }
    if (indicator == 0)
      userRecord = null;
    else {
      userRecord = new UserRecord();
      if (clientBuild >= 35) 
        userRecord.userId = dataIn.readLongObj();
      userRecord.acceptingSpam = dataIn.readSmallint();
      userRecord.emailAddress = dataIn.readString();
      if (clientBuild >= 10) 
        userRecord.notifyByEmail = dataIn.readSmallint();
      userRecord.currentKeyId = dataIn.readLongObj();
      if (clientBuild >= 35) {
        userRecord.storageLimit = dataIn.readLongObj();
        userRecord.transferLimit = dataIn.readLongObj();
        userRecord.maxSubAccounts = dataIn.readSmallint();
        userRecord.flags = dataIn.readLongObj();
      }
      if (clientBuild >= 142 && serverBuild >= 142)
        userRecord.autoResp = dataIn.readCharByte();
    }

    // read UserSettingsRecord
    if (clientBuild >= 130) {
      Usr_Settings_Co userSettingsSet = new Usr_Settings_Co();
      userSettingsSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
      userSettingsRecord = userSettingsSet.userSettingsRecord;
    }

    // read AutoResponderRecord
    if (clientBuild >= 142 && serverBuild >= 142) {
      Usr_AutoResponder_Co autoResponderSet = new Usr_AutoResponder_Co();
      autoResponderSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
      autoResponderRecord = autoResponderSet.autoResponderRecords != null ? autoResponderSet.autoResponderRecords[0] : null;
    }
  } // end initFromStream()

  public String toString() {
    return "[Usr_AltUsrData_Rq"
      + ": userRecord="           + userRecord
      + ", userSettingsRecord="   + userSettingsRecord
      + ", autoResponderRecord="  + autoResponderRecord
      + "]";
  }

}