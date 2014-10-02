/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.cache;

import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.URLs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.1 $</b>
*
* @author  Marcin Kurzawa
*/
public class CacheUsrUtils {

  public static Record[][] checkValidityOfRecipients(final FetchedDataCache cache, Record[][] selectedRecipients, StringBuffer errorSB) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUsrUtils.class, "checkValidityOfRecipients(Record[][] selectedRecipients, StringBuffer errorSB)");
    if (trace != null) trace.args(selectedRecipients);
    // check for contacts to have messaging enabled
    ArrayList badContactsL = null;
    // check for address contacts to have a valid email address
    ArrayList badAddressesL = null;
    // check for invalid or inaccessible folders
    ArrayList badFoldersL = null;
    // put objects that pass in here
    ArrayList[] filteredSelectedRecipientsL = new ArrayList[selectedRecipients.length];
    for (int recipientType=MsgLinkRecord.RECIPIENT_TYPE_TO; selectedRecipients.length>recipientType && recipientType<=MsgLinkRecord.RECIPIENT_TYPE_BCC; recipientType++) {
      filteredSelectedRecipientsL[recipientType] = new ArrayList();
      for (int i=0; selectedRecipients[recipientType] != null && i<selectedRecipients[recipientType].length; i++) {
        Record rec = selectedRecipients[recipientType][i];
        boolean invalid = false;
        if (rec instanceof ContactRecord) {
          ContactRecord cRec = (ContactRecord) rec;
          if ((cRec.permits.intValue() & ContactRecord.PERMIT_DISABLE_MESSAGING) != 0) {
            if (badContactsL == null) badContactsL = new ArrayList();
            if (!badContactsL.contains(rec))
              badContactsL.add(rec);
            invalid = true;
          }
        } else if (rec instanceof MsgDataRecord) {
          MsgDataRecord mData = (MsgDataRecord) rec;
          if (mData.isTypeAddress() && (mData.email == null || mData.email.length() == 0)) {
            if (badAddressesL == null) badAddressesL = new ArrayList();
            if (!badAddressesL.contains(rec))
              badAddressesL.add(rec);
            invalid = true;
          }
        } else if (rec instanceof FolderRecord) {
          FolderRecord fldRec = (FolderRecord) rec;
          FolderRecord fRec = cache.getFolderRecord(fldRec.folderId);
          if (fRec == null || cache.getFolderShareRecordMy(fRec.folderId, true) == null) {
            if (badFoldersL == null) badFoldersL = new ArrayList();
            if (!badFoldersL.contains(rec))
              badFoldersL.add(rec);
            invalid = true;
          }
        }
        if (!invalid) {
          filteredSelectedRecipientsL[recipientType].add(rec);
        }
      }
    }
    appendInvalidRecipientErrMsg(cache, errorSB, badContactsL, com.CH_cl.lang.Lang.rb.getString("msg_The_following_selected_contact(s)_have_messaging_permission_disabled..."));
    appendInvalidRecipientErrMsg(cache, errorSB, badAddressesL, com.CH_cl.lang.Lang.rb.getString("msg_The_following_address_contacts_do_not_have_a_default_email_address_present..."));
    appendInvalidRecipientErrMsg(cache, errorSB, badFoldersL, com.CH_cl.lang.Lang.rb.getString("msg_The_following_folders_cannot_be_found_or_are_not_accessible..."));
    Record[][] filteredSelectedRecipients = new Record[filteredSelectedRecipientsL.length][];
    for (int recipientType=0; recipientType<filteredSelectedRecipients.length; recipientType++) {
      Record[] recipients = new Record[filteredSelectedRecipientsL[recipientType].size()];
      filteredSelectedRecipientsL[recipientType].toArray(recipients);
      filteredSelectedRecipients[recipientType] = recipients;
    }
    if (trace != null) trace.exit(CacheUsrUtils.class, filteredSelectedRecipients);
    return filteredSelectedRecipients;
  }
  private static void appendInvalidRecipientErrMsg(final FetchedDataCache cache,StringBuffer errSB, ArrayList recsL, String msgPrefix) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUsrUtils.class, "appendInvalidRecipientErrMsg(StringBuffer errSB, ArrayList recsL, String msgPrefix)");
    if (trace != null) trace.args(errSB, recsL, msgPrefix);
    if (recsL != null) {
      StringBuffer sb = new StringBuffer();
      for (int i=0; i<recsL.size(); i++) {
        sb.append(TextRenderer.getRenderedText(cache, recsL.get(i)));
        if (i<recsL.size()-1)
          sb.append(", ");
      }
      if (errSB.length() > 0) errSB.append("\n\n");
      errSB.append(msgPrefix);
      errSB.append("\n\n");
      errSB.append(sb.toString());
    }
    if (trace != null) trace.exit(CacheUsrUtils.class);
  }

  /**
  * @return an acknowledged contact record or user record.
  */
  public static Record convertUserIdToFamiliarUser(final FetchedDataCache cache, Long userId, boolean recipientOk, boolean senderOk) {
    return convertUserIdToFamiliarUser(cache, userId, recipientOk, senderOk, true);
  }
  public static Record convertUserIdToFamiliarUser(final FetchedDataCache cache, Long userId, boolean recipientOk, boolean senderOk, boolean includeWebUsers) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUsrUtils.class, "convertUserIdToFamiliarUser(Long userId, boolean recipientOk, boolean senderOk, boolean includeWebUsers)");
    if (trace != null) trace.args(userId);
    if (trace != null) trace.args(recipientOk);
    if (trace != null) trace.args(senderOk);
    if (trace != null) trace.args(includeWebUsers);

    Long myUserId = cache.getMyUserId();
    Record familiarUser = null;
    if (recipientOk) {
      ContactRecord cRec = cache.getContactRecordOwnerWith(myUserId, userId);
      if (cRec != null && (cRec.isOfActiveTypeAnyState() || cRec.isOfInitiatedType()))
        familiarUser = cRec;
    }
    if (familiarUser == null && senderOk) {
      ContactRecord cRec = cache.getContactRecordOwnerWith(userId, myUserId);
      if (cRec != null && cRec.isOfActiveTypeAnyState())
        familiarUser = cRec;
    }
    ContactRecord cRec = (ContactRecord) familiarUser;
    if (cRec != null && cRec.isOfActiveTypeAnyState()) {
      // good contact
    } else if (cRec != null && cRec.ownerUserId.equals(myUserId) && cRec.isOfInitiatedType()) {
      // Ok, my initiated contact has meaningful name.
      // Owner check is necessary, because someone elses initiated contact with me doesn't have a meaningful name.
    } else if (cRec == null ||
          // or not acknowledged, only acknowledged contacts have meaningful names...
          (!cRec.isOfActiveType() &&
          cRec.status != null && cRec.status.shortValue() != ContactRecord.STATUS_DECLINED_ACKNOWLEDGED)
        )
    {
      UserRecord uRec = cache.getUserRecord(userId);
      if (uRec != null) {
        if (includeWebUsers || uRec.status == null || !uRec.isWebAccount())
          familiarUser = uRec;
      } else {
        uRec = new UserRecord();
        uRec.userId = userId;
        familiarUser = uRec;
      }
    }

    if (trace != null) trace.exit(CacheUsrUtils.class, familiarUser);
    return familiarUser;
  }

  /**
  * @return All user IDs that have access to specified shares through share ownerships or groups
  * Do not include related shares lookup
  */
  public static Long[] findAccessUsers(final FetchedDataCache cache, FolderShareRecord[] shares) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUsrUtils.class, "findAccessUsers(con, FolderShareRecord[] shares)");
    if (trace != null) trace.args(shares);
    HashSet uIDsSet = new HashSet();
    HashSet gIDsSet = new HashSet();
    findAccessUsers(cache, shares, uIDsSet, gIDsSet);
    Long[] userIDs = new Long[uIDsSet.size()];
    uIDsSet.toArray(userIDs);
    if (trace != null) trace.exit(CacheUsrUtils.class, userIDs);
    return userIDs;
  }
  private static void findAccessUsers(final FetchedDataCache cache, FolderShareRecord[] shares, Set uIDsSet, Set gIDsSet) {
    Long[] uIDs = FolderShareRecord.getOwnerUserIDs(shares);
    if (uIDs != null) {
      uIDsSet.addAll(Arrays.asList(uIDs));
    }
    Long[] gIDs = FolderShareRecord.getOwnerGroupIDs(shares);
    if (gIDs != null) {
      // find related shares of groups not already processed to avoid infinite recurrsion
      ArrayList gIDsL = null;
      for (int i=0; i<gIDs.length; i++) {
        if (gIDsSet.add(gIDs[i])) {
          if (gIDsL == null) gIDsL = new ArrayList();
          gIDsL.add(gIDs[i]);
        }
      }
      // for all groups not already processed, find accessors
      if (gIDsL != null && gIDsL.size() > 0) {
        gIDs = new Long[gIDsL.size()];
        gIDsL.toArray(gIDs);
        FolderShareRecord[] groupShares = cache.getFolderShareRecordsForFolders(gIDs);
        if (groupShares != null) {
          findAccessUsers(cache, groupShares, uIDsSet, gIDsSet);
        }
      }
    }
  }

  /**
  * @return personal (nullable), short, full parts of default email address for specified user
  */
  public static String[] getCachedDefaultEmail(final FetchedDataCache cache, UserRecord userRecord, boolean isGeneratePersonalPart) {
    String[] emailAddr = null;
    if (userRecord.defaultEmlId != null && userRecord.defaultEmlId.longValue() != UserRecord.GENERIC_EMAIL_ID) {
      EmailRecord emlRec = cache.getEmailRecord(userRecord.defaultEmlId);
      if (emlRec != null) {
        emailAddr = new String[3];
        String personal = emlRec.personal;
        if (isGeneratePersonalPart) {
          personal = personal != null ? personal : userRecord.handle;
          emailAddr[2] = personal + " <" + emlRec.emailAddr + ">";
        } else {
          emailAddr[2] = emlRec.getEmailAddressFull();
        }
        emailAddr[0] = personal;
        emailAddr[1] = emlRec.emailAddr;
      }
    }
    return emailAddr;
  }

  public static String getDefaultApplicationTitle(final FetchedDataCache cache, UserRecord userRecord) {
    String emailStr = "";
    if (!userRecord.isWebAccount()) {
      String[] emailStrings = CacheUsrUtils.getCachedDefaultEmail(cache, userRecord, false);
      emailStr = emailStrings != null ? " :: " + emailStrings[2] : "";
    }
    String title = userRecord.shortInfo() + emailStr;
    return title;
  }


  /**
  * @return a set of personal(nullable)/short/full version of email address.
  */
  public static String[] getEmailAddressSet(final FetchedDataCache cache, Record rec) {
    String[] emailSet = null;
    if (rec instanceof UserRecord) {
      emailSet = CacheUsrUtils.getCachedDefaultEmail(cache, (UserRecord) rec, false);
    } else if (rec instanceof ContactRecord) {
      ContactRecord cRec = (ContactRecord) rec;
      Long myUserId = cache.getMyUserId();
      Long otherUserId = null;
      if (cRec.ownerUserId.equals(myUserId)) {
        otherUserId = cRec.contactWithId;
      } else if (cRec.contactWithId.equals(myUserId)) {
        otherUserId = cRec.ownerUserId;
      }
      // try using UserRecord, else return NULL
      UserRecord uRec = cache.getUserRecord(otherUserId);
      if (uRec != null) {
        emailSet = CacheUsrUtils.getCachedDefaultEmail(cache, (UserRecord) uRec, false);
      }
      if (emailSet == null) {
        emailSet = new String[3];
        emailSet[0] = TextRenderer.getRenderedText(cache, cRec);
        emailSet[1] = "" + otherUserId + "@" + URLs.getElements(URLs.DOMAIN_MAIL)[0];
        emailSet[2] = emailSet[0] + " <" + otherUserId + "@" + URLs.getElements(URLs.DOMAIN_MAIL)[0] + ">";
      }
    } else if (rec instanceof EmailAddressRecord) {
      emailSet = new String[3];
      emailSet[0] = EmailRecord.getPersonal(((EmailAddressRecord) rec).address);
      emailSet[1] = ((EmailAddressRecord) rec).address;
      emailSet[2] = ((EmailAddressRecord) rec).address;
      //Email.
      // remove possible Full Name part
      String nick = EmailRecord.getNick(emailSet[1]);
      String domain = EmailRecord.getDomain(emailSet[1]);
      if (nick != null && nick.length() > 0 && domain != null && domain.length() > 0) {
        emailSet[1] = nick + "@" + domain;
      }
    } else if (rec instanceof EmailRecord) {
      emailSet = new String[2];
      emailSet[0] = ((EmailRecord) rec).personal;
      emailSet[1] = ((EmailRecord) rec).emailAddr;
      emailSet[2] = ((EmailRecord) rec).getEmailAddressFull();
    }
    return emailSet;
  }
}