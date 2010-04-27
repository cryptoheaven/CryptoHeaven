/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.service.msg.dataSets.fld;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class Fld_NewFld_Rq extends ProtocolMsgDataSet {

  // <parentFolderId> <folderType> <numToKeep> <keepAsOldAs> <parentShareId>    
  // <ownerType> <ownerUserId> <viewParentId> <encFolderName> <encFolderDesc> <encSymmetricKey> <pubKeyId>
  // <Fld_AddShares_Rq>
  public Long parentFolderId;
  public Short folderType; 
  public Short numToKeep;
  public Integer keepAsOldAs;
  public Long parentShareId;
  public FolderShareRecord folderShareRecord;
  public Fld_AddShares_Rq addSharesRequest;

  /** Creates new Fld_NewFld_Rq */
  public Fld_NewFld_Rq() {
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {

    dataOut.writeLongObj(parentFolderId);
    dataOut.writeSmallint(folderType);
    dataOut.writeSmallint(numToKeep);
    dataOut.writeInteger(keepAsOldAs);
    dataOut.writeLongObj(parentShareId);
    if (clientBuild >= 296 && serverBuild >= 296)
      dataOut.writeSmallint(folderShareRecord.ownerType);
    if (clientBuild >= 148 && serverBuild >= 148)
      dataOut.writeLongObj(folderShareRecord.ownerUserId);
    dataOut.writeLongObj(folderShareRecord.getViewParentId());
    dataOut.writeBytes(folderShareRecord.getEncFolderName());
    dataOut.writeBytes(folderShareRecord.getEncFolderDesc());
    dataOut.writeBytes(folderShareRecord.getEncSymmetricKey());
    dataOut.writeLongObj(folderShareRecord.getPubKeyId());

    // write indicator
    if (addSharesRequest == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      addSharesRequest.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }

  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    parentFolderId = dataIn.readLongObj();
    folderType = dataIn.readSmallint();
    numToKeep = dataIn.readSmallint();
    keepAsOldAs = dataIn.readInteger();
    parentShareId = dataIn.readLongObj();

    folderShareRecord = new FolderShareRecord();
    if (clientBuild >= 296 && serverBuild >= 296)
      folderShareRecord.ownerType = dataIn.readSmallint();
    else
      folderShareRecord.ownerType = new Short(Record.RECORD_TYPE_USER);
    if (clientBuild >= 148 && serverBuild >= 148)
      folderShareRecord.ownerUserId = dataIn.readLongObj();
    if (clientBuild >= 13)
      folderShareRecord.setViewParentId(dataIn.readLongObj());
    folderShareRecord.setEncFolderName(dataIn.readSymCipherBulk());
    folderShareRecord.setEncFolderDesc(dataIn.readSymCipherBulk());
    folderShareRecord.setEncSymmetricKey(dataIn.readAsyCipherBlock());
    folderShareRecord.setPubKeyId(dataIn.readLongObj());

    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      addSharesRequest = null;
    else {
      addSharesRequest = new Fld_AddShares_Rq();
      addSharesRequest.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    }
  } // end initFromStream()

  public String toString() {
    return "["+Misc.getClassNameWithoutPackage(getClass())
      + ": parentFolderId="     + parentFolderId
      + ", folderType="         + folderType
      + ", numToKeep="          + numToKeep
      + ", keepAsOldAs="        + keepAsOldAs
      + ", parentShareId="      + parentShareId
      + ", folderShareRecord="  + folderShareRecord
      + ", addSharesRequest="   + addSharesRequest
      + "]";
  }
}