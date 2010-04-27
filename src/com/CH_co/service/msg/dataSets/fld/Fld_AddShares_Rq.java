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
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: Add Folder Shares
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Fld_AddShares_Rq extends ProtocolMsgDataSet {

  // <numberOfShares> { <folderId> <ownerType> <ownerUserId> <viewParentId> <encFolderName> <encFolderDesc> <encSymmetricKey> <pubKeyId> <canWrite> <canDelete> }+
  // <numberOfContacts> { <contactId> }+
  // <numberOfGroups> { <groupShareId> }+
  public FolderShareRecord[] shareRecords;
  public Obj_IDList_Co contactIds;
  public Obj_IDList_Co groupShareIds;

  /** Creates new Fld_AddShares_Rq */
  public Fld_AddShares_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AddShares_Rq.class, "Fld_AddShares_Rq()");
    if (trace != null) trace.exit(Fld_AddShares_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AddShares_Rq.class, "writeToStream(DataOutputStream2)");

    // write indicator
    if (shareRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(shareRecords.length);

      for (int i=0; i<shareRecords.length; i++) {
        dataOut.writeLongObj(shareRecords[i].folderId);
        if (clientBuild >= 296 && serverBuild >= 296)
          dataOut.writeSmallint(shareRecords[i].ownerType);
        dataOut.writeLongObj(shareRecords[i].ownerUserId);
        dataOut.writeLongObj(shareRecords[i].getViewParentId());
        dataOut.writeBytes(shareRecords[i].getEncFolderName());
        dataOut.writeBytes(shareRecords[i].getEncFolderDesc());
        dataOut.writeBytes(shareRecords[i].getEncSymmetricKey());
        dataOut.writeLongObj(shareRecords[i].getPubKeyId());
        dataOut.writeSmallint(shareRecords[i].canWrite);
        dataOut.writeSmallint(shareRecords[i].canDelete);
      }
    }

    if (clientBuild >= 148 && serverBuild >= 148) {
      if (contactIds == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        contactIds.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
      }
    } else {
      contactIds.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }

    if (clientBuild >= 296 && serverBuild >= 296) {
      if (groupShareIds == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        groupShareIds.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
      }
    }

    if (trace != null) trace.exit(Fld_AddShares_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_AddShares_Rq.class, "initFromStream(DataInputStream2)");

    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      shareRecords = new FolderShareRecord[0];
    else {
      shareRecords = new FolderShareRecord[dataIn.readShort()];

      for (int i=0; i<shareRecords.length; i++) {
        shareRecords[i] = new FolderShareRecord();
        shareRecords[i].folderId = dataIn.readLongObj();
        if (clientBuild >= 296 && serverBuild >= 296)
          shareRecords[i].ownerType = dataIn.readSmallint();
        else
          shareRecords[i].ownerType = new Short(Record.RECORD_TYPE_USER);
        shareRecords[i].ownerUserId = dataIn.readLongObj();
        if (clientBuild >= 13) 
          shareRecords[i].setViewParentId(dataIn.readLongObj());
        shareRecords[i].setEncFolderName(dataIn.readSymCipherBulk());
        shareRecords[i].setEncFolderDesc(dataIn.readSymCipherBulk());
        shareRecords[i].setEncSymmetricKey(dataIn.readAsyCipherBlock());
        shareRecords[i].setPubKeyId(dataIn.readLongObj());
        shareRecords[i].canWrite = dataIn.readSmallint();
        shareRecords[i].canDelete = dataIn.readSmallint();
      }
    }

    if (clientBuild >= 148 && serverBuild >= 148) {
      // read indicator
      indicator = dataIn.read();
      if (indicator == 0)
        contactIds = null;
      else {
        contactIds = new Obj_IDList_Co();
        contactIds.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
      }
    } else {
      contactIds = new Obj_IDList_Co();
      contactIds.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    }

    if (clientBuild >= 296 && serverBuild >= 296) {
      // read indicator
      indicator = dataIn.read();
      if (indicator == 0)
        groupShareIds = null;
      else {
        groupShareIds = new Obj_IDList_Co();
        groupShareIds.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
      }
    }

    if (trace != null) trace.exit(Fld_AddShares_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Fld_AddShares_Rq"
      + ": shareRecords=" + Misc.objToStr(shareRecords)
      + ", contactIds=" + contactIds
      + ", groupShareIds=" + groupShareIds
      + "]";
  }

}