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

package com.CH_cl.service.cache;

import com.CH_cl.service.records.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.cryptx.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *  Utilities to work with cache and records.
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class CacheUtilities extends Object {

  /** Hide the constructor, all methods are static. */
  private CacheUtilities() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUtilities.class, "CacheUtilities()");
    if (trace != null) trace.exit(CacheUtilities.class);
  }

  public static boolean hasAttachments(Record rec) {
    boolean hasAttachments = false;
    if (rec instanceof MsgLinkRecord) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgDataRecord msgData = cache.getMsgDataRecord(((MsgLinkRecord) rec).msgId);
      hasAttachments = msgData.hasAttachments();
    } else if (rec instanceof MsgDataRecord) {
      hasAttachments = ((MsgDataRecord) rec).hasAttachments();
    }
    return hasAttachments;
  }

   /** Find a corresponding pair in the cache for each record
    * <code> recs </code> can be an instance of FolderRecord or FolderShareRecord
    * Pairs can only exist with the current user's shares, cannot form a pair with some other share.
    */
  public static FolderPair convertRecordToPair(Record rec) {
    FolderPair[] pairs = convertRecordToPairs(rec);
    FolderPair pair = null;
    if (pairs != null && pairs.length > 0)
      pair = pairs[0];
    return pair;
  }
  public static FolderPair[] convertRecordToPairs(Record rec) {
    FolderPair[] pairs = convertRecordsToPairs(new Record[] { rec });
    return pairs;
  }
  public static FolderPair[] convertRecordsToPairs(Record[] recs) {
    FolderPair[] pairs = convertRecordsToPairs(recs, false);
    return pairs;
  }
  public static FolderPair[] convertRecordsToPairs(Record[] recs, boolean makeupPairsIfDoNotExist) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUtilities.class, "convertRecordsToPairs(Record[], boolean makeupPairsIfDoNotExist)");
    if (trace != null) trace.args(recs);
    if (trace != null) trace.args(makeupPairsIfDoNotExist);

    FolderPair[] folderPairs = null;

    if (recs != null && recs.length > 0) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      ArrayList list = new ArrayList(recs.length);
      FolderPair folderPair = null;
      Long userId = cache.getMyUserId();
      Set groupIDsSet = null;

      for (int i=0; i<recs.length; i++) {
        Record rec = recs[i];
        if (rec instanceof FolderShareRecord) {
          FolderShareRecord shareRecord = (FolderShareRecord) rec;
          if (shareRecord.ownerUserId.equals(userId)) {
            FolderRecord folderRecord = cache.getFolderRecord(shareRecord.folderId);
            if (folderRecord == null && makeupPairsIfDoNotExist) {
              folderRecord = new FolderRecord();
              folderRecord.folderId = shareRecord.folderId;
              folderRecord.ownerUserId = userId;
            }
            if (folderRecord != null) {
              folderPair = new FolderPair(shareRecord, folderRecord);
              list.add(folderPair);
            }
          }
        } else if (rec instanceof FolderRecord) {
          FolderRecord folderRecord = (FolderRecord) rec;
          if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
          FolderShareRecord shareRecord = cache.getFolderShareRecordMy(folderRecord.getId(), groupIDsSet);
          if (shareRecord == null && makeupPairsIfDoNotExist) {
            shareRecord = new FolderShareRecord();
            shareRecord.folderId = folderRecord.folderId;
            shareRecord.ownerUserId = userId;
          }
          if (shareRecord != null) {
            folderPair = new FolderPair(shareRecord, folderRecord);
            list.add(folderPair);
          }
        } else {
          throw new IllegalArgumentException("Instance " + rec.getClass().getName() + " is not supported in this call!");
        }
      }

      folderPairs = (FolderPair[]) ArrayUtils.toArray(list, FolderPair.class);
    }

    if (trace != null) trace.exit(CacheUtilities.class, folderPairs);
    return folderPairs;
  }

  /**
   * Familiar are those in AddressBooks, not in WhiteLists, first the first match, ignore others.
   */
  public static Record convertToFamiliarEmailRecord(String emailAddress) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Record familiar = null;
    MsgDataRecord[] addrRecords = cache.getAddrRecords(emailAddress);
    MsgDataRecord addrRecord = addrRecords != null && addrRecords.length > 0 ? addrRecords[0] : null;
    if (addrRecord != null) {
      // we know that something like that exists, now find it from ADDRESS BOOKS only
      FolderRecord[] addrBooks = cache.getFolderRecords(new FolderFilter(FolderRecord.ADDRESS_FOLDER));
      MsgLinkRecord[] addrLinks = cache.getMsgLinkRecordsForFolders(RecordUtils.getIDs(addrBooks));
      Long[] addrIDs = MsgLinkRecord.getMsgIDs(addrLinks);
      addrRecords = cache.getAddrRecords(emailAddress, addrIDs);
      addrRecord = addrRecords != null ? addrRecords[0] : null;
    }
    familiar = addrRecord;
    if (familiar == null)
      familiar = new EmailAddressRecord(emailAddress);
    return familiar;
  }

  public static void makeFolderCategories(Long userId, List foldersBufferL, List sharesBufferL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUtilities.class, "makeFolderCategories(List folderRecordBufferL, List shareRecordBufferL)");
    if (userId != null) {
      addCategoryFolder(FolderRecord.CATEGORY_MAIL_ID, FolderShareRecord.CATEGORY_MAIL_ID, FolderRecord.CATEGORY_MAIL_FOLDER, userId, foldersBufferL, sharesBufferL);
      addCategoryFolder(FolderRecord.CATEGORY_FILE_ID, FolderShareRecord.CATEGORY_FILE_ID, FolderRecord.CATEGORY_FILE_FOLDER, userId, foldersBufferL, sharesBufferL);
      addCategoryFolder(FolderRecord.FOLDER_LOCAL_ID, FolderShareRecord.SHARE_LOCAL_ID, FolderRecord.LOCAL_FILES_FOLDER, userId, foldersBufferL, sharesBufferL);
      addCategoryFolder(FolderRecord.CATEGORY_CHAT_ID, FolderShareRecord.CATEGORY_CHAT_ID, FolderRecord.CATEGORY_CHAT_FOLDER, userId, foldersBufferL, sharesBufferL);
      addCategoryFolder(FolderRecord.CATEGORY_GROUP_ID, FolderShareRecord.CATEGORY_GROUP_ID, FolderRecord.CATEGORY_GROUP_FOLDER, userId, foldersBufferL, sharesBufferL);
    }
    if (trace != null) trace.exit(CacheUtilities.class);
  }

  private static void addCategoryFolder(long folderId, long shareId, short folderType, Long userId, List foldersL, List sharesL) {
    // add Category Folder
    if (foldersL != null) {
      FolderRecord fldRec = new FolderRecord();
      fldRec.folderId = new Long(folderId);
      fldRec.parentFolderId = fldRec.folderId;
      fldRec.ownerUserId = userId;
      fldRec.folderType = new Short(folderType);
      fldRec.numToKeep = null;
      fldRec.keepAsOldAs = null;
      fldRec.numOfShares = new Short((short)1);
      fldRec.dateCreated = null;
      fldRec.dateUpdated = null;
      foldersL.add(fldRec);
    }
    // add related share
    if (sharesL != null) {
      FolderShareRecord shrRec = new FolderShareRecord();
      shrRec.shareId = new Long(shareId);
      shrRec.folderId = new Long(folderId);
      shrRec.ownerType = new Short(Record.RECORD_TYPE_USER);
      shrRec.ownerUserId = userId;
      shrRec.dateCreated = null;
      shrRec.dateUpdated = null;
      sharesL.add(shrRec);
    }
  }

  public static void unlockPassProtectedMsg(MsgDataRecord msgDataRecStartWith, Hasher.Set bodyKey) {
    // first unseal the specific message so its fast for the user in the view
    unlockPassProtectedMsgs(new MsgDataRecord[] { msgDataRecStartWith }, bodyKey);
    // next unseal any other message that might be using the same password...
    Thread th = new ThreadTraced("UnlockPassProtectedMsgs Runner") {
      public void runTraced() {
        unlockPassProtectedMsgs(null, null);
      }
    };
    th.setDaemon(true);
    th.start();
  }
  private static void unlockPassProtectedMsgs(MsgDataRecord[] msgDatas, Hasher.Set bodyKey) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (msgDatas == null)
      msgDatas = cache.getMsgDataRecords(new MsgFilter((Boolean) null, (Boolean) null, Boolean.FALSE));
    List bodyKeys = null;
    if (bodyKey == null) {
      bodyKeys = cache.getMsgBodyKeys();
    } else {
      bodyKeys = new ArrayList();
      bodyKeys.add(bodyKey);
    }
    ArrayList msgDatasL = new ArrayList();
    ArrayList msgLinksL = new ArrayList();
    if (msgDatas != null) {
      for (int i=0; i<msgDatas.length; i++) {
        MsgLinkRecord[] msgLinks = cache.getMsgLinkRecordsForMsg(msgDatas[i].msgId);
        if (msgLinks != null && msgLinks.length > 0) {
          // Find a symmetric key from links that might have been password protected and not unsealed yet...
          BASymmetricKey symmetricKey = null;
          for (int k=0; k<msgLinks.length; k++) {
            if (msgLinks[k].getSymmetricKey() != null) {
              symmetricKey = msgLinks[k].getSymmetricKey();
              break;
            }
          }
          if (symmetricKey != null) {
            if (msgDatas[i].getSendPrivKeyId() != null) {
              // for performance don't verify everything, do it when person asks to see it
              msgDatas[i].unSealWithoutVerify(symmetricKey, bodyKeys);
            }
            //msgDatas[i].unSealWithoutVerify(symmetricKey, bodyKeys);
            if (msgDatas[i].getTextBody() != null) {
              MsgLinkRecord.clearPostRenderingCache(msgLinks);
              msgDatasL.add(msgDatas[i]);
              for (int k=0; k<msgLinks.length; k++)
                msgLinksL.add(msgLinks[k]);
            }
          }
        }
      }
    }
    MsgDataRecord[] msgDatasUnsealed = (MsgDataRecord[]) ArrayUtils.toArray(msgDatasL, MsgDataRecord.class);
    MsgLinkRecord[] msgLinksUnsealed = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksL, MsgLinkRecord.class);
    // trigger listeners to update changed messages
    cache.addMsgDataRecords(msgDatasUnsealed);
    cache.addMsgLinkRecords(msgLinksUnsealed);

    if (msgDatasUnsealed != null && msgDatasUnsealed.length > 0) {
      // check if unsealing some messages can unlock any attachments
      Long[] msgDataIDsUnsealed = RecordUtils.getIDs(msgDatasUnsealed);
      // start with file attachments...
      FileLinkRecord[] fileLinkAttachments = cache.getFileLinkRecordsOwnersAndType(msgDataIDsUnsealed, new Short(Record.RECORD_TYPE_MESSAGE));
      // adding attachments back to cache will unseal them and allow for unsealing of nested bodies...
      // it will also refresh registered listener viewes with unsealed data...
      cache.addFileLinkRecords(fileLinkAttachments);
      // follow with message attachments...
      MsgLinkRecord[] msgLinkAttachments = cache.getMsgLinkRecordsOwnersAndType(msgDataIDsUnsealed, new Short(Record.RECORD_TYPE_MESSAGE));
      // adding attachments back to cache will unseal them and allow for unsealing of nested bodies...
      cache.addMsgLinkRecords(msgLinkAttachments);
      // if there are message attachments present, unseal their bodies too
      // it will also refresh registered listener viewes with unsealed data...
      if (msgLinkAttachments != null && msgLinkAttachments.length > 0) {
        Long[] msgLinkAttachmentIDs = RecordUtils.getIDs(msgLinkAttachments);
        MsgDataRecord[] msgDataAttachments = cache.getMsgDataRecordsForLinks(msgLinkAttachmentIDs);
        if (msgDataAttachments != null && msgDataAttachments.length > 0) {
          // recursively go through the attachments
          unlockPassProtectedMsgs(msgDataAttachments, null);
        }
      }
    }
  }

  public static MsgLinkRecord[] getMsgLinkRecordsWithFetchedDatas(Long folderId) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgLinkRecord[] mLinks = cache.getMsgLinkRecordsForFolder(folderId);
    ArrayList mLinksFetchedL = new ArrayList();
    for (int i=0; i<mLinks.length; i++) {
      MsgDataRecord mData = cache.getMsgDataRecord(mLinks[i].msgId);
      if (mData != null && mData.getEncText() != null)
        mLinksFetchedL.add(mLinks[i]);
    }
    return (MsgLinkRecord[]) ArrayUtils.toArray(mLinksFetchedL, MsgLinkRecord.class);
  }

  /**
   * @return All user IDs that have access to specified shares through share ownerships or groups
   * Do not include related shares lookup
   */
  public static Long[] findAccessUsers(FolderShareRecord[] shares) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheUtilities.class, "findAccessUsers(con, FolderShareRecord[] shares)");
    if (trace != null) trace.args(shares);
    HashSet uIDsSet = new HashSet();
    HashSet gIDsSet = new HashSet();
    findAccessUsers(shares, uIDsSet, gIDsSet);
    Long[] userIDs = new Long[uIDsSet.size()];
    uIDsSet.toArray(userIDs);
    if (trace != null) trace.exit(CacheUtilities.class, userIDs);
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
}