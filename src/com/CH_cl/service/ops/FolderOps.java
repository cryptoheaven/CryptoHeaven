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

package com.CH_cl.service.ops;

import java.awt.*;
import java.util.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;

import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.util.*;


/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
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
  public static Fld_NewFld_Rq createNewFldRq( Component parentGUI,
                                              FolderPair parentPair, 
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
    Hashtable groupIDsHT = null;

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
        MessageDialog.showErrorDialog(parentGUI, "Could not fetch user's Public Key.  Operation terminated.", "Fetch Error");
        throw new RuntimeException("Could not fetch user's Public Key.  Operation terminated.");
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
          MessageDialog.showErrorDialog(parentGUI, "Could not fetch user's Public Key to encrypt folder shares.  Operation terminated.", "Fetch Error");
          throw new RuntimeException("Could not fetch user's Public Key to encrypt folder shares.  Operation terminated.");
        }
      } else {
        if (groupIDsHT == null) groupIDsHT = cache.getFolderGroupIDsMyHT();
        FolderShareRecord groupShare = cache.getFolderShareRecordMy(additionalShares[i].ownerUserId, groupIDsHT);
        // we should have the key of the group, but check just in case
        if (groupShare != null) {
          additionalShares[i].seal(groupShare.getSymmetricKey());
        } else {
          MessageDialog.showErrorDialog(parentGUI, "Could not locate group's encryption key.  Operation terminated.", "Fetch Error");
          throw new RuntimeException("Could not locate group's encryption key.  Operation terminated.");
        }
      }
    }

    // If we are creating any shares...
    if (additionalShareCount > 0) {
      // Specify contact list users only if I am creating my own folder, else let server check those for the folder owner
      if (myUserId.equals(ownerUserId)) {
        shareRequest.contactIds = new Obj_IDList_Co(RecordUtils.getIDs(cache.getContactRecordsMyActive()));
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
  public static FolderPair[] getAllChatFolderPairsFromCache(ContactRecord chatWithContact, FolderRecord[]  chatFlds, boolean includeNonOneToOne) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Vector chatFolderPairsV = null;
    // Any chatting folders present?
    if (chatFlds != null) {
      Long userId = cache.getMyUserId();
      for (int i=0; i<chatFlds.length; i++) {
        // By default only 1-1 folders are considered, with two shares, my and other.
        if (includeNonOneToOne || chatFlds[i].numOfShares.shortValue() == 2) {
          // Check folder's shares to confirm that one is mine and second other guy's.
          FolderShareRecord[] chatShares = cache.getFolderShareRecordsForFolder(chatFlds[i].folderId);
          if (chatShares != null && chatShares.length > 1 && (includeNonOneToOne || chatShares.length == 2)) {
            FolderRecord chatFolder = null;
            FolderShareRecord chatShare = null;
            boolean foundMy = false;
            boolean foundOther = false;
            for (int k=0; k<chatShares.length; k++) {
              if (chatShares[k].ownerUserId.equals(userId)) {
                chatShare = chatShares[k];
                foundMy = true;
              }
              else if (chatShares[k].ownerUserId.equals(chatWithContact.contactWithId))
                foundOther = true;
            }
            if (foundMy && foundOther) {
              // May begin chatting in currently found chat folder
              chatFolder = chatFlds[i];
              if (chatFolderPairsV == null) chatFolderPairsV = new Vector();
              chatFolderPairsV.addElement(new FolderPair(chatShare, chatFolder));
            }
          }
        }
      } // end for
    }
    // Found required chatting folder.
    FolderPair[] chatFolderPairs = (FolderPair[]) ArrayUtils.toArray(chatFolderPairsV, FolderPair.class);
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
      Fld_NewFld_Rq dataSet = FolderOps.createNewFldRq(null, null, FolderRecord.ADDRESS_FOLDER, "Address Book", "Saved Email Addresses", null, null, null, null, new BASymmetricKey(32), useInheritedSharing, null, SIL);
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
      fPair = CacheUtilities.convertRecordToPair(cache.getFolderRecord(addrFolderId));
    }
    return fPair;
  }

  public static FolderPair getOrCreateWhiteList(ServerInterfaceLayer SIL) { // pass in SIL so it works from APIs without main frame
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();
    Long whiteFolderId = userRecord.whiteFolderId;
    if (whiteFolderId == null || cache.getFolderRecord(whiteFolderId) == null) {
      boolean useInheritedSharing = false;
      Fld_NewFld_Rq dataSet = FolderOps.createNewFldRq(null, getOrCreateAddressBook(SIL), FolderRecord.WHITELIST_FOLDER, "Allowed Senders", "List of originators who you have decided should be able to send you messages WITHOUT being checked for Spam. This will prevent their messages being blocked even if our Anti-Spam process calculates that the message is spam.", null, null, null, null, new BASymmetricKey(32), useInheritedSharing, null, SIL);
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
      fPair = CacheUtilities.convertRecordToPair(cache.getFolderRecord(whiteFolderId));
    }
    return fPair;
  }

  public static FolderPair getOrCreateDraftFolder(ServerInterfaceLayer SIL) { // pass in SIL so it works from APIs without main frame
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();
    Long draftFolderId = userRecord.draftFolderId;
    if (draftFolderId == null || cache.getFolderRecord(draftFolderId) == null) {
      boolean useInheritedSharing = false;
      Fld_NewFld_Rq draftDataSet = FolderOps.createNewFldRq(null, null, FolderRecord.MESSAGE_FOLDER, "Drafts", "Saved Message Drafts", null, null, null, null, new BASymmetricKey(32), useInheritedSharing, null, SIL);
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
      fPair = CacheUtilities.convertRecordToPair(cache.getFolderRecord(draftFolderId));
    }
    return fPair;
  }

  public static FolderPair getOrCreateJunkFolder(ServerInterfaceLayer SIL) { // pass in SIL so it works from APIs without main frame
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();
    Long junkFolderId = userRecord.junkFolderId;
    if (junkFolderId == null || cache.getFolderRecord(junkFolderId) == null) {
      boolean useInheritedSharing = false;
      Fld_NewFld_Rq dataSet = FolderOps.createNewFldRq(null, null, FolderRecord.MESSAGE_FOLDER, "Spam", "Suspected spam e-mail is deposited here", null, null, null, new Integer(1296000), new BASymmetricKey(32), useInheritedSharing, null, SIL); // 15 days default expiry // "Junk e-mail"
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
      fPair = CacheUtilities.convertRecordToPair(cache.getFolderRecord(junkFolderId));
    }
    return fPair;
  }

  public static FolderPair getOrCreateRecycleFolder(ServerInterfaceLayer SIL) { // pass in SIL so it works from APIs without main frame
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();
    Long recycleFolderId = userRecord.recycleFolderId;
    if (recycleFolderId == null || cache.getFolderRecord(recycleFolderId) == null) {
      boolean useInheritedSharing = false;
      Fld_NewFld_Rq dataSet = FolderOps.createNewFldRq(null, null, FolderRecord.RECYCLE_FOLDER, "Recycle Bin", "Deleted items are deposited here", null, null, null, null, new BASymmetricKey(32), useInheritedSharing, null, SIL); // no default expiry
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
      fPair = CacheUtilities.convertRecordToPair(cache.getFolderRecord(recycleFolderId));
    }
    return fPair;
  }

  /**
   * Search among the cached folders, and find a folder which is most likely the
   * root in the tree view.
   * @return Root folder or null if guess cannot be made or some looping of folders is found.
   */
  public static FolderPair getRootmostFolderInViewHierarchy(Long childFolderId) {
    Hashtable visitedFolderIDsHT = new Hashtable();
    Hashtable groupIDsHT = FetchedDataCache.getSingleInstance().getFolderGroupIDsMyHT();
    return getRootmostFolderInViewHierarchy(childFolderId, visitedFolderIDsHT, groupIDsHT);
  }
  private static FolderPair getRootmostFolderInViewHierarchy(Long childFolderId, Hashtable visitedFolderIDsHT, Hashtable groupIDsHT) {
    // if visited, return null;
    if (visitedFolderIDsHT.get(childFolderId) == null) {
      visitedFolderIDsHT.put(childFolderId, childFolderId);
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      FolderRecord fRec = cache.getFolderRecord(childFolderId);
      if (fRec != null) {
        FolderShareRecord sRec = cache.getFolderShareRecordMy(fRec.folderId, groupIDsHT);
        if (sRec != null) {
          FolderPair fPair = new FolderPair(sRec, fRec);
          if (fPair.isViewRoot())
            return fPair;
          else {
            Long parentFolderId = fPair.getParentId();
            return getRootmostFolderInViewHierarchy(parentFolderId, visitedFolderIDsHT, groupIDsHT);
          }
        }
      }
    } else {
      return null;
    }
    return null;
  }

}