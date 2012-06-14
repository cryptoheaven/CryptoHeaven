/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/
package com.CH_cl.service.cache;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
*
* @author Marcin
*/
public class CacheUsrUtils {

  /**
  * @return an acknowledged contact record or user record.
  */
  public static Record convertUserIdToFamiliarUser(Long userId, boolean recipientOk, boolean senderOk) {
    return convertUserIdToFamiliarUser(userId, recipientOk, senderOk, true);
  }
  public static Record convertUserIdToFamiliarUser(Long userId, boolean recipientOk, boolean senderOk, boolean includeWebUsers) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUsrUtils.class, "convertUserIdToFamiliarUser(Long userId, boolean recipientOk, boolean senderOk, boolean includeWebUsers)");
    if (trace != null) trace.args(userId);
    if (trace != null) trace.args(recipientOk);
    if (trace != null) trace.args(senderOk);
    if (trace != null) trace.args(includeWebUsers);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
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
      }
    }

    if (trace != null) trace.exit(CacheUsrUtils.class, familiarUser);
    return familiarUser;
  }

  /**
  * @return All user IDs that have access to specified shares through share ownerships or groups
  * Do not include related shares lookup
  */
  public static Long[] findAccessUsers(FolderShareRecord[] shares) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUsrUtils.class, "findAccessUsers(con, FolderShareRecord[] shares)");
    if (trace != null) trace.args(shares);
    HashSet uIDsSet = new HashSet();
    HashSet gIDsSet = new HashSet();
    findAccessUsers(shares, uIDsSet, gIDsSet);
    Long[] userIDs = new Long[uIDsSet.size()];
    uIDsSet.toArray(userIDs);
    if (trace != null) trace.exit(CacheUsrUtils.class, userIDs);
    return userIDs;
  }
  private static void findAccessUsers(FolderShareRecord[] shares, Set uIDsSet, Set gIDsSet) {
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
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderShareRecord[] groupShares = cache.getFolderShareRecordsForFolders(gIDs);
        if (groupShares != null) {
          findAccessUsers(groupShares, uIDsSet, gIDsSet);
        }
      }
    }
  }

  /**
  * @return personal (nullable), short, full parts of default email address for specified user
  */
  public static String[] getCachedDefaultEmail(UserRecord userRecord, boolean isGeneratePersonalPart) {
    String[] emailAddr = null;
    if (userRecord.defaultEmlId != null && userRecord.defaultEmlId.longValue() != UserRecord.GENERIC_EMAIL_ID) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
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

  public static String getDefaultApplicationTitle(UserRecord userRecord) {
    String emailStr = "";
    if (!userRecord.isWebAccount()) {
      String[] emailStrings = CacheUsrUtils.getCachedDefaultEmail(userRecord, false);
      emailStr = emailStrings != null ? " :: " + emailStrings[2] : "";
    }
    String title = userRecord.shortInfo() + emailStr;
    return title;
  }

}