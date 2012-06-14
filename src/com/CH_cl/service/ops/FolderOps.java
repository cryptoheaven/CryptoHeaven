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

package com.CH_cl.service.ops;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.fld.Fld_AddShares_Rq;
import com.CH_co.service.msg.dataSets.fld.Fld_Folders_Rp;
import com.CH_co.service.msg.dataSets.fld.Fld_NewFld_Rq;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.*;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.NotificationCenter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/** 
* <b>Copyright</b> &copy; 2001-2012
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
* <b>$Revision: 1.22 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class FolderOps extends Object {


  /**
  * Static method to fill the New Folder Request with data provided (usually from GUI entry fields)
  * @return Fld_NewFld_Rq data set suitable to send as a request for new folder, or creat/fetch of chat folder.
  */
  public static Fld_NewFld_Rq createNewFldRq( FolderPair parentPair,
                                              short newFolderType,
                                              String newFolderName,
                                              String newFolderDesc,
                                              String newShareName,
                                              String newShareDesc,
                                              Short numToKeep,
                                              Integer keepAsOldAs,
                                              BASymmetricKey baSymmetricKey,
                                              boolean useInheritedSharing,
                                              FolderShareRecord[] additionalShares,
                                              ServerInterfaceLayer SIL
                                            )
  {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    Fld_NewFld_Rq request = new Fld_NewFld_Rq();
    request.folderShareRecord = new FolderShareRecord();

    FolderRecord parentFolder = null;
    Long parentFolderId = null;
    Long parentShareId = null;
    Long myUserId = cache.getMyUserId();
    Short ownerType = new Short(Record.RECORD_TYPE_USER);
    Long ownerUserId = myUserId;
    Set groupIDsSet = null;

    if (parentPair != null) {
      parentFolder = parentPair.getFolderRecord();
      if (!useInheritedSharing && parentFolder.ownerUserId.equals(cache.getMyUserId()))
        parentFolderId = parentFolder.folderId;
      parentShareId = parentPair.getFolderShareRecord().getId();
      request.folderShareRecord.setViewParentId(parentFolder.folderId);
    }

    if (useInheritedSharing && parentPair != null) {
      ownerUserId = parentFolder.ownerUserId;
      // If inheriting sharing, folder owner is the same as parent folder owner
      parentFolderId = parentFolder.folderId;
      // Create a folder shares that reflect parent folder sharing properties.
      int additionalShareCount = parentFolder.numOfShares.shortValue() - 1;
      // get all shares of the parent folder, if not in cache, fetch it
      FolderShareRecord[] parentShares = cache.getFolderShareRecordsForFolder(parentFolderId);
      if (parentShares == null || parentShares.length != additionalShareCount + 1) {
        SIL.submitAndWait(new MessageAction(CommandCodes.FLD_Q_GET_FOLDER_SHARES, new Obj_IDList_Co(parentShareId)), 30000);
        parentShares = cache.getFolderShareRecordsForFolder(parentFolderId);
      }
      // reset specified additional shares and replace them with newly generated ones if any
      additionalShares = null;
      if (additionalShareCount > 0) {
        additionalShares = new FolderShareRecord[additionalShareCount];
        int index = 0; // counting parentShares
        int count = 0; // counting additionalShares which excludes parent owner's share
        do {
          FolderShareRecord parentShare = parentShares[index];
          if (!parentShare.ownerUserId.equals(parentFolder.ownerUserId)) {
            FolderShareRecord newShare = new FolderShareRecord();
            newShare.ownerType = parentShare.ownerType;
            newShare.ownerUserId = parentShare.ownerUserId;
            newShare.canWrite = parentShare.canWrite;
            newShare.canDelete = parentShare.canDelete;
            newShare.setSymmetricKey(baSymmetricKey);
            additionalShares[count] = newShare;
            count ++;
          }
          index ++;
        } while (count < additionalShareCount);
      }
    }

    request.parentFolderId = parentFolderId;
    request.parentShareId = parentShareId;

    request.folderType = new Short(newFolderType);

    request.folderShareRecord.setFolderName(newFolderName.trim());
    if (newFolderDesc.trim().length() > 0) {
      request.folderShareRecord.setFolderDesc(newFolderDesc.trim());
    }

    request.numToKeep = numToKeep != null ? numToKeep : new Short((short)0);
    request.keepAsOldAs = keepAsOldAs != null ? keepAsOldAs : new Integer(0);

    request.folderShareRecord.setSymmetricKey(baSymmetricKey);
    request.folderShareRecord.ownerType = ownerType;
    request.folderShareRecord.ownerUserId = ownerUserId;

    // user ids of people who are involved but we don't have public keys cached
    Vector userIDsOfNeededKeysV = new Vector();
    KeyRecord keyRec = cache.getKeyRecordForUser(ownerUserId);
    if (keyRec == null || keyRec.plainPublicKey == null) {
      userIDsOfNeededKeysV.addElement(ownerUserId);
    }

    int additionalShareCount = additionalShares != null ? additionalShares.length : 0;

    // create a folder sharing request
    Fld_AddShares_Rq shareRequest = new Fld_AddShares_Rq();

    // Fetch public keys for involved users that are not already fetched
    for (int i=0; i<additionalShareCount; i++) {
      if (additionalShares[i].isOwnedByUser()) {
        Long uId = additionalShares[i].ownerUserId;
        KeyRecord kR = cache.getKeyRecordForUser(uId);
        if (kR == null || kR.plainPublicKey == null)
          if (!userIDsOfNeededKeysV.contains(uId))
            userIDsOfNeededKeysV.addElement(uId);
      }
    }
    // We are lacking public KEYs, fetch them now.
    if (userIDsOfNeededKeysV.size() > 0) {
      SIL.submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, new Obj_IDList_Co(userIDsOfNeededKeysV)), 120000);
    }

    {
      try {
        if (request.folderShareRecord.isOwnedBy(myUserId, (Long[]) null)) {
          request.folderShareRecord.seal(cache.getUserRecord().getSymKeyFldShares());
        } else {
          KeyRecord ownerKeyRec = cache.getKeyRecordForUser(ownerUserId);
          request.folderShareRecord.seal(ownerKeyRec);
        }
      } catch (Throwable t) {
        String title = "Fetch Error";
        String msg = "Could not fetch user's Public Key.  Operation terminated.";
        NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, title, msg);
        throw new RuntimeException(msg);
      }
    }

    for (int i=0; i<additionalShareCount; i++) {
      additionalShares[i].setFolderName(newShareName.trim());
      String desc = newShareDesc.trim();
      if (desc.length() > 0) {
        additionalShares[i].setFolderDesc(desc);
      } else {
        additionalShares[i].setFolderDesc(null);
      }
      if (additionalShares[i].isOwnedByUser()) {
        KeyRecord kRec = cache.getKeyRecordForUser(additionalShares[i].ownerUserId);
        // we should have the public key of the user, but check just in case
        if (kRec != null) {
          if (additionalShares[i].ownerUserId.equals(myUserId))
            additionalShares[i].seal(cache.getUserRecord().getSymKeyFldShares());
          else
            additionalShares[i].seal(kRec);
        } else {
          String title = "Fetch Error";
          String msg = "Could not fetch user's Public Key to encrypt folder shares.  Operation terminated.";
          NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, title, msg);
          throw new RuntimeException(msg);
        }
      } else {
        if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
        FolderShareRecord groupShare = cache.getFolderShareRecordMy(additionalShares[i].ownerUserId, groupIDsSet);
        // we should have the key of the group, but check just in case
        if (groupShare != null) {
          additionalShares[i].seal(groupShare.getSymmetricKey());
        } else {
          String title = "Fetch Error";
          String msg = "Could not locate group's encryption key.  Operation terminated.";
          NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, title, msg);
          throw new RuntimeException(msg);
        }
      }
    }

    // If we are creating any shares...
    if (additionalShareCount > 0) {
      // Specify contact list users only if I am creating my own folder, else let server check those for the folder owner
      if (myUserId.equals(ownerUserId)) {
        shareRequest.contactIds = new Obj_IDList_Co(RecordUtils.getIDs(cache.getContactRecordsMyActive(true)));
        shareRequest.groupShareIds = new Obj_IDList_Co(RecordUtils.getIDs(cache.getFolderSharesMyForFolders(cache.getFolderGroupIDsMy(), true)));
      }
      shareRequest.shareRecords = additionalShares;
      request.addSharesRequest = shareRequest;
    }

    return request;
  }

  /**
  * @return all chatting folder pairs related to the specified contact, including multi-user chat folders.
  */
  public static FolderPair[] getAllChatFolderPairsFromCache(ContactRecord chatWithContact, FolderRecord[]  chatFlds) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    ArrayList chatFolderPairsL = null;
    // Any chatting folders present?
    if (chatFlds != null) {
      Long userId = cache.getMyUserId();
      for (int i=0; i<chatFlds.length; i++) {
        // Check folder's shares to confirm that one is mine and second other guy's.
        FolderShareRecord[] chatShares = cache.getFolderShareRecordsForFolder(chatFlds[i].folderId);
        Long[] accessUserIDs = CacheUsrUtils.findAccessUsers(chatShares);
        if (accessUserIDs.length >= 2) {
          boolean foundMy = false;
          boolean foundOther = false;
          for (int a=0; a<accessUserIDs.length; a++) {
            Long accessUserId = accessUserIDs[a];
            if (accessUserId.equals(userId))
              foundMy = true;
            else if (accessUserId.equals(chatWithContact.contactWithId))
              foundOther = true;
          }
          if (foundMy && foundOther) {
            if (chatFolderPairsL == null) chatFolderPairsL = new ArrayList();
            FolderShareRecord chatShare = cache.getFolderShareRecordMy(chatFlds[i].folderId, true);
            chatFolderPairsL.add(new FolderPair(chatShare, chatFlds[i]));
          }
        }
      } // end for
    }
    // Found required chatting folder.
    FolderPair[] chatFolderPairs = (FolderPair[]) ArrayUtils.toArray(chatFolderPairsL, FolderPair.class);
    return chatFolderPairs;
  }

  /**
  * @return chatting folder pair for chatting with specified contact searching between specified chat folder records.
  */
  public static FolderPair getChatFolderPairFromCache(MemberContactRecordI[] chatWithContacts, FolderRecord[] chatFlds) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderRecord chatFolder = null;
    FolderShareRecord chatShare = null;
    // Any chatting folders present?
    if (chatFlds != null) {
      Long userId = cache.getMyUserId();
      for (int i=0; i<chatFlds.length; i++) {
        // Only 1-1 folders are considered, with two shares, my and other.
        if (chatFlds[i].numOfShares.shortValue() == 1 + chatWithContacts.length) {
          // Check folder's shares to confirm that one is mine and second other guy's.
          FolderShareRecord[] chatShares = cache.getFolderShareRecordsForFolder(chatFlds[i].folderId);
          if (chatShares != null && chatShares.length == 1 + chatWithContacts.length) {
            boolean foundMy = false;
            boolean anyOtherMissing = false;
            for (int j=0; j<chatWithContacts.length; j++) {
              boolean foundOther = false;
              Long ownerUserId = null;
              Long[] ownerGroupId = null;
              if (chatWithContacts[j].getMemberType() == Record.RECORD_TYPE_USER)
                ownerUserId = ((ContactRecord) chatWithContacts[j]).contactWithId;
              else if (chatWithContacts[j].getMemberType() == Record.RECORD_TYPE_GROUP)
                ownerGroupId = new Long[] {((FolderPair) chatWithContacts[j]).getId()};
              for (int k=0; k<chatShares.length; k++) {
                if (chatShares[k].isOwnedBy(userId, (Long[]) null)) {
                  chatShare = chatShares[k];
                  foundMy = true;
                } else if (chatShares[k].isOwnedBy(ownerUserId, ownerGroupId)) {
                  foundOther = true;
                }
              }
              if (!foundOther) {
                anyOtherMissing = true;
              }
            }
            if (foundMy && !anyOtherMissing) {
              // May begin chatting in currently found chat folder
              chatFolder = chatFlds[i];
              break;
            }
          }
        }
      } // end for
    }
    FolderPair chatFolderPair = null;
    // Found required chatting folder.
    if (chatFolder != null) {
      chatFolderPair = new FolderPair(chatShare, chatFolder);
    }
    return chatFolderPair;
  }

  public static FolderPair getOrCreateAddressBook(ServerInterfaceLayer SIL) { // pass in SIL so it works from APIs without main frame
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();
    Long addrFolderId = userRecord.addrFolderId;
    if (addrFolderId == null || cache.getFolderRecord(addrFolderId) == null) {
      boolean useInheritedSharing = false;
      Fld_NewFld_Rq dataSet = FolderOps.createNewFldRq(null, FolderRecord.ADDRESS_FOLDER, "Address Book", "Saved Email Addresses", null, null, null, null, new BASymmetricKey(32), useInheritedSharing, null, SIL);
      ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_NEW_DFT_ADDRESS_OR_GET_OLD, dataSet), 60000);
      // assign folder id back to UserRecord in cache to avoid potential loops
      if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS) {
        Fld_Folders_Rp fldSet = (Fld_Folders_Rp) msgAction.getMsgDataSet();
        addrFolderId = fldSet.folderRecords[0].folderId;
        userRecord.addrFolderId = addrFolderId;
      }
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    }
    FolderPair fPair = null;
    if (addrFolderId != null) {
      fPair = CacheFldUtils.convertRecordToPair(cache.getFolderRecord(addrFolderId));
    }
    return fPair;
  }

  public static FolderPair getOrCreateWhiteList(ServerInterfaceLayer SIL) { // pass in SIL so it works from APIs without main frame
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();
    Long whiteFolderId = userRecord.whiteFolderId;
    if (whiteFolderId == null || cache.getFolderRecord(whiteFolderId) == null) {
      boolean useInheritedSharing = false;
      Fld_NewFld_Rq dataSet = FolderOps.createNewFldRq(getOrCreateAddressBook(SIL), FolderRecord.WHITELIST_FOLDER, "Allowed Senders", "List of originators who you have decided should be able to send you messages WITHOUT being checked for Spam. This will prevent their messages being blocked even if our Anti-Spam process calculates that the message is spam.", null, null, null, null, new BASymmetricKey(32), useInheritedSharing, null, SIL);
      ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_NEW_DFT_WHITE_OR_GET_OLD, dataSet), 60000);
      // assign folder id back to UserRecord in cache to avoid potential loops
      if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS) {
        Fld_Folders_Rp fldSet = (Fld_Folders_Rp) msgAction.getMsgDataSet();
        whiteFolderId = fldSet.folderRecords[0].folderId;
        userRecord.whiteFolderId = whiteFolderId;
      }
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    }
    FolderPair fPair = null;
    if (whiteFolderId != null) {
      fPair = CacheFldUtils.convertRecordToPair(cache.getFolderRecord(whiteFolderId));
    }
    return fPair;
  }

  public static FolderPair getOrCreateDraftFolder(ServerInterfaceLayer SIL) { // pass in SIL so it works from APIs without main frame
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();
    Long draftFolderId = userRecord.draftFolderId;
    if (draftFolderId == null || cache.getFolderRecord(draftFolderId) == null) {
      boolean useInheritedSharing = false;
      Fld_NewFld_Rq draftDataSet = FolderOps.createNewFldRq(null, FolderRecord.MESSAGE_FOLDER, "Drafts", "Saved Message Drafts", null, null, null, null, new BASymmetricKey(32), useInheritedSharing, null, SIL);
      ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_NEW_DFT_DRAFT_OR_GET_OLD, draftDataSet), 60000);
      // assign folder id back to UserRecord in cache to avoid potential loops
      if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS) {
        Fld_Folders_Rp fldSet = (Fld_Folders_Rp) msgAction.getMsgDataSet();
        draftFolderId = fldSet.folderRecords[0].folderId;
        userRecord.draftFolderId = draftFolderId;
      }
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    }
    FolderPair fPair = null;
    if (draftFolderId != null) {
      fPair = CacheFldUtils.convertRecordToPair(cache.getFolderRecord(draftFolderId));
    }
    return fPair;
  }

  public static FolderPair getOrCreateJunkFolder(ServerInterfaceLayer SIL) { // pass in SIL so it works from APIs without main frame
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();
    Long junkFolderId = userRecord.junkFolderId;
    if (junkFolderId == null || cache.getFolderRecord(junkFolderId) == null) {
      boolean useInheritedSharing = false;
      Fld_NewFld_Rq dataSet = FolderOps.createNewFldRq(null, FolderRecord.MESSAGE_FOLDER, "Spam", "Suspected spam email is deposited here", null, null, null, new Integer(1296000), new BASymmetricKey(32), useInheritedSharing, null, SIL); // 15 days default expiry // "Junk email"
      ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_NEW_DFT_JUNK_OR_GET_OLD, dataSet), 60000);
      // assign folder id back to UserRecord in cache to avoid potential loops
      if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS) {
        Fld_Folders_Rp fldSet = (Fld_Folders_Rp) msgAction.getMsgDataSet();
        junkFolderId = fldSet.folderRecords[0].folderId;
        userRecord.junkFolderId = junkFolderId;
      }
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    }
    FolderPair fPair = null;
    if (junkFolderId != null) {
      fPair = CacheFldUtils.convertRecordToPair(cache.getFolderRecord(junkFolderId));
    }
    return fPair;
  }

  public static FolderPair getOrCreateRecycleFolder(ServerInterfaceLayer SIL) { // pass in SIL so it works from APIs without main frame
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();
    Long recycleFolderId = userRecord.recycleFolderId;
    if (recycleFolderId == null || cache.getFolderRecord(recycleFolderId) == null) {
      boolean useInheritedSharing = false;
      Fld_NewFld_Rq dataSet = FolderOps.createNewFldRq(null, FolderRecord.RECYCLE_FOLDER, "Recycle Bin", "Deleted items are deposited here", null, null, null, null, new BASymmetricKey(32), useInheritedSharing, null, SIL); // no default expiry
      ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_NEW_DFT_RECYCLE_OR_GET_OLD, dataSet), 60000);
      // assign folder id back to UserRecord in cache to avoid potential loops
      if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS) {
        Fld_Folders_Rp fldSet = (Fld_Folders_Rp) msgAction.getMsgDataSet();
        recycleFolderId = fldSet.folderRecords[0].folderId;
        userRecord.recycleFolderId = recycleFolderId;
      }
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    }
    FolderPair fPair = null;
    if (recycleFolderId != null) {
      fPair = CacheFldUtils.convertRecordToPair(cache.getFolderRecord(recycleFolderId));
    }
    return fPair;
  }

  /**
  * Search among the cached folders, and find a folder which is most likely the
  * root in the tree view.
  * @return Root folder or null if guess cannot be made or some looping of folders is found.
  */
  public static FolderPair getRootmostFolderInViewHierarchy(Long childFolderId) {
    HashSet visitedFolderIDsHS = new HashSet();
    Set groupIDsSet = FetchedDataCache.getSingleInstance().getFolderGroupIDsMySet();
    return getRootmostFolderInViewHierarchy(childFolderId, visitedFolderIDsHS, groupIDsSet);
  }
  private static FolderPair getRootmostFolderInViewHierarchy(Long childFolderId, Set visitedFolderIDsSet, Set groupIDsSet) {
    // if visited, return null;
    if (!visitedFolderIDsSet.contains(childFolderId)) {
      visitedFolderIDsSet.add(childFolderId);
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      FolderRecord fRec = cache.getFolderRecord(childFolderId);
      if (fRec != null) {
        FolderShareRecord sRec = cache.getFolderShareRecordMy(fRec.folderId, groupIDsSet);
        if (sRec != null) {
          FolderPair fPair = new FolderPair(sRec, fRec);
          if (fPair.isViewRoot())
            return fPair;
          else {
            Long parentFolderId = fPair.getParentId();
            return getRootmostFolderInViewHierarchy(parentFolderId, visitedFolderIDsSet, groupIDsSet);
          }
        }
      }
    } else {
      return null;
    }
    return null;
  }

}