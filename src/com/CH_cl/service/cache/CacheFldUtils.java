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

import com.CH_co.service.records.FolderPair;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.records.Record;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
*
* @author Marcin
*/
public class CacheFldUtils {

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
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheFldUtils.class, "convertRecordsToPairs(Record[], boolean makeupPairsIfDoNotExist)");
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
          if (shareRecord.isOwnedByGroup() && groupIDsSet == null)
            groupIDsSet = cache.getFolderGroupIDsMySet();
          if (shareRecord.isOwnedBy(userId, groupIDsSet)) {
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

    if (trace != null) trace.exit(CacheFldUtils.class, folderPairs);
    return folderPairs;
  }

  public static void makeFolderCategories(Long userId, List foldersBufferL, List sharesBufferL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheFldUtils.class, "makeFolderCategories(List folderRecordBufferL, List shareRecordBufferL)");
    if (userId != null) {
      addCategoryFolder(FolderRecord.CATEGORY_MAIL_ID, FolderShareRecord.CATEGORY_MAIL_ID, FolderRecord.CATEGORY_MAIL_FOLDER, userId, foldersBufferL, sharesBufferL);
      addCategoryFolder(FolderRecord.CATEGORY_FILE_ID, FolderShareRecord.CATEGORY_FILE_ID, FolderRecord.CATEGORY_FILE_FOLDER, userId, foldersBufferL, sharesBufferL);
      addCategoryFolder(FolderRecord.FOLDER_LOCAL_ID, FolderShareRecord.SHARE_LOCAL_ID, FolderRecord.LOCAL_FILES_FOLDER, userId, foldersBufferL, sharesBufferL);
      addCategoryFolder(FolderRecord.CATEGORY_CHAT_ID, FolderShareRecord.CATEGORY_CHAT_ID, FolderRecord.CATEGORY_CHAT_FOLDER, userId, foldersBufferL, sharesBufferL);
      addCategoryFolder(FolderRecord.CATEGORY_GROUP_ID, FolderShareRecord.CATEGORY_GROUP_ID, FolderRecord.CATEGORY_GROUP_FOLDER, userId, foldersBufferL, sharesBufferL);
    }
    if (trace != null) trace.exit(CacheFldUtils.class);
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
}