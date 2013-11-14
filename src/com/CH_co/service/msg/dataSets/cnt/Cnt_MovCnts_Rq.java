/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets.cnt;

import java.io.IOException;

import com.CH_co.util.Misc;
import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.records.ContactRecord;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 * 
 * Move Contacts
 * @author  Marcin Kurzawa
 */
public class Cnt_MovCnts_Rq extends ProtocolMsgDataSet {

  // <destFolderId> <numOfContacts> { <contactId> <encOwnerNote> }+ 
  public ContactRecord[] contactRecords;
  public FolderRecord folderRecord;
  
  /** Creates new Cnt_MovCnts_Rq */
  public Cnt_MovCnts_Rq() {
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    dataOut.writeLongObj(folderRecord.folderId);
    dataOut.writeShort(contactRecords.length);

    for (int i=0; i<contactRecords.length; i++) {
      dataOut.writeLongObj(contactRecords[i].contactId);
      dataOut.writeBytes(contactRecords[i].getEncOwnerNote());
    }

  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    folderRecord = new FolderRecord();
    folderRecord.folderId = dataIn.readLongObj();
    
    contactRecords = new ContactRecord[dataIn.readShort()];

    for (int i=0; i<contactRecords.length; i++) {
      contactRecords[i] = new ContactRecord();
      contactRecords[i].contactId = dataIn.readLongObj();
      contactRecords[i].setEncOwnerNote(dataIn.readSymCipherBulk());
    }
  } // end initFromStream()

  public String toString() {
    return "[Cnt_MovCnts_Rq"
      + ": folderRecord="   + folderRecord
      + ", contactRecords=" + Misc.objToStr(contactRecords)
      + "]";
  }
}
