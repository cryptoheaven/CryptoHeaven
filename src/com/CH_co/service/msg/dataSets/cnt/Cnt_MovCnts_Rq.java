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

package com.CH_co.service.msg.dataSets.cnt;

import java.io.IOException;

import com.CH_co.util.Misc;
import com.CH_co.monitor.ProgMonitor;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.records.ContactRecord;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 * 
 * Move Contacts
 * @author  Marcin Kurzawa
 * @version 
 */
public class Cnt_MovCnts_Rq extends ProtocolMsgDataSet {

  // <destFolderId> <numOfContacts> { <contactId> <encOwnerNote> }+ 
  public ContactRecord[] contactRecords;
  public FolderRecord folderRecord;
  
  /** Creates new Cnt_MovCnts_Rq */
  public Cnt_MovCnts_Rq() {
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    dataOut.writeLongObj(folderRecord.folderId);
    dataOut.writeShort(contactRecords.length);

    for (int i=0; i<contactRecords.length; i++) {
      dataOut.writeLongObj(contactRecords[i].contactId);
      dataOut.writeBytes(contactRecords[i].getEncOwnerNote());
    }

  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
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
