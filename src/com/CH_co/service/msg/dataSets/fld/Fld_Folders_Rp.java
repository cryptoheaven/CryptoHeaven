/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.fld;

import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.records.Record;
import com.CH_co.util.Misc;
import java.io.IOException;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class Fld_Folders_Rp extends ProtocolMsgDataSet {
  // <numOfFolders> { <folderId> <parentFolderId> <ownerUserId> <folderType> <numToKeep> <keepAsOldAs> <numOfShares> <dateCreated> <dateUpdated> }*
  // <numOfShares> { <shareId> <folderId> <ownerType> <ownerUserId> <encFolderName> <encFolderDesc> <encSymmetricKey> <pubKeyId> <canWrite> <canDelete> <dateCreated> <dateUpdated> <dateUsed> }*

  public FolderRecord[] folderRecords;
  public FolderShareRecord[] shareRecords;

  /** Creates new Fld_Folders_Co */
  public Fld_Folders_Rp() {
  }
  public Fld_Folders_Rp(FolderRecord[] folderRecords, FolderShareRecord[] shareRecords) {
    this.folderRecords = folderRecords;
    this.shareRecords = shareRecords;
  }
  public Fld_Folders_Rp(FolderRecord folderRecord, FolderShareRecord[] shareRecords) {
    if (folderRecord != null)
      this.folderRecords = new FolderRecord[] { folderRecord };
    this.shareRecords = shareRecords;
  }
  public Fld_Folders_Rp(FolderRecord folderRecord, FolderShareRecord shareRecord) {
    if (folderRecord != null)
      this.folderRecords = new FolderRecord[] { folderRecord };
    if (shareRecord != null)
      this.shareRecords = new FolderShareRecord[] { shareRecord };
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // write FolderRecords first
    // write indicator
    if (folderRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(folderRecords.length);

      for (int i=0; i<folderRecords.length; i++) {
        dataOut.writeLongObj(folderRecords[i].folderId);
        dataOut.writeLongObj(folderRecords[i].parentFolderId);
        dataOut.writeLongObj(folderRecords[i].ownerUserId);
        if (clientBuild < 324 && folderRecords[i].folderType.shortValue() == FolderRecord.CHATTING_FOLDER) {
          dataOut.writeSmallint(new Short(FolderRecord.POSTING_FOLDER));
        } else {
          dataOut.writeSmallint(folderRecords[i].folderType);
        }
        dataOut.writeSmallint(folderRecords[i].numToKeep);
        dataOut.writeInteger(folderRecords[i].keepAsOldAs);
        dataOut.writeSmallint(folderRecords[i].numOfShares);
        dataOut.writeTimestamp(folderRecords[i].dateCreated);
        dataOut.writeTimestamp(folderRecords[i].dateUpdated);
        if (clientBuild >= 602 && serverBuild >= 602)
          dataOut.writeInteger(folderRecords[i].numOfViewChildren);
      }
    }
    // write shareRecords second
    // write indicator
    if (shareRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(shareRecords.length);

      for (int i=0; i<shareRecords.length; i++) {
        dataOut.writeLongObj(shareRecords[i].shareId);
        dataOut.writeLongObj(shareRecords[i].folderId);
        if (clientBuild >= 296 && serverBuild >= 296) 
          dataOut.writeSmallint(shareRecords[i].ownerType);
        dataOut.writeLongObj(shareRecords[i].ownerUserId);
        if (clientBuild >= 13) 
          dataOut.writeLongObj(shareRecords[i].getViewParentId());
        dataOut.writeBytes(shareRecords[i].getEncFolderName());
        dataOut.writeBytes(shareRecords[i].getEncFolderDesc());
        dataOut.writeBytes(shareRecords[i].getEncSymmetricKey());
        dataOut.writeLongObj(shareRecords[i].getPubKeyId());
        dataOut.writeSmallint(shareRecords[i].canWrite);
        dataOut.writeSmallint(shareRecords[i].canDelete);
        dataOut.writeTimestamp(shareRecords[i].dateCreated);
        dataOut.writeTimestamp(shareRecords[i].dateUpdated);
        if (clientBuild >= 748 && serverBuild >= 748)
          dataOut.writeTimestamp(shareRecords[i].dateUsed);
      }
    }

  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // read FolderRecords first
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      folderRecords = new FolderRecord[0];
    else {
      folderRecords = new FolderRecord[dataIn.readShort()];

      for (int i=0; i<folderRecords.length; i++) {
        folderRecords[i] = new FolderRecord();
        folderRecords[i].folderId = dataIn.readLongObj();
        folderRecords[i].parentFolderId = dataIn.readLongObj();
        folderRecords[i].ownerUserId = dataIn.readLongObj();
        folderRecords[i].folderType = dataIn.readSmallint();
        folderRecords[i].numToKeep = dataIn.readSmallint();
        folderRecords[i].keepAsOldAs = dataIn.readInteger();
        folderRecords[i].numOfShares = dataIn.readSmallint();
        folderRecords[i].dateCreated = dataIn.readTimestamp();
        folderRecords[i].dateUpdated = dataIn.readTimestamp();
        if (clientBuild >= 602 && serverBuild >= 602)
          folderRecords[i].numOfViewChildren = dataIn.readInteger();
      }
    }

    // read shareRecords second
    // read indicator
    indicator = dataIn.read();
    if (indicator == 0)
      shareRecords = new FolderShareRecord[0];
    else {
      shareRecords = new FolderShareRecord[dataIn.readShort()];

      for (int i=0; i<shareRecords.length; i++) {
        shareRecords[i] = new FolderShareRecord();
        shareRecords[i].shareId = dataIn.readLongObj();
        shareRecords[i].folderId = dataIn.readLongObj();
        if (clientBuild >= 296 && serverBuild >= 296)
          shareRecords[i].ownerType = dataIn.readSmallint();
        else
          shareRecords[i].ownerType = new Short(Record.RECORD_TYPE_USER);
        shareRecords[i].ownerUserId = dataIn.readLongObj();
        shareRecords[i].setViewParentId(dataIn.readLongObj());
        shareRecords[i].setEncFolderName(dataIn.readSymCipherBulk());
        shareRecords[i].setEncFolderDesc(dataIn.readSymCipherBulk());
        shareRecords[i].setEncSymmetricKey(dataIn.readAsyCipherBlock());
        shareRecords[i].setPubKeyId(dataIn.readLongObj());
        shareRecords[i].canWrite = dataIn.readSmallint();
        shareRecords[i].canDelete = dataIn.readSmallint();
        shareRecords[i].dateCreated = dataIn.readTimestamp();
        shareRecords[i].dateUpdated = dataIn.readTimestamp();
        if (clientBuild >= 748 && serverBuild >= 748)
          shareRecords[i].dateUsed = dataIn.readTimestamp();
      }
    }

  } // end initFromStream()


  public String toString() {
    return "[Fld_Folders_Rp"
      + ": folderRecords=" + Misc.objToStr(folderRecords)
      + ", shareRecords=" + Misc.objToStr(shareRecords)
      + "]";
  }
}