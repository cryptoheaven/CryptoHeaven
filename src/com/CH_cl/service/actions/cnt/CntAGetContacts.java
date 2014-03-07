/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.cnt;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.actions.file.FileAGetFiles;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.cnt.Cnt_AcceptDecline_Rq;
import com.CH_co.service.msg.dataSets.cnt.Cnt_GetCnts_Rp;
import com.CH_co.service.msg.dataSets.cnt.Cnt_Rename_Rq;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.ContactRecord;
import com.CH_co.service.records.InvEmlRecord;
import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import java.util.ArrayList;
import java.util.Set;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class CntAGetContacts extends ClientMessageAction {

  /** Creates new CntAGetContacts */
  public CntAGetContacts() {
  }

  /**
  * The action handler performs all actions related to the received message (reply),
  * and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CntAGetContacts.class, "runAction(Connection)");

    Cnt_GetCnts_Rp dataSet = (Cnt_GetCnts_Rp) getMsgDataSet();
    MessageAction request = runAction(getServerInterfaceLayer(), dataSet, this);

    if (trace != null) trace.exit(FileAGetFiles.class, request);
    return request;
  }

  /**
  * Run the action, also used by the folder re-synch code
  * @param SIL
  * @param dataSet
  * @param context
  * @return null
  */
  public static MessageAction runAction(ServerInterfaceLayer SIL, Cnt_GetCnts_Rp dataSet, MessageAction context) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CntAGetContacts.class, "runAction(ServerInterfaceLayer SIL, Cnt_GetCnts_Rp dataSet, MessageAction context)");

    ContactRecord[] contactRecords = dataSet.contactRecords;
    InvEmlRecord[] invEmlRecords = dataSet.invEmlRecords;

    FetchedDataCache cache = SIL.getFetchedDataCache();
    UserRecord myUser = cache.getUserRecord();
    Long userId = cache.getMyUserId();
    Set groupIDsSet = null;

    if (trace != null) trace.data(10, "Running contacts reply with contacts ", contactRecords);

    // Since we need folder shares to which the contacts belong (if its our contact, does not apply for other peoples contacts with us)
    // in order to decrypt the contact notes, lets make sure we have all the shares we need,
    // and if not, fetch them now.  For contacts that are simple notify messages (not specifically requested)
    // and do need any unsealing, do not fetch shares for them.

    // Gather all necessary folders for incoming contacts.
    ArrayList folderIDsL = null;
    for (int i=0; contactRecords!=null && i<contactRecords.length; i++) {
      ContactRecord cRec = contactRecords[i];
      if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
      if (cRec.ownerUserId != null && cRec.ownerUserId.equals(userId) &&
          cRec.folderId != null &&
          cRec.getEncOwnerNote() != null && // if enc note is null, no point fetching a share to decrypt it.
          cache.getFolderShareRecordMy(cRec.folderId, groupIDsSet) == null)
      {
        if (folderIDsL == null) folderIDsL = new ArrayList();
        folderIDsL.add(cRec.folderId);
      }
    }
    if (folderIDsL != null && folderIDsL.size() > 0) {
      Long[] folderIDs = (Long[]) ArrayUtils.toArray(folderIDsL, Long.class);
      folderIDs = (Long[]) ArrayUtils.removeDuplicates(folderIDs);
      SIL.submitAndWait(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_SOME, new Obj_IDList_Co(folderIDs)), 60000);
    }

    // See if we got any contacts for which we don't have user handles fetched
    if (contactRecords != null) {
      ArrayList unknownUserIDsL = null;
      for (int i=0; i<contactRecords.length; i++) {
        ContactRecord cRec = contactRecords[i];
        Long uId = cRec.ownerUserId;
        if (cache.getUserRecord(uId) == null) {
          if (unknownUserIDsL == null) unknownUserIDsL = new ArrayList();
          if (!unknownUserIDsL.contains(uId)) unknownUserIDsL.add(uId);
        }
        uId = cRec.contactWithId;
        if (cache.getUserRecord(uId) == null) {
          if (unknownUserIDsL == null) unknownUserIDsL = new ArrayList();
          if (!unknownUserIDsL.contains(uId)) unknownUserIDsL.add(uId);
        }
        uId = cRec.creatorId;
        if (cache.getUserRecord(uId) == null) {
          if (unknownUserIDsL == null) unknownUserIDsL = new ArrayList();
          if (!unknownUserIDsL.contains(uId)) unknownUserIDsL.add(uId);
        }
      }
      if (unknownUserIDsL != null && unknownUserIDsL.size() > 0) {
        Long[] unknownUserIDs = (Long[]) ArrayUtils.toArray(unknownUserIDsL, Long.class);
        SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(unknownUserIDs)), 30000);
      }
    }

    // If contact is 'gray-arrow' meaning someone else's contact with me then pretend that folderid is of my contact folder
    for (int i=0; contactRecords!=null && i<contactRecords.length; i++)
      if (contactRecords[i].contactWithId.equals(userId))
        contactRecords[i].folderId = myUser.contactFolderId;

    cache.addContactRecords(contactRecords);
    // Check to see if any of the cached InvEmlRecords need to be hidden
    if (contactRecords != null && contactRecords.length > 0) {
      InvEmlRecord[] allInvEmls = cache.getInvEmlRecords();
      if (allInvEmls != null && allInvEmls.length > 0) {
        ArrayList updatedInvEmlsL = new ArrayList();
        for (int i=0; i<contactRecords.length; i++) {
          ContactRecord cRec = contactRecords[i];
          if (cRec.ownerUserId.equals(userId) && cRec.isOfActiveTypeAnyState()) {
            for (int k=0; k<allInvEmls.length; k++) {
              InvEmlRecord invEml = allInvEmls[k];
              if (!invEml.removed.booleanValue() && invEml.sentByUID.equals(userId) && invEml.emailAddr.equalsIgnoreCase(cRec.getOwnerNote())) {
                invEml.removed = Boolean.TRUE;
                updatedInvEmlsL.add(invEml);
              }
            }
          }
        }
        // update matching InvEmlRecords in the cache so that listeners can register the change
        if (updatedInvEmlsL.size() > 0) {
          InvEmlRecord[] updatedInvEmls = (InvEmlRecord[]) ArrayUtils.toArray(updatedInvEmlsL, InvEmlRecord.class);
          cache.addInvEmlRecords(updatedInvEmls);
        }
      }
    }

    // Add InvEmlRecords but HIDE the invitations which are not Removed and have Active contacts with the same name
    if (invEmlRecords != null && invEmlRecords.length > 0) {
      ArrayList filteredInvEmlRecordsL = new ArrayList();
      ContactRecord[] allMyContactRecords = cache.getContactRecordsForUsers(new Long[] { userId });
      for (int i=0; i<invEmlRecords.length; i++) {
        InvEmlRecord invEmlRec = invEmlRecords[i];
        boolean shouldKeep = true;
        if (!invEmlRec.removed.booleanValue() && allMyContactRecords != null) {
          for (int k=0; k<allMyContactRecords.length; k++) {
            ContactRecord cRec = allMyContactRecords[k];
            if (cRec.ownerUserId.equals(userId) && cRec.isOfActiveTypeAnyState() && invEmlRec.sentByUID.equals(userId) && invEmlRec.emailAddr.equalsIgnoreCase(cRec.getOwnerNote())) {
              shouldKeep = false;
              break;
            }
          }
        }
        if (shouldKeep)
          filteredInvEmlRecordsL.add(invEmlRec);
      }
      if (filteredInvEmlRecordsL.size() > 0) {
        InvEmlRecord[] filteredInvEmlRecs = (InvEmlRecord[]) ArrayUtils.toArray(filteredInvEmlRecordsL, InvEmlRecord.class);
        cache.addInvEmlRecords(filteredInvEmlRecs);
      }
    }

    // See if we got any new contacts that need to be recrypted.
    if (contactRecords != null) {
      ArrayList recryptedContactsWithMeL = null;
      ArrayList recryptedContactsMineL = null;
      for (int i=0; i<contactRecords.length; i++) {
        ContactRecord cRec = contactRecords[i];
        // Recrypt assymetrically encrypted contacts
        if (cRec.contactWithId.equals(userId) && cRec.getOtherKeyId() != null && cRec.getEncOtherNote() != null) {
          ContactRecord cRecClone = (ContactRecord) cRec.clone();
          cRecClone.sealRecrypt(myUser.getSymKeyCntNotes());
          if (recryptedContactsWithMeL == null) recryptedContactsWithMeL = new ArrayList();
          recryptedContactsWithMeL.add(cRecClone);
        }
        // Encrypt (with me) contact which was created by someone else for me and given to me without specifying the note.  Use handle as default name.
        else if (cRec.contactWithId.equals(userId)) {
          if (cRec.getNote(userId) == null && cache.getContactRecord(cRec.contactId).getNote(userId) == null || (cRec.getOtherKeyId() == null && cRec.getEncOtherNote() != null && cRec.getEncOtherNote().size() == 0)) {
            ContactRecord cRecClone = (ContactRecord) cRec.clone();
            cRecClone.setOtherNote(cache.getUserRecord(cRecClone.ownerUserId).handle);
            cRecClone.sealRecrypt(myUser.getSymKeyCntNotes());
            if (recryptedContactsWithMeL == null) recryptedContactsWithMeL = new ArrayList();
            recryptedContactsWithMeL.add(cRecClone);
          }
        }
        // Encrypt (my) contact which was created by someone else for me and given to me without specifying the note.  Use handle as default name.
        else if (cRec.ownerUserId.equals(userId)) {
          if (cRec.getNote(userId) == null && cRec.getEncOwnerNote() == null && cache.getContactRecord(cRec.contactId).getNote(userId) == null) {
            ContactRecord cRecClone = (ContactRecord) cRec.clone();
            cRecClone.setOwnerNote(cache.getUserRecord(cRecClone.contactWithId).handle);
            cRecClone.seal(cache.getFolderShareRecordMy(cRecClone.folderId, groupIDsSet).getSymmetricKey());
            if (recryptedContactsMineL == null) recryptedContactsMineL = new ArrayList();
            recryptedContactsMineL.add(cRecClone);
          }
        }
      }
      if (recryptedContactsWithMeL != null && recryptedContactsWithMeL.size() > 0) {
        ContactRecord[] recryptedContacts = (ContactRecord[]) ArrayUtils.toArray(recryptedContactsWithMeL, ContactRecord.class);
        SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_RENAME_CONTACTS_WITH_ME, new Cnt_AcceptDecline_Rq(recryptedContacts)));
      }
      if (recryptedContactsMineL != null && recryptedContactsMineL.size() > 0) {
        for (int i=0; i<recryptedContactsMineL.size(); i++) {
          ContactRecord cRec = (ContactRecord) recryptedContactsMineL.get(i);
          SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_RENAME_MY_CONTACT, new Cnt_Rename_Rq(cRec)));
        }
      }
    }

    ArrayList toAcknowledgeL = null;

    for (int i=0; contactRecords!=null && i<contactRecords.length; i++) {
      final ContactRecord cRec = contactRecords[i];
      if (cRec.status != null) {
        short status = cRec.status.shortValue();
        if (cRec.ownerUserId != null && cRec.ownerUserId.equals(cache.getMyUserId()) &&
          (status == ContactRecord.STATUS_ACCEPTED || status == ContactRecord.STATUS_DECLINED)) {
          if (toAcknowledgeL == null) toAcknowledgeL = new ArrayList();
          toAcknowledgeL.add(cRec.contactId);
        }
      }
    }

    if (toAcknowledgeL != null) {
      Obj_IDList_Co reply = new Obj_IDList_Co();
      reply.IDs = (Long[]) ArrayUtils.toArray(toAcknowledgeL, Long.class);
      MessageAction replyMsg = new MessageAction(CommandCodes.CNT_Q_ACKNOWLEDGE_CONTACTS, reply);
      SIL.submitAndReturn(replyMsg);
    }

    if (trace != null) trace.exit(CntAGetContacts.class, null);
    return null;
  }

}