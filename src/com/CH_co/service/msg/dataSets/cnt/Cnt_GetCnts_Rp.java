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

package com.CH_co.service.msg.dataSets.cnt;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitor;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.*;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Get My Contacts
 * @author  Marcin Kurzawa
 * @version
 */
public class Cnt_GetCnts_Rp extends ProtocolMsgDataSet {

  // <numOfContacts> { <contactId> <folderId> <ownerUserId> <contactWithId> <creatorId> <status> <permits> <encOwnerNote> <otherKeyId> <encOtherSymKey> <encOtherNote> <dateCreated> <dateUpdated> }*
  // <numOfInvEmls> { <id> <emailAddr> <sentByUID> <fromName> <fromEmail> <msg> <dateSent> }*
  public ContactRecord[] contactRecords;
  public InvEmlRecord[] invEmlRecords;

  /** Creates new Cnt_GetCnts_Rp */
  public Cnt_GetCnts_Rp() {
  }

  /** Creates new Cnt_GetCnts_Rp */
  public Cnt_GetCnts_Rp(ContactRecord[] contactRecords) {
    this.contactRecords = contactRecords;
  }

  /** Creates new Cnt_GetCnts_Rp */
  public Cnt_GetCnts_Rp(ContactRecord contactRecord) {
    this.contactRecords = new ContactRecord[] { contactRecord };
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // write indicator
    if (contactRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(contactRecords.length);

      for (int i=0; i<contactRecords.length; i++) {
        dataOut.writeLongObj(contactRecords[i].contactId);
        dataOut.writeLongObj(contactRecords[i].folderId);
        dataOut.writeLongObj(contactRecords[i].ownerUserId);
        dataOut.writeLongObj(contactRecords[i].contactWithId);
        if (clientBuild >= 35)
          dataOut.writeLongObj(contactRecords[i].creatorId);
        if (clientBuild < 260 || serverBuild < 260) // for old clients and engines, translate the status
          dataOut.writeSmallint(ContactRecord.isOnlineStatus(contactRecords[i].status) ? Short.valueOf(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE) : contactRecords[i].status);
        else
          dataOut.writeSmallint(contactRecords[i].status);
        if (clientBuild >= 28)
          dataOut.writeInteger(contactRecords[i].permits);
        dataOut.writeBytes(contactRecords[i].getEncOwnerNote());
        dataOut.writeLongObj(contactRecords[i].getOtherKeyId());
        dataOut.writeBytes(contactRecords[i].getEncOtherSymKey());
        dataOut.writeBytes(contactRecords[i].getEncOtherNote());
        dataOut.writeTimestamp(contactRecords[i].dateCreated);
        dataOut.writeTimestamp(contactRecords[i].dateUpdated);
      }
    }

    if (clientBuild >= 500 && serverBuild >= 500) {
      if (invEmlRecords == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        dataOut.writeShort(invEmlRecords.length);
        for (int i=0; i<invEmlRecords.length; i++) {
          dataOut.writeLongObj(invEmlRecords[i].id);
          dataOut.writeString(invEmlRecords[i].emailAddr);
          dataOut.writeLongObj(invEmlRecords[i].sentByUID);
          dataOut.writeString(invEmlRecords[i].fromName);
          dataOut.writeString(invEmlRecords[i].fromEmail);
          dataOut.writeString(invEmlRecords[i].msg);
          dataOut.writeTimestamp(invEmlRecords[i].dateSent);
          dataOut.writeBooleanObj(invEmlRecords[i].removed);
        }
      }
    }
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      contactRecords = new ContactRecord[0];
    else {

      contactRecords = new ContactRecord[dataIn.readShort()];

      for (int i=0; i<contactRecords.length; i++) {
        contactRecords[i] = new ContactRecord();
        contactRecords[i].contactId = dataIn.readLongObj();
        contactRecords[i].folderId = dataIn.readLongObj();
        contactRecords[i].ownerUserId = dataIn.readLongObj();
        contactRecords[i].contactWithId = dataIn.readLongObj();
        contactRecords[i].creatorId = dataIn.readLongObj();
        contactRecords[i].status = dataIn.readSmallint();
        if (clientBuild < 260 || serverBuild < 260) // for old clients and engines, translate the status
          if (ContactRecord.isOnlineStatus(contactRecords[i].status))
            contactRecords[i].status = Short.valueOf(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE);
        contactRecords[i].permits = dataIn.readInteger();
        contactRecords[i].setEncOwnerNote(dataIn.readSymCipherBulk());
        contactRecords[i].setOtherKeyId(dataIn.readLongObj());
        contactRecords[i].setEncOtherSymKey(dataIn.readAsyCipherBlock());
        contactRecords[i].setEncOtherNote(dataIn.readSymCipherBulk());
        contactRecords[i].dateCreated = dataIn.readTimestamp();
        contactRecords[i].dateUpdated = dataIn.readTimestamp();
      }
    }

    if (clientBuild >= 500 && serverBuild >= 500) {
      indicator = dataIn.read();
      if (indicator == 0)
        invEmlRecords = null;
      else {
        invEmlRecords = new InvEmlRecord[dataIn.readShort()];
        for (int i=0; i<invEmlRecords.length; i++) {
          invEmlRecords[i] = new InvEmlRecord();
          invEmlRecords[i].id = dataIn.readLongObj();
          invEmlRecords[i].emailAddr = dataIn.readString();
          invEmlRecords[i].sentByUID = dataIn.readLongObj();
          invEmlRecords[i].fromName = dataIn.readString();
          invEmlRecords[i].fromEmail = dataIn.readString();
          invEmlRecords[i].msg = dataIn.readString();
          invEmlRecords[i].dateSent = dataIn.readTimestamp();
          invEmlRecords[i].removed = dataIn.readBooleanObj();
        }
      }
    }
  } // end initFromStream()

  public String toString() {
    return "[Cnt_GetCnts_Rp"
      + ": contactRecords=" + Misc.objToStr(contactRecords)
      + ": invEmlRecords=" + Misc.objToStr(invEmlRecords)
      + "]";
  }
}