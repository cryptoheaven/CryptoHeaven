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

import com.CH_cl.service.actions.msg.MsgAGet;
import com.CH_cl.service.records.FolderRecUtil;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.Misc;
import java.sql.Timestamp;
import java.util.*;

/**
*
* @author Marcin
*/
public class CacheFldUtils {

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

  public static void getKnownGroupMembers(Long groupId, ArrayList targetUIDsL) {
    ArrayList processedGroupIDsL = new ArrayList();
    getKnownGroupMembers_loop(groupId, targetUIDsL, processedGroupIDsL);
  }
  private static void getKnownGroupMembers_loop(Long groupId, ArrayList targetUIDsL, ArrayList processedGroupIDsL) {
    processedGroupIDsL.add(groupId);
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderShareRecord[] shares = cache.getFolderShareRecordsForFolder(groupId);
    if (shares != null) {
      for (int i=0; i<shares.length; i++) {
        FolderShareRecord share = shares[i];
        if (share.isOwnedByUser()) {
          if (!targetUIDsL.contains(share.ownerUserId))
            targetUIDsL.add(share.ownerUserId);
        } else {
          if (!processedGroupIDsL.contains(share.ownerUserId))
            getKnownGroupMembers_loop(share.ownerUserId, targetUIDsL, processedGroupIDsL);
        }
      }
    }
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


  public static List prepareSynchRequest(FetchedDataCache cache, Collection folderIDsL, Collection startStampsL, List resultRequestSetsL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheFldUtils.class, "prepareSynchRequest(FetchedDataCache cache, Collection folderIDsL, Collection startStampsL, List resultRequestSetsL)");
    if (trace != null) trace.args(folderIDsL, startStampsL);
    // smaller-and-faster batch for initial re-synch, larger for continuation requests
    int initialLow = startStampsL == null ? 20 : 200;
    int initialHigh = startStampsL == null ? 100 : 1000;
    // Distribute item count per folder depending of number for folders to re-synch.
    int maxItemsForFetchedFolders = Math.max(initialLow, (initialHigh/(folderIDsL.size()+1))*2);
    int maxItemsForNonFetchedFolders = 5;
    int maxItemsForProbingNewItems = 1;
    Object[] folderIDs = folderIDsL.toArray();
    Object[] startStamps = startStampsL != null ? startStampsL.toArray() : null;
    for (int i=0; i<folderIDs.length; i++) {
      Long folderId = (Long) folderIDs[i];
      Timestamp startStamp = (Timestamp) (startStamps != null ? startStamps[i] : null);
      resultRequestSetsL = prepareSynchRequest(cache, folderId, startStamp, maxItemsForFetchedFolders, maxItemsForNonFetchedFolders, maxItemsForProbingNewItems, resultRequestSetsL);
    }
    if (trace != null) trace.exit(CacheFldUtils.class, resultRequestSetsL);
    return resultRequestSetsL;
  }

  public static List prepareSynchRequest(FetchedDataCache cache, Long folderId, Timestamp startStamp, int maxItemsForFetchedFolders, int maxItemsForNonFetchedFolders, int maxItemsForProbingNewItems, List resultRequestSetsL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheFldUtils.class, "prepareSynchRequest(FetchedDataCache cache, Long folderId, Timestamp startStamp, int maxItemsForFetchedFolders, int maxItemsForNonFetchedFolders, int maxItemsForProbingNewItems, List resultRequestSetsL)");
    if (trace != null) trace.args(folderId, startStamp);
    if (trace != null) trace.args(maxItemsForFetchedFolders);
    if (trace != null) trace.args(maxItemsForNonFetchedFolders);
    if (trace != null) trace.args(maxItemsForProbingNewItems);
    boolean wasFetched = FolderRecUtil.wasFolderFetchRequestIssued(folderId);
    boolean wasInvalidated = FolderRecUtil.wasFolderViewInvalidated(folderId);
    if (trace != null) trace.data(10, "preparing for folderId="+folderId+", startStamp="+startStamp+", wasFetched="+wasFetched+", wasInvalidated="+wasInvalidated);
    FolderRecord folder = cache.getFolderRecord(folderId);
    FolderShareRecord share = cache.getFolderShareRecordMy(folderId, true);
    if (folder != null && share != null) {
      if (folder.isFileType() || folder.isMsgType()) {
        boolean isProbingForNewItems = startStamp == null && !wasInvalidated;
        boolean isTruncated = false;
        Boolean includeBodies = null;
        Record[] links = null;
        if (folder.isFileType()) {
          links = cache.getFileLinkRecordsOwnerAndType(folderId, new Short(Record.RECORD_TYPE_FOLDER));
        } else if (folder.isMsgType()) {
          includeBodies = Boolean.valueOf(folder != null && 
                  ( (folder.folderType.shortValue() == FolderRecord.POSTING_FOLDER && wasFetched) ||
                    folder.isChatting() // always fetch chatting messages
                ));
          links = cache.getMsgLinkRecordsForFolder(folderId);
          // check for partially fetched msg folder - set isTruncated
          if (MsgAGet.hasNextOrFetching(folderId))
            isTruncated = true;
        }
        Integer[] diffBits = null;
        Timestamp past3days = new Timestamp(System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L));
        if (links != null) {
          if (trace != null) trace.data(20, "sorting links for folder "+folderId);
          // sort links most-recent-first
          Arrays.sort(links, new Comparator() {
            public int compare(Object o1, Object o2) {
              // newest on top
              if (o1 != null && o2 != null && o1 instanceof LinkRecordI && o2 instanceof LinkRecordI)
                return ((LinkRecordI) o2).getCreatedStamp().compareTo(((LinkRecordI) o1).getCreatedStamp());
              else if (o1 == null && o2 == null)
                return 0;
              else if (o1 == null)
                return -1;
              else if (o2 == null)
                return 1;
              else
                return 0;
            }
          });
          // For recent items or continuation requests use the supplied "max" item count,
          // For old items and top-of-list non-continuation requests, use lower value 1/5 of max + 20.
          int maxOldItemsForFetchedFolders = startStamp != null ? maxItemsForFetchedFolders : Math.min(maxItemsForFetchedFolders, maxItemsForFetchedFolders/5+20);
          List linksL = Arrays.asList(links);
          List linksFilteredL = null;
          // truncate list at "maxItemsForFetchedFolders" items or 25 items and 3 days old
          int eligibleCount = 0;
          int firstEligibleIndex = -1;
          for (int k=0; k<linksL.size(); k++) {
            LinkRecordI link = (LinkRecordI) linksL.get(k);
            if (trace != null) trace.data(30, "considering link id", link.getId());
            if (startStamp == null || startStamp.getTime() >= link.getCreatedStamp().getTime()) {
              if (trace != null) trace.data(31, "stamp check passed");
              if (firstEligibleIndex == -1) {
                firstEligibleIndex = k;
                if (trace != null) trace.data(32, "firstEligibleIndex=", firstEligibleIndex);
              }
              if ((eligibleCount >= maxItemsForFetchedFolders && wasFetched) || 
                      (eligibleCount >= maxItemsForNonFetchedFolders && !wasFetched) ||
                      (eligibleCount >= maxOldItemsForFetchedFolders && link.getCreatedStamp().getTime() < past3days.getTime()) ||
                      (eligibleCount >= maxItemsForProbingNewItems && isProbingForNewItems && wasFetched)) {
                if (trace != null) trace.data(33, "link not eligible, truncating at k=", k);
                linksFilteredL = linksL.subList(firstEligibleIndex, k);
                isTruncated = true;
                break;
              }
              // Loop not braked-out so it must be eligible.
              eligibleCount ++;
            }
          }
          // if didn't break-out of the loop with in-eligible link, make filtered list now
          if (linksFilteredL == null && firstEligibleIndex >= 0 && eligibleCount < linksL.size()) {
            linksFilteredL = linksL.subList(firstEligibleIndex, firstEligibleIndex+eligibleCount);
          }
          // if we have a new filtered list, use only those elements
          if (linksFilteredL != null) {
            links = (Record[]) ArrayUtils.toArray(linksFilteredL, Record.class);
          }
          // gather corresponding stat marks
          if (trace != null) trace.data(40, "filter done, items "+links.length+" last element is "+(links != null && links.length > 0 ? links[links.length-1].toString() : "LIST EMPTY"));
          diffBits = new Integer[links.length];
          for (int k=0; k<links.length; k++) {
            LinkRecordI link = (LinkRecordI) links[k];
            StatRecord stat = cache.getStatRecord(link.getId(), link.getCompatibleStatTypeIndex());
            int comboDiffBits = (stat != null ? stat.mark.intValue() : 0) << 16;
            if (link instanceof MsgLinkRecord) {
              MsgLinkRecord mLink = (MsgLinkRecord) link;
              comboDiffBits = comboDiffBits | mLink.status.intValue();
            } else if (link instanceof FileLinkRecord) {
              FileLinkRecord fLink = (FileLinkRecord) link;
              comboDiffBits = comboDiffBits | fLink.status.intValue();
            }
            diffBits[k] = new Integer(comboDiffBits);
          }
          if (trace != null) trace.data(50, "stat marks gathered");
        }

        // For truncated requests (ie: Android style partially fetched msg folders),
        // skip empty continuation requests...
        if (isTruncated && startStamp != null && (links == null || links.length == 0)) {
          // skip
          if (trace != null) trace.data(60, "REQUEST skipped due to truncated empty continuation");
        } else {

          // request structure: <shareId> <includeBodies> <startStamp> <list-of-link-ids>+ <stats-marks>+ <lastStamp> <isTruncated> <fetchNumItems>
          Obj_List_Co objs = new Obj_List_Co();
          objs.objs = new Object[9];
          objs.objs[0] = share != null ? share.shareId : null;
          objs.objs[1] = includeBodies;
          objs.objs[2] = startStamp != null ? startStamp : null; // this is start stamp of our intent, NOT the of our first link! The top of list must be NULL so server will include most recent links.
          objs.objs[3] = RecordUtils.getIDs(links);
          objs.objs[4] = diffBits;
          objs.objs[5] = links != null && links.length > 0 ? ((LinkRecordI) links[links.length-1]).getCreatedStamp() : null; // in case server has links deleted, this will tell him what stamp we have cached
          objs.objs[6] = Boolean.valueOf(isTruncated); // We are sending entire list, or truncating at some point
          objs.objs[7] = Boolean.valueOf(isProbingForNewItems);
          objs.objs[8] = new Integer(wasFetched ? maxItemsForFetchedFolders : maxItemsForNonFetchedFolders);

          if (trace != null) trace.data(70, "REQUEST = "+Misc.objToStr(objs.objs));
          if (resultRequestSetsL == null) resultRequestSetsL = new ArrayList();
          resultRequestSetsL.add(objs);
        }
      } else {
        if (trace != null) trace.data(100, "not a file or message folder, prep for synch skipped");
      }
    }
    if (trace != null) trace.exit(CacheFldUtils.class, "request set list");
    return resultRequestSetsL;
  }

}