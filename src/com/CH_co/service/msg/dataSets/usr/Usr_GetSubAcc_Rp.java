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

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Usr_GetSubAcc_Rp extends ProtocolMsgDataSet {

  // <numberOfUsers> {
  //    <userId> <passwordHash> <handle> <dateCreated> <dateExpired> <dateLastLogin> <dateLastLogout>
  //    <status> <statusInfo> <acceptingSpam> <emailAddress> <notifyByEmail> <dateNotified>
  //    <storageLimit> <storageUsed> <checkStorageDate> <transferLimit> <transferUsed>
  //    <maxSubAccounts> <parentId> <masterId> <flags> <defaultEmlId> <autoResp>
  //    }*
  //    <numOfEmls> { <emlId> <userId> <creatorId> <emailAddr> <dateCreated> <dateUpdated> }*
  //    <numOfResponders { <userId> <dateStart> <dateEnd> <compText> }*

  public UserRecord[] userRecords;
  public EmailRecord[] emailRecords;
  public AutoResponderRecord[] autoResponderRecords;

  /** Creates new Usr_GetSubAcc_Rp */
  public Usr_GetSubAcc_Rp() {
  }

  /** Creates new Usr_GetSubAcc_Rp */
  public Usr_GetSubAcc_Rp(UserRecord userRecord) {
    this.userRecords = new UserRecord[] { userRecord };
  }

  /** Creates new Usr_GetSubAcc_Rp */
  public Usr_GetSubAcc_Rp(UserRecord[] userRecords, EmailRecord[] emailRecords) {
    this.userRecords = userRecords;
    this.emailRecords = emailRecords;
  }

  /** Creates new Usr_GetSubAcc_Rp */
  public Usr_GetSubAcc_Rp(UserRecord[] userRecords, EmailRecord[] emailRecords, AutoResponderRecord[] autoResponderRecords) {
    this.userRecords = userRecords;
    this.emailRecords = emailRecords;
    this.autoResponderRecords = autoResponderRecords;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // write indicator
    if (userRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(userRecords.length);
      for (int i=0; i<userRecords.length; i++ ) {

        dataOut.writeLongObj(userRecords[i].userId);
        dataOut.writeLongObj(userRecords[i].passwordHash);
        dataOut.writeString(userRecords[i].handle);
        dataOut.writeTimestamp(userRecords[i].dateCreated);
        dataOut.writeTimestamp(userRecords[i].dateExpired);
        dataOut.writeTimestamp(userRecords[i].dateLastLogin);
        dataOut.writeTimestamp(userRecords[i].dateLastLogout);
        dataOut.writeSmallint(userRecords[i].status);
        if (serverBuild >= 456 && clientBuild >= 456)
          dataOut.writeString(userRecords[i].statusInfo);
        dataOut.writeSmallint(userRecords[i].acceptingSpam);
        dataOut.writeString(userRecords[i].emailAddress);
        dataOut.writeSmallint(userRecords[i].notifyByEmail);
        dataOut.writeTimestamp(userRecords[i].dateNotified);

        if (serverBuild >= 458 && clientBuild >= 458)
          dataOut.writeLongObj(userRecords[i].currentKeyId);

        dataOut.writeLongObj(userRecords[i].storageLimit);
        dataOut.writeLongObj(userRecords[i].storageUsed);
        dataOut.writeTimestamp(userRecords[i].checkStorageDate);
        dataOut.writeLongObj(userRecords[i].transferLimit);
        dataOut.writeLongObj(userRecords[i].transferUsed);

        dataOut.writeSmallint(userRecords[i].maxSubAccounts);
        dataOut.writeLongObj(userRecords[i].parentId);
        dataOut.writeLongObj(userRecords[i].masterId);
        dataOut.writeLongObj(userRecords[i].flags);
        dataOut.writeLongObj(userRecords[i].defaultEmlId);
        if (clientBuild >= 142 && serverBuild >= 142)
          dataOut.writeCharByte(userRecords[i].autoResp);
        if (clientBuild >= 260 && serverBuild >= 260)
          dataOut.writeCharByte(userRecords[i].online);
      }
    }

    Eml_Get_Rp emailSet = new Eml_Get_Rp(emailRecords);
    emailSet.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);

    // write AutoResponderRecords
    if (clientBuild >= 142 && serverBuild >= 142) {
      Usr_AutoResponder_Co autoResponderSet = new Usr_AutoResponder_Co(autoResponderRecords);
      autoResponderSet.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      userRecords = new UserRecord[0];
    else {
      userRecords = new UserRecord[dataIn.readShort()];

      for (int i=0; i<userRecords.length; i++) {
        userRecords[i]                  = new UserRecord();
        userRecords[i].userId           = dataIn.readLongObj();
        userRecords[i].passwordHash     = dataIn.readLongObj();
        userRecords[i].handle           = dataIn.readString();
        userRecords[i].dateCreated      = dataIn.readTimestamp();
        userRecords[i].dateExpired      = dataIn.readTimestamp();
        userRecords[i].dateLastLogin    = dataIn.readTimestamp();
        userRecords[i].dateLastLogout   = dataIn.readTimestamp();
        userRecords[i].status           = dataIn.readSmallint();
        if (serverBuild >= 456 && clientBuild >= 456)
          userRecords[i].statusInfo     = dataIn.readString();
        userRecords[i].acceptingSpam    = dataIn.readSmallint();
        userRecords[i].emailAddress     = dataIn.readString();
        userRecords[i].notifyByEmail    = dataIn.readSmallint();
        userRecords[i].dateNotified     = dataIn.readTimestamp();

        if (serverBuild >= 458 && clientBuild >= 458)
          userRecords[i].currentKeyId   = dataIn.readLongObj();

        userRecords[i].storageLimit     = dataIn.readLongObj();
        userRecords[i].storageUsed      = dataIn.readLongObj();
        userRecords[i].checkStorageDate = dataIn.readTimestamp();
        userRecords[i].transferLimit    = dataIn.readLongObj();
        userRecords[i].transferUsed     = dataIn.readLongObj();

        userRecords[i].maxSubAccounts   = dataIn.readSmallint();
        userRecords[i].parentId         = dataIn.readLongObj();
        userRecords[i].masterId         = dataIn.readLongObj();
        userRecords[i].flags            = dataIn.readLongObj();
        userRecords[i].defaultEmlId     = dataIn.readLongObj();
        if (clientBuild >= 142 && serverBuild >= 142)
          userRecords[i].autoResp = dataIn.readCharByte();
        if (clientBuild >= 260 && serverBuild >= 260)
          userRecords[i].online = dataIn.readCharByte();
      }
    }

    Eml_Get_Rp emailSet = new Eml_Get_Rp();
    emailSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    emailRecords = emailSet.emailRecords;

    // read AutoResponderRecords
    if (clientBuild >= 142 && serverBuild >= 142) {
      Usr_AutoResponder_Co autoResponderSet = new Usr_AutoResponder_Co();
      autoResponderSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
      autoResponderRecords = autoResponderSet.autoResponderRecords;
    }
  } // end initFromStream()


  public String toString() {
    return "[Usr_GetSubAcc_Rp"
      + ": userRecords="          + Misc.objToStr(userRecords)
      + ", emailRecords="         + Misc.objToStr(emailRecords)
      + ", autoResponderRecords=" + Misc.objToStr(autoResponderRecords)
      + "]";
  }

}