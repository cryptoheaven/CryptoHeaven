/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.ops;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.cnt.Cnt_NewCnt_Rq;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.*;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.CallbackI;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class ContactOps {

  /**
  * Sends invite for Contact List using an email address, follows by email invites is such user doesn't exist.
  * @param emlAddresses
  * @param emlPersonalMessage
  * @param dialog
  * @param autoCreateWebAccounts
  * @return array of email addresses considered, parsed from 'emlAddresses'
  */
  public static ArrayList doInviteToContacts_Threaded(final ServerInterfaceLayer SIL, String emlAddresses, String emlPersonalMessage, final CallbackI callback, final boolean autoCreateWebAccounts, ArrayList returnEmlAddrsL, ArrayList returnEmlNicksL) {
    final String emailAddresses = emlAddresses != null ? emlAddresses.trim() : null;
    final String emlPersonalMsg = emlPersonalMessage != null ? emlPersonalMessage.trim() : null;
    //final String[] texts = Misc.getEmailInvitationText(personalMsg, null, FetchedDataCache.getSingleInstance().getUserRecord());

    // gather email addresses and nicks for adding to AddressBook
    final ArrayList emailAddressesL = returnEmlAddrsL != null ? returnEmlAddrsL : new ArrayList();
    final ArrayList emailNicksL = returnEmlNicksL != null ? returnEmlNicksL : new ArrayList();
    final ArrayList emailPersonalsL = new ArrayList();
    if (emailAddresses != null) {
      StringTokenizer st = new StringTokenizer(emailAddresses, ",;:");
      while (st.hasMoreTokens()) {
        String token = st.nextToken().trim();
        String[] emls = EmailRecord.gatherAddresses(token);
        if (emls != null && emls.length > 0) {
          for (int i=0; i<emls.length; i++) {
            String eml = emls[i];
            String addrFull;
            // If emails were separeted by other than ,;: then we will have multiple addresses here.  Otherwise use the original token with original personal info.
            if (emls.length == 1)
              addrFull = token;
            else
              addrFull = eml;
            if (EmailRecord.findEmailAddress(emailAddressesL, eml) < 0) {
              emailAddressesL.add(eml);
              emailNicksL.add(EmailRecord.getPersonalOrNick(addrFull));
              emailPersonalsL.add(EmailRecord.getPersonal(addrFull));
            }
          }
        }
      }
    }

    boolean shouldSend = emailAddressesL.size() > 0;
    if (shouldSend) {
      Thread th = new ThreadTraced("Invitation Sender") {
        public void runTraced() {
          // Check if that email address already belongs to an existing user account, create a contact too
          Object[] set = new Object[] { ArrayUtils.toArray(emailAddressesL, Object.class), new Boolean(autoCreateWebAccounts) };
          SIL.submitAndWait(new MessageAction(CommandCodes.EML_Q_LOOKUP_ADDR, new Obj_List_Co(set)), 60000);
          FetchedDataCache cache = SIL.getFetchedDataCache();
          UserRecord myUser = cache.getUserRecord();
          Long myUserId = myUser.userId;

          if (myUserId != null) {
            FolderShareRecord share = cache.getFolderShareRecordMy(myUser.contactFolderId, false);
            if (share != null) {
              Long shareId = share.shareId;
              BASymmetricKey folderSymKey = cache.getFolderShareRecord(shareId).getSymmetricKey();
              String contactReason = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("msg_USER_requests_authorization_for_addition_to_Contact_List."), new Object[] {myUser.handle});
              for (int i=0; i<emailAddressesL.size(); i++) {
                String emlAddr = (String) emailAddressesL.get(i);
                String personal = (String) emailPersonalsL.get(i);
                EmailRecord emlRec = cache.getEmailRecord(emlAddr);
                if (emlRec != null) {
                  Long contactWithId = emlRec.userId;
                  if (!myUserId.equals(contactWithId)) {
                    ContactRecord cRec = cache.getContactRecordOwnerWith(myUserId, contactWithId);
                    if (cRec == null) {
                      // Check if we have user's public key, if not fetch it
                      KeyRecord pubKey = cache.getKeyRecordForUser(contactWithId);
                      if (pubKey == null) {
                        Obj_IDList_Co request = new Obj_IDList_Co();
                        request.IDs = new Long[] { contactWithId };
                        MessageAction msgAction = new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, request);
                        SIL.submitAndWait(msgAction, 60000);
                      }
                      Cnt_NewCnt_Rq request = new Cnt_NewCnt_Rq();
                      request.shareId = shareId;
                      request.contactRecord = new ContactRecord();
                      request.contactRecord.contactWithId = contactWithId;
                      if (personal != null && personal.length() > 0)
                        request.contactRecord.setOwnerNote(personal);
                      else
                        request.contactRecord.setOwnerNote(emlAddr);
                      request.contactRecord.setOtherNote(contactReason);
                      request.contactRecord.setOtherSymKey(new BASymmetricKey(32));
                      request.contactRecord.seal(folderSymKey, cache.getKeyRecordForUser(contactWithId));
                      SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_NEW_CONTACT, request));
                    }
                  }
                }
              }
            }
          }

          // Send invites by Email
          // skip if email already belongs to my active or declined contact
          StringBuffer filteredEmlAddressesSB = new StringBuffer();
          for (int i=0; i<emailAddressesL.size(); i++) {
            String emlAddr = (String) emailAddressesL.get(i);
            EmailRecord emlRec = cache.getEmailRecord(emlAddr);
            if (emlRec != null) {
              Long contactWithId = emlRec.userId;
              if (!myUserId.equals(contactWithId)) {
                ContactRecord cRec = cache.getContactRecordOwnerWith(myUserId, contactWithId);
                if (cRec != null && (cRec.isOfActiveTypeAnyState() || cRec.isOfDeclinedTypeAnyState())) {
                  emlAddr = null;
                }
              }
            }
            if (emlAddr != null) {
              if (filteredEmlAddressesSB.length() > 0)
                filteredEmlAddressesSB.append(",");
              filteredEmlAddressesSB.append(emlAddr);
            }
          }
          boolean inviteSuccess = true;
          if (filteredEmlAddressesSB.length() > 0) {
            Obj_List_Co request = new Obj_List_Co();
            request.objs = new String[] { filteredEmlAddressesSB.toString(), emlPersonalMsg };
            ClientMessageAction msgActionInv = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_SEND_EMAIL_INVITATION, request), 120000);
            DefaultReplyRunner.nonThreadedRun(SIL, msgActionInv);
            inviteSuccess = msgActionInv != null && msgActionInv.getActionCode() >= 0;
          }
          if (callback != null)
            callback.callback(Boolean.valueOf(inviteSuccess));
        }
      };
      th.setDaemon(true);
      th.start();
    }
    if (!shouldSend && callback != null)
      callback.callback(Boolean.FALSE);
    return emailAddressesL;
  }

  public static void doCreateContacts_Threaded(final ServerInterfaceLayer SIL, final String name, final String reason, final Long[] contactWithUserIds, final CallbackI callback) {
    Thread th = new ThreadTraced("Initiate Contact Submitter") {
      public void runTraced() {
        FetchedDataCache cache = SIL.getFetchedDataCache();

        String reasonStr = reason != null && reason.trim().length() > 0 ? reason.trim() : java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("msg_USER_requests_authorization_for_addition_to_Contact_List."), new Object[] {cache.getUserRecord().handle});

        Long shareId = cache.getFolderShareRecordMy(cache.getUserRecord().contactFolderId, false).shareId;
        BASymmetricKey folderSymKey = cache.getFolderShareRecord(shareId).getSymmetricKey();

        // make sure we have all public keys of other users
        ArrayList userIDsWithNoKeysL = new ArrayList();
        for (int i=0; i<contactWithUserIds.length; i++) {
          if (cache.getKeyRecordForUser(contactWithUserIds[i]) == null)
            userIDsWithNoKeysL.add(contactWithUserIds[i]);
        }
        if (userIDsWithNoKeysL.size() > 0) {
          Obj_IDList_Co request = new Obj_IDList_Co(userIDsWithNoKeysL);
          MessageAction msgAction = new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, request);
          SIL.submitAndWait(msgAction, 60000, 3);
        }

        // filter only the userIDs that actually exist by checking if key exists or was returned
        ArrayList userIDsL = new ArrayList();
        for (int i=0; i<contactWithUserIds.length; i++) {
          Long contactWithUserId = contactWithUserIds[i];
          if (cache.getKeyRecordForUser(contactWithUserIds[i]) != null)
            userIDsL.add(contactWithUserId);
        }
        Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsL, Long.class);
        
        // fetch any unknown user handles so we can name our contact
        ArrayList unknownUserIDsL = new ArrayList();
        for (int i=0; i<userIDs.length; i++) {
          Long userId = userIDs[i];
          UserRecord uRec = cache.getUserRecord(userId);
          if (uRec == null)
            unknownUserIDsL.add(userId);
        }
        if (unknownUserIDsL.size() > 0) {
          MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(unknownUserIDsL));
          SIL.submitAndWait(msgAction, 60000, 3);
        }

        // send the Contact creation requests
        for (int i=0; i<userIDs.length; i++) {
          //1310 <shareId>   <contactWithId> <encOwnerNote> <otherKeyId> <encOtherSymKey> <encOtherNote>

          // process each contact separately one at a time
          Long userId = userIDs[i];
          UserRecord uRec = cache.getUserRecord(userId);

          Cnt_NewCnt_Rq request = new Cnt_NewCnt_Rq();
          request.shareId = shareId;
          request.contactRecord = new ContactRecord();
          request.contactRecord.contactWithId = userId;
          request.contactRecord.setOwnerNote(name != null && name.trim().length() > 0 ? name.trim() : (uRec != null ? uRec.handle : userId.toString()));
          request.contactRecord.setOtherNote(reasonStr);
          request.contactRecord.setOtherSymKey(new BASymmetricKey(32));
          request.contactRecord.seal(folderSymKey, cache.getKeyRecordForUser(userId));

          // using the callback we must know when it is finished so use submit with fetch
          if (callback != null) {
            SIL.submitAndWait(new MessageAction(CommandCodes.CNT_Q_NEW_CONTACT, request), 60000);
          } else {
            SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_NEW_CONTACT, request));
          }
        }
        if (callback != null) {
          callback.callback(userIDs);
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }
}