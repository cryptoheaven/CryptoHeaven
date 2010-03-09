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

package com.CH_cl.service.actions.cnt;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.cnt.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
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
    ContactRecord[] contactRecords = dataSet.contactRecords;
    InvEmlRecord[] invEmlRecords = dataSet.invEmlRecords;

    FetchedDataCache cache = getFetchedDataCache();
    UserRecord myUser = cache.getUserRecord();
    Long userId = cache.getMyUserId();
    Set groupIDsSet = null;

    if (trace != null) trace.data(10, "Running contacts reply with contacts ", contactRecords);

    // Since we need folder shares to which the contacts belong (if its our contact, does not apply for other peoples contacts with us)
    // in order to decrypt the contact notes, lets make sure we have all the shares we need,
    // and if not, fetch them now.  For contacts that are simple notify messages (not specifically requested)
    // and do need any unsealing, do not fetch shares for them.

    // Gather all necessary folders for incoming contacts.
    Vector folderIDsV = null;
    for (int i=0; contactRecords!=null && i<contactRecords.length; i++) {
      ContactRecord cRec = contactRecords[i];
      if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
      if (cRec.ownerUserId != null && cRec.ownerUserId.equals(userId) &&
          cRec.folderId != null &&
          cRec.getEncOwnerNote() != null && // if enc note is null, no point fetching a share to decrypt it.
          cache.getFolderShareRecordMy(cRec.folderId, groupIDsSet) == null)
      {
        if (folderIDsV == null) folderIDsV = new Vector();
        folderIDsV.addElement(cRec.folderId);
      }
    }
    if (folderIDsV != null && folderIDsV.size() > 0) {
      Long[] folderIDs = (Long[]) ArrayUtils.toArray(folderIDsV, Long.class);
      folderIDs = (Long[]) ArrayUtils.removeDuplicates(folderIDs);
      getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_SOME, new Obj_IDList_Co(folderIDs)), 120000);
    }

    // See if we got any contacts for which we don't have user handles fetched
    if (contactRecords != null) {
      Vector unknownUserIDsV = null;
      for (int i=0; i<contactRecords.length; i++) {
        ContactRecord cRec = contactRecords[i];
        Long uId = cRec.ownerUserId;
        if (cache.getUserRecord(uId) == null) {
          if (unknownUserIDsV == null) unknownUserIDsV = new Vector();
          if (!unknownUserIDsV.contains(uId)) unknownUserIDsV.addElement(uId);
        }
        uId = cRec.contactWithId;
        if (cache.getUserRecord(uId) == null) {
          if (unknownUserIDsV == null) unknownUserIDsV = new Vector();
          if (!unknownUserIDsV.contains(uId)) unknownUserIDsV.addElement(uId);
        }
        uId = cRec.creatorId;
        if (cache.getUserRecord(uId) == null) {
          if (unknownUserIDsV == null) unknownUserIDsV = new Vector();
          if (!unknownUserIDsV.contains(uId)) unknownUserIDsV.addElement(uId);
        }
      }
      if (unknownUserIDsV != null && unknownUserIDsV.size() > 0) {
        Long[] unknownUserIDs = (Long[]) ArrayUtils.toArray(unknownUserIDsV, Long.class);
        getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(unknownUserIDs)), 30000);
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
        Vector updatedInvEmlsV = new Vector();
        for (int i=0; i<contactRecords.length; i++) {
          ContactRecord cRec = contactRecords[i];
          if (cRec.ownerUserId.equals(userId) && cRec.isOfActiveTypeAnyState()) {
            for (int k=0; k<allInvEmls.length; k++) {
              InvEmlRecord invEml = allInvEmls[k];
              if (!invEml.removed.booleanValue() && invEml.sentByUID.equals(userId) && invEml.emailAddr.equalsIgnoreCase(cRec.getOwnerNote())) {
                invEml.removed = Boolean.TRUE;
                updatedInvEmlsV.addElement(invEml);
              }
            }
          }
        }
        // update matching InvEmlRecords in the cache so that listeners can register the change
        if (updatedInvEmlsV.size() > 0) {
          InvEmlRecord[] updatedInvEmls = (InvEmlRecord[]) ArrayUtils.toArray(updatedInvEmlsV, InvEmlRecord.class);
          cache.addInvEmlRecords(updatedInvEmls);
        }
      }
    }

    // Add InvEmlRecords but HIDE the invitations which are not Removed and have Active contacts with the same name
    if (invEmlRecords != null && invEmlRecords.length > 0) {
      Vector filteredInvEmlRecordsV = new Vector();
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
          filteredInvEmlRecordsV.addElement(invEmlRec);
      }
      if (filteredInvEmlRecordsV.size() > 0) {
        InvEmlRecord[] filteredInvEmlRecs = (InvEmlRecord[]) ArrayUtils.toArray(filteredInvEmlRecordsV, InvEmlRecord.class);
        cache.addInvEmlRecords(filteredInvEmlRecs);
      }
    }

    // See if we got any new contacts that need to be recrypted.
    if (contactRecords != null) {
      Vector recryptedContactsWithMeV = null;
      Vector recryptedContactsMineV = null;
      for (int i=0; i<contactRecords.length; i++) {
        ContactRecord cRec = contactRecords[i];
        // Recrypt assymetrically encrypted contacts
        if (cRec.contactWithId.equals(userId) && cRec.getOtherKeyId() != null && cRec.getEncOtherNote() != null) {
          ContactRecord cRecClone = (ContactRecord) cRec.clone();
          cRecClone.sealRecrypt(myUser.getSymKeyCntNotes());
          if (recryptedContactsWithMeV == null) recryptedContactsWithMeV = new Vector();
          recryptedContactsWithMeV.addElement(cRecClone);
        }
        // Encrypt (with me) contact which was created by someone else for me and given to me without specifying the note.  Use handle as default name.
        else if (cRec.contactWithId.equals(userId)) {
          if (cRec.getNote(userId) == null && cache.getContactRecord(cRec.contactId).getNote(userId) == null || (cRec.getOtherKeyId() == null && cRec.getEncOtherNote() != null && cRec.getEncOtherNote().size() == 0)) {
            ContactRecord cRecClone = (ContactRecord) cRec.clone();
            cRecClone.setOtherNote(cache.getUserRecord(cRecClone.ownerUserId).handle);
            cRecClone.sealRecrypt(myUser.getSymKeyCntNotes());
            if (recryptedContactsWithMeV == null) recryptedContactsWithMeV = new Vector();
            recryptedContactsWithMeV.addElement(cRecClone);
          }
        }
        // Encrypt (my) contact which was created by someone else for me and given to me without specifying the note.  Use handle as default name.
        else if (cRec.ownerUserId.equals(userId)) {
          if (cRec.getNote(userId) == null && cRec.getEncOwnerNote() == null && cache.getContactRecord(cRec.contactId).getNote(userId) == null) {
            ContactRecord cRecClone = (ContactRecord) cRec.clone();
            cRecClone.setOwnerNote(cache.getUserRecord(cRecClone.contactWithId).handle);
            cRecClone.seal(cache.getFolderShareRecordMy(cRecClone.folderId, groupIDsSet).getSymmetricKey());
            if (recryptedContactsMineV == null) recryptedContactsMineV = new Vector();
            recryptedContactsMineV.addElement(cRecClone);
          }
        }
      }
      if (recryptedContactsWithMeV != null && recryptedContactsWithMeV.size() > 0) {
        ContactRecord[] recryptedContacts = (ContactRecord[]) ArrayUtils.toArray(recryptedContactsWithMeV, ContactRecord.class);
        getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.CNT_Q_RENAME_CONTACTS_WITH_ME, new Cnt_AcceptDecline_Rq(recryptedContacts)));
      }
      if (recryptedContactsMineV != null && recryptedContactsMineV.size() > 0) {
        for (int i=0; i<recryptedContactsMineV.size(); i++) {
          ContactRecord cRec = (ContactRecord) recryptedContactsMineV.elementAt(i);
          getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.CNT_Q_RENAME_MY_CONTACT, new Cnt_Rename_Rq(cRec)));
        }
      }
    }

    Vector toAcknowledgeV = null;

    for (int i=0; contactRecords!=null && i<contactRecords.length; i++) {
      final ContactRecord cRec = contactRecords[i];
      if (cRec.status != null) {
        short status = cRec.status.shortValue();
        if (cRec.ownerUserId != null && cRec.ownerUserId.equals(getFetchedDataCache().getMyUserId()) &&
           (status == ContactRecord.STATUS_ACCEPTED || status == ContactRecord.STATUS_DECLINED)) {
          UserRecord uRec = getFetchedDataCache().getUserRecord(cRec.contactWithId);
          String userName = uRec != null ? uRec.shortInfo() : ("(" + cRec.contactWithId + ")");
          String newState = status == ContactRecord.STATUS_ACCEPTED ? "accepted" : "declined";
          String msg = "Contact '" + cRec.getOwnerNote() + "' with user " + userName + " has been " + newState + " by the other party.";
          String title = "Contact " + newState;

          if (status == ContactRecord.STATUS_ACCEPTED) {
            Sounds.playAsynchronous(Sounds.YOU_WERE_AUTHORIZED);
            final JCheckBox jEnableAudibleNotify = new JMyCheckBox("Enable audible notification when contact's status changes to Available.");
            jEnableAudibleNotify.setSelected((cRec.permits.intValue() & ContactRecord.SETTING_DISABLE_AUDIBLE_ONLINE_NOTIFY) == 0);

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            panel.setLayout(new GridBagLayout());

            int posY = 0;
            panel.add(new JMyLabel(msg), new GridBagConstraints(0, posY, 1, 1, 0, 0,
                  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
            posY ++;
            panel.add(jEnableAudibleNotify, new GridBagConstraints(0, posY, 1, 1, 10, 0,
                  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
            posY ++;

            ActionListener defaultButtonAction = new ActionListener() {
              public void actionPerformed(ActionEvent event) {
                Object[] objs = new Object[] { cRec.contactId, Integer.valueOf(jEnableAudibleNotify.isSelected() ? 0 : ContactRecord.SETTING_DISABLE_AUDIBLE_ONLINE_NOTIFY) };
                Obj_List_Co dataSet = new Obj_List_Co();
                dataSet.objs = objs;
                getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.CNT_Q_ALTER_SETTINGS, dataSet));
                Window w = SwingUtilities.windowForComponent((Component)event.getSource());
                w.setVisible(false);
                w.dispose();
              }
            };
            MessageDialog.showDialog(null, panel, title, MessageDialog.INFORMATION_MESSAGE, null, defaultButtonAction, false, false, false);
          } else if (status == ContactRecord.STATUS_DECLINED) {
            Sounds.playAsynchronous(Sounds.DIALOG_ERROR);
            MessageDialog.showInfoDialog(null, msg, title);
          }

          if (toAcknowledgeV == null) toAcknowledgeV = new Vector();
          toAcknowledgeV.addElement(cRec.contactId);
        }
      }
    }

    if (toAcknowledgeV != null) {
      Obj_IDList_Co reply = new Obj_IDList_Co();
      reply.IDs = (Long[]) ArrayUtils.toArray(toAcknowledgeV, Long.class);
      MessageAction replyMsg = new MessageAction(CommandCodes.CNT_Q_ACKNOWLEDGE_CONTACTS, reply);
      getServerInterfaceLayer().submitAndReturn(replyMsg);
    }

    if (trace != null) trace.exit(CntAGetContacts.class, null);
    return null;
  }

}