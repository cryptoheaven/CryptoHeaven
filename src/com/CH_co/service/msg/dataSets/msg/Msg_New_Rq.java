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

package com.CH_co.service.msg.dataSets.msg;

import java.io.IOException;

import com.CH_co.cryptx.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.records.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.obj.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Msg_New_Rq extends ProtocolMsgDataSet {

  /*
     <numOfShares> { <shareId> }*
     <numOfContacts> { <contactId> }*
     <numOfRecipients> { <ownerObjId> <ownerObjType> <encSymmetricKey> <recPubKeyId> <status/recipientType> }+
     <REmsgLinkId> <REmsgId> <REownerObjId> <REownerObjType>
     <objType> <importance> <recipients> <encSubject> <encText> <compressed> <encSignedDigest> <encEncDigest> <sendPrivKeyId> <dateExpired> <bodyPassHint> <bodyPassHash>
     [ { <hash> }+ ]
     [<symmetricKey>]
     [<toEmailAddress>+ <emailSubject> <contentType> <emailBody> <recipientType>+ <importance>]
     <anyAttachments> [
                        <numOfMsgs> { <fromMsgLinkId> }+
                        <numOfShares> { <fromShareId> }+
                        <numOfAttachedMsgs> { <msgLinkId> <encSymmetricKey> }*
                        <numOfAttachedFiles> { <fileLinkId> <encSymmetricKey> }*
                      ]
     <numberOfLocalAttachments>
        {   <ownerObjId> <ownerObjType> <encFileType> <encFileName> <encFileDesc> <encSymmetricKey> <origSize>
            <encOrigDataDigest> <encSignedOrigDigest> <encEncDataDigest> <signingKeyId> <encSize> <encDataFile>
        }*
  */

  // local file attachments are handled by the file upload data set...
  public File_NewFiles_Rq localFiles;

  public Record fromAccount;
  public Long[] shareIds;
  public Long[] contactIds;
  public MsgLinkRecord[] linkRecords;
  public MsgLinkRecord replyToMsgLink;
  public MsgDataRecord dataRecord;
  public Obj_List_Co hashes;
  public BASymmetricKey symmetricKey;

  public Long[] attachmentsFromMsgLinkIDs;
  public Long[] attachmentsFromShareIDs;
  public MsgLinkRecord[] attachedMsgLinkRecords;
  public FileLinkRecord[] attachedFileLinkRecords;

  public Obj_List_Co emailRequest;

  /** Creates new Msg_New_Rq */
  public Msg_New_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_New_Rq.class, "Msg_New_Rq()");
    if (trace != null) trace.exit(Msg_New_Rq.class);
  }

  /** Creates new Msg_New_Rq */
  public Msg_New_Rq(Long shareId, Long contactId, MsgLinkRecord linkRecord, MsgDataRecord dataRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_New_Rq.class, "Msg_New_Rq(Long shareId, Long contactId, MsgLinkRecord linkRecord, MsgDataRecord dataRecord)");
    if (trace != null) trace.args(shareId, contactId, linkRecord, dataRecord);
    this.shareIds = shareId != null ? new Long[] { shareId } : null;
    this.contactIds = contactId != null ? new Long[] { contactId } : null;
    this.linkRecords = new MsgLinkRecord[] { linkRecord };
    this.dataRecord = dataRecord;
    if (trace != null) trace.exit(Msg_New_Rq.class);
  }

  /** Creates new Msg_New_Rq */
  public Msg_New_Rq(Long[] shareIds, Long[] contactIds, MsgLinkRecord[] linkRecords, MsgDataRecord dataRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_New_Rq.class, "Msg_New_Rq(Long[] shareIds, Long[] contactIds, MsgLinkRecord[] linkRecords, MsgDataRecord dataRecord)");
    if (trace != null) trace.args(shareIds, contactIds, linkRecords, dataRecord);
    this.shareIds = shareIds;
    this.contactIds = contactIds;
    this.linkRecords = linkRecords;
    this.dataRecord = dataRecord;
    if (trace != null) trace.exit(Msg_New_Rq.class);
  }

  /** Creates new Msg_New_Rq */
  public Msg_New_Rq(Record fromAccount, Long[] shareIds, Long[] contactIds, MsgLinkRecord[] linkRecords, MsgDataRecord dataRecord, Long[] fromMsgLinkIDs, Long[] fromShareIDs, MsgLinkRecord[] msgAndPostAttachments, FileLinkRecord[] fileAttachments) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_New_Rq.class, "Msg_New_Rq(Long[] shareIds, Long[] contactIds, MsgLinkRecord[] linkRecords, MsgDataRecord dataRecord, Long[] fromMsgLinkIDs, Long[] fromShareIDs, MsgLinkRecord[] msgAndPostAttachments, FileLinkRecord[] fileAttachments");
    if (trace != null) trace.args(fromAccount, shareIds, contactIds, linkRecords, dataRecord, fromMsgLinkIDs, fromShareIDs);
    if (trace != null) trace.args(msgAndPostAttachments, fileAttachments);
    this.fromAccount = fromAccount;
    this.shareIds = shareIds;
    this.contactIds = contactIds;
    this.linkRecords = linkRecords;
    this.dataRecord = dataRecord;
    this.attachmentsFromMsgLinkIDs = fromMsgLinkIDs;
    this.attachmentsFromShareIDs = fromShareIDs;
    this.attachedMsgLinkRecords = msgAndPostAttachments;
    this.attachedFileLinkRecords = fileAttachments;
    if (trace != null) trace.exit(Msg_New_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_New_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    // write fromAccount
    if (clientBuild >= 186 && serverBuild >= 186) {
      if (fromAccount == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        if (fromAccount instanceof UserRecord)
          dataOut.write(1);
        else if (fromAccount instanceof EmailRecord)
          dataOut.write(2);
        else
          dataOut.write(-1);
        dataOut.writeLong(fromAccount.getId().longValue());
      }
    }
    // write shareIds
    if (shareIds == null || shareIds.length == 0)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(shareIds.length);

      for (int i=0; i<shareIds.length; i++)
        dataOut.writeLongObj(shareIds[i]);
    }
    // write contactIds
    if (contactIds == null || contactIds.length == 0)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(contactIds.length);

      for (int i=0; i<contactIds.length; i++)
        dataOut.writeLongObj(contactIds[i]);
    }
    // write new linkRecords data
    if (linkRecords == null || linkRecords.length == 0)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeShort(linkRecords.length);
      for (int i=0; i<linkRecords.length; i++) {
        dataOut.writeLongObj(linkRecords[i].ownerObjId);
        dataOut.writeSmallint(linkRecords[i].ownerObjType);
        dataOut.writeBytes(linkRecords[i].getEncSymmetricKey());
        dataOut.writeLongObj(linkRecords[i].getRecPubKeyId());
        dataOut.writeSmallint(linkRecords[i].status);
      }
    }
    // write replyToMsgLink
    if (replyToMsgLink == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeLongObj(replyToMsgLink.msgLinkId);
      dataOut.writeLongObj(replyToMsgLink.msgId);
      dataOut.writeLongObj(replyToMsgLink.ownerObjId);
      dataOut.writeSmallint(replyToMsgLink.ownerObjType);
    }
    // write new dataRecord data
    if (dataRecord == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      //if (clientBuild >= 86 && serverBuild >= 86)
        dataOut.writeSmallint(dataRecord.objType);
      dataOut.writeSmallint(dataRecord.importance);
      //if (clientBuild >= 82 && serverBuild >= 82)
        dataOut.writeString(dataRecord.getRecipients());
      dataOut.writeBytes(dataRecord.getEncSubject());
      dataOut.writeBytes(dataRecord.getEncText());
      dataOut.writeSmallint(dataRecord.getFlags());
      dataOut.writeBytes(dataRecord.getEncSignedDigest());
      dataOut.writeBytes(dataRecord.getEncEncDigest());
      dataOut.writeLongObj(dataRecord.getSendPrivKeyId());
      if (clientBuild >= 220 && serverBuild >= 220)
        dataOut.writeTimestamp(dataRecord.dateExpired);
      if (clientBuild >= 310 && serverBuild >= 310)
        dataOut.writeString(dataRecord.bodyPassHint);
      if (clientBuild >= 250 && serverBuild >= 250)
        dataOut.writeLongObj(dataRecord.bodyPassHash);
    }

    // write email address hashes if Address object is created
    if (clientBuild >= 102 && serverBuild >= 102) {
      if (hashes == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        hashes.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
      }
    }

    // write symmetric key (used when regular email is also sent)
    dataOut.writeBytes(symmetricKey);

    // write email request
    if (emailRequest == null) 
      dataOut.write(0);
    else {
      dataOut.write(1);
      emailRequest.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }

    // write attachments part
    if ( (attachedMsgLinkRecords == null || attachedMsgLinkRecords.length == 0) &&
         (attachedFileLinkRecords == null || attachedFileLinkRecords.length == 0) ) 
      dataOut.writeBoolean(false);
    else {
      dataOut.writeBoolean(true);

      // write attachmentsFromMsgLinkIDs
      if (attachmentsFromMsgLinkIDs == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        dataOut.writeShort(attachmentsFromMsgLinkIDs.length);
        for (int i=0; i<attachmentsFromMsgLinkIDs.length; i++)
          dataOut.writeLongObj(attachmentsFromMsgLinkIDs[i]);
      }

      // write attachmentsFromShareIDs
      if (attachmentsFromShareIDs == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        dataOut.writeShort(attachmentsFromShareIDs.length);
        for (int i=0; i<attachmentsFromShareIDs.length; i++)
          dataOut.writeLongObj(attachmentsFromShareIDs[i]);
      }

      // write attached message links
      if (attachedMsgLinkRecords == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        dataOut.writeShort(attachedMsgLinkRecords.length);
        for (int i=0; i<attachedMsgLinkRecords.length; i++) {
          dataOut.writeLongObj(attachedMsgLinkRecords[i].msgLinkId);
          dataOut.writeBytes(attachedMsgLinkRecords[i].getEncSymmetricKey());
        }
      }

      // write attached file links
      if (attachedFileLinkRecords == null)
        dataOut.write(0);
      else {
        dataOut.write(1);
        dataOut.writeShort(attachedFileLinkRecords.length);
        for (int i=0; i<attachedFileLinkRecords.length; i++) {
          dataOut.writeLongObj(attachedFileLinkRecords[i].fileLinkId);
          dataOut.writeBytes(attachedFileLinkRecords[i].getEncSymmetricKey());
        }
      }
    } // end write attachments part

    // write local file attachments
    if (localFiles == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      localFiles.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }

    if (trace != null) trace.exit(Msg_New_Rq.class);
  } // end writeToStream()



  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Msg_New_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    // read fromAccount
    if (clientBuild >= 186 && serverBuild >= 186) {
      int indicator = dataIn.read();
      if (indicator == 0)
        fromAccount = null;
      else {
        int type = dataIn.read();
        long id = dataIn.readLong();
        if (type == 1) {
          fromAccount = new UserRecord();
          ((UserRecord) fromAccount).userId = new Long(id);
        } else if (type == 2) {
          fromAccount = new EmailRecord();
          ((EmailRecord) fromAccount).emlId = new Long(id);
        }
      }
    }
    // read shareIds
    int indicator = dataIn.read();
    if (indicator == 0)
      shareIds = null;
    else {
      shareIds = new Long[dataIn.readShort()];
      for (int i=0; i<shareIds.length; i++)
        shareIds[i] = dataIn.readLongObj();
    }
    // read contactIds
    indicator = dataIn.read();
    if (indicator == 0)
      contactIds = null;
    else {
      contactIds = new Long[dataIn.readShort()];
      for (int i=0; i<contactIds.length; i++)
        contactIds[i] = dataIn.readLongObj();
    }
    // read linkRecord data
    indicator = dataIn.read();
    if (indicator == 0)
      linkRecords = null;
    else {
      linkRecords = new MsgLinkRecord[dataIn.readShort()];
      for (int i=0; i<linkRecords.length; i++) {
        linkRecords[i] = new MsgLinkRecord();
        linkRecords[i].ownerObjId = dataIn.readLongObj();
        linkRecords[i].ownerObjType = dataIn.readSmallint();
        linkRecords[i].setEncSymmetricKey(dataIn.readSymCipherBulk());
        linkRecords[i].setRecPubKeyId(dataIn.readLongObj());
        if (clientBuild >= 51)
          linkRecords[i].status = dataIn.readSmallint();
      }
    }

    if (clientBuild >= 12) {
      // read replyToMsgLink
      indicator = dataIn.read();
      if (indicator == 0)
        replyToMsgLink = null;
      else {
        replyToMsgLink = new MsgLinkRecord();
        replyToMsgLink.msgLinkId = dataIn.readLongObj();
        replyToMsgLink.msgId = dataIn.readLongObj();
        replyToMsgLink.ownerObjId = dataIn.readLongObj();
        replyToMsgLink.ownerObjType = dataIn.readSmallint();
      }
    }

    // read dataRecord data
    indicator = dataIn.read();
    if (indicator == 0)
      dataRecord = null;
    else {
      dataRecord = new MsgDataRecord();
      if (clientBuild >= 86)
        dataRecord.objType = dataIn.readSmallint();
      else
        dataRecord.objType = new Short(MsgDataRecord.OBJ_TYPE_MSG);
      dataRecord.importance = dataIn.readSmallint();
      if (clientBuild >= 82)
        dataRecord.setRecipients(dataIn.readString());
      dataRecord.setEncSubject(dataIn.readSymCipherBulk());
      dataRecord.setEncText(dataIn.readSymCipherBulk());
      dataRecord.setFlags(dataIn.readSmallint());
      dataRecord.setEncSignedDigest(dataIn.readSymCipherBulk());
      dataRecord.setEncEncDigest(dataIn.readSymCipherBulk());
      dataRecord.setSendPrivKeyId(dataIn.readLongObj());
      if (clientBuild >= 220 && serverBuild >= 220)
        dataRecord.dateExpired = dataIn.readTimestamp();
      if (clientBuild >= 310 && serverBuild >= 310)
        dataRecord.bodyPassHint = dataIn.readString();
      if (clientBuild >= 250 && serverBuild >= 250)
        dataRecord.bodyPassHash = dataIn.readLongObj();
    }

    // read email address hashes if Address object is created
    if (clientBuild >= 102 && serverBuild >= 102) {
      indicator = dataIn.read();
      if (indicator == 0) 
        hashes = null;
      else {
        hashes = new Obj_List_Co();
        hashes.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
      }
    }

    if (clientBuild >= 76)
      symmetricKey = dataIn.readSymmetricKey();

    if (clientBuild >= 13) {
      // read email request
      indicator = dataIn.read();
      if (indicator == 0) 
        emailRequest = null;
      else {
        emailRequest = new Obj_List_Co();
        emailRequest.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
      }
    }

    // read attachments part
    boolean bIndicator = dataIn.readBoolean();
    if (bIndicator == false) {
      attachmentsFromMsgLinkIDs = null;
      attachmentsFromShareIDs = null;
      attachedMsgLinkRecords = null;
      attachedFileLinkRecords = null;
    } else {

      // read attachmentsFromMsgLinkIDs
      indicator = dataIn.read();
      if (indicator == 0)
        attachmentsFromMsgLinkIDs = null;
      else {
        attachmentsFromMsgLinkIDs = new Long[dataIn.readShort()];
        for (int i=0; i<attachmentsFromMsgLinkIDs.length; i++) 
          attachmentsFromMsgLinkIDs[i] = dataIn.readLongObj();
      }

      // read attachmentsFromShareIDs
      indicator = dataIn.read();
      if (indicator == 0)
        attachmentsFromShareIDs = null;
      else {
        attachmentsFromShareIDs = new Long[dataIn.readShort()];
        for (int i=0; i<attachmentsFromShareIDs.length; i++) 
          attachmentsFromShareIDs[i] = dataIn.readLongObj();
      }

      // read attached message links
      indicator = dataIn.read();
      if (indicator == 0)
        attachedMsgLinkRecords = null;
      else {
        attachedMsgLinkRecords = new MsgLinkRecord[dataIn.readShort()];
        for (int i=0; i<attachedMsgLinkRecords.length; i++) {
          attachedMsgLinkRecords[i] = new MsgLinkRecord();
          attachedMsgLinkRecords[i].msgLinkId = dataIn.readLongObj();
          attachedMsgLinkRecords[i].setEncSymmetricKey(dataIn.readSymCipherBulk());
        }
      }

      // read attached file links
      indicator = dataIn.read();
      if (indicator == 0)
        attachedFileLinkRecords = null;
      else {
        attachedFileLinkRecords = new FileLinkRecord[dataIn.readShort()];
        for (int i=0; i<attachedFileLinkRecords .length; i++) {
          attachedFileLinkRecords[i] = new FileLinkRecord();
          attachedFileLinkRecords[i].fileLinkId = dataIn.readLongObj();
          attachedFileLinkRecords[i].setEncSymmetricKey(dataIn.readSymCipherBulk());
        }
      }
    }

    // read local file attachments
    indicator = dataIn.read();
    if (indicator == 0)
      localFiles = null;
    else {
      localFiles = new File_NewFiles_Rq();
      localFiles.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    }

    if (trace != null) trace.exit(Msg_New_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[Msg_New_Rq"
        + ": fromAccount="    + fromAccount
        + ", shareIds="       + Misc.objToStr(shareIds)
        + ", contactIds="     + Misc.objToStr(contactIds)
        + ", linkRecords="    + Misc.objToStr(linkRecords)
        + ", replyToMsgLink=" + replyToMsgLink
        + ", dataRecord="     + dataRecord
        + ", hashes="         + hashes
        + ", emailRequest="   + emailRequest
        + ", attachmentsFromMsgLinkID=" + Misc.objToStr(attachmentsFromMsgLinkIDs)
        + ", attachmentsFromShareIDs="  + Misc.objToStr(attachmentsFromShareIDs)
        + ", attachedMsgLinkRecords="   + Misc.objToStr(attachedMsgLinkRecords)
        + ", attachedFileLinkRecords="  + Misc.objToStr(attachedFileLinkRecords)
        + ", localFiles="   + Misc.objToStr(localFiles)
        + "]";
  }

}