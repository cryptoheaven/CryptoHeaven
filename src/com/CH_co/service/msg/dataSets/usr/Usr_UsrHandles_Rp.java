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

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.Eml_Get_Rp;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p> 
 * 
 * Reply with list of User IDs and handles
 * @author  Marcin Kurzawa
 * @version 
 */
public class Usr_UsrHandles_Rp extends ProtocolMsgDataSet {
  // <numOfHandles> { <userId> <handle> <acceptingSpam> }+ { <emailRecords> }*
  public UserRecord[] userRecords;
  public EmailRecord[] emailRecords;

  /** Creates new Usr_UsrHandles_Rp */
  public Usr_UsrHandles_Rp() {
  }

  /** Creates new Usr_UsrHandles_Rp */
  public Usr_UsrHandles_Rp(UserRecord[] userRecords) {
    this.userRecords = userRecords;
  }

  /** Creates new Usr_UsrHandles_Rp */
  public Usr_UsrHandles_Rp(UserRecord[] userRecords, EmailRecord[] emailRecords) {
    this.userRecords = userRecords;
    this.emailRecords = emailRecords;
  }

  /** Creates new Usr_UsrHandles_Rp */
  public Usr_UsrHandles_Rp(UserRecord userRecord, EmailRecord[] emailRecords) {
    this.userRecords = userRecord != null ? new UserRecord[] { userRecord } : null;
    this.emailRecords = emailRecords;
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
        dataOut.writeString(userRecords[i].handle);
        if (clientBuild >= 272 && serverBuild >= 272) {
          dataOut.writeSmallint(userRecords[i].status);
        }
        dataOut.writeSmallint(userRecords[i].acceptingSpam);
        if (clientBuild >= 182 && serverBuild >= 182) {
          dataOut.writeLongObj(userRecords[i].defaultEmlId);
        }
      }
    }
    if (clientBuild >= 182 && serverBuild >= 182) {
      // write indicator
      if (emailRecords == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        new Eml_Get_Rp(emailRecords).writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
      }
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
        userRecords[i] = new UserRecord();
        userRecords[i].userId = dataIn.readLongObj();
        userRecords[i].handle = dataIn.readString();
        if (clientBuild >= 272 && serverBuild >= 272) {
          userRecords[i].status = dataIn.readSmallint();
        }
        userRecords[i].acceptingSpam = dataIn.readSmallint();
        if (clientBuild >= 182 && serverBuild >= 182) {
          userRecords[i].defaultEmlId = dataIn.readLongObj();
        }
      }
    }
    if (clientBuild >= 182 && serverBuild >= 182) {
      indicator = dataIn.read();
      if (indicator == 0)
        emailRecords = null;
      else {
        Eml_Get_Rp emlSet = new Eml_Get_Rp();
        emlSet.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
        emailRecords = emlSet.emailRecords;
      }
    }
  } // end initFromStream()


  public String toString() {
    return "[Usr_UsrHandles_Rp"
      + ": userRecords=" + Misc.objToStr(userRecords)
      + ", emailRecords=" + Misc.objToStr(emailRecords)
      + "]";
  }

}