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


  public static List prepareSynchRequest(FetchedDataCache cache, Collection folderIDsL, Collection startStampsL, Collection wasFetchedL, Collection wasInvalidatedL, List resultRequestSetsL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheFldUtils.class, "prepareSynchRequest(FetchedDataCache cache, Collection folderIDsL, Collection startStampsL, Collection wasFetchedL, Collection wasInvalidatedL, List resultRequestSetsL)");
    if (trace != null) trace.args(folderIDsL, startStampsL, wasFetchedL, wasInvalidatedL);
    // smaller-and-faster batch for initial re-synch, larger for continuation requests
    int initialLow = startStampsL == null ? 20 : 200;
    int initialHigh = startStampsL == null ? 100 : 1000;
    // Distribute item count per folder depending of number for folders to re-synch.
    int maxItemsForFetchedFolders = Math.max(initialLow, (initialHigh/(folderIDsL.size()+1))*2);
    // When cache has no items for folder then fetch a few more initially, subsequent fetches are smaller probes.
    int maxItemsForNonFetchedFoldersInitial = 10;
    int maxItemsForNonFetchedFoldersNext = 3;
    int maxItemsForProbingNewItems = 1;
    Object[] folderIDs = folderIDsL.toArray();
    Object[] startStamps = startStampsL != null && startStampsL.size() > 0 ? startStampsL.toArray() : null;
    Object[] wasFetcheds = wasFetchedL != null && wasFetchedL.size() > 0 ? wasFetchedL.toArray() : null;
    Object[] wasInvalidateds = wasInvalidatedL != null && wasInvalidatedL.size() > 0 ? wasInvalidatedL.toArray() : null;
    for (int i=0; i<folderIDs.length; i++) {
      Long folderId = (Long) folderIDs[i];
      Timestamp startStamp = (Timestamp) (startStamps != null && startStamps.length > i ? startStamps[i] : null);
      Boolean wasFetched = (Boolean) (wasFetcheds != null && wasFetcheds.length > i ? wasFetcheds[i] : null);
      Boolean wasInvalidated = (Boolean) (wasInvalidateds != null && wasInvalidateds.length > i ? wasInvalidateds[i] : null);
      resultRequestSetsL = prepareSynchRequest(cache, folderId, startStamp, maxItemsForFetchedFolders, maxItemsForNonFetchedFoldersInitial, maxItemsForNonFetchedFoldersNext, maxItemsForProbingNewItems, wasFetched, wasInvalidated, resultRequestSetsL);
    }
    if (trace != null) trace.exit(CacheFldUtils.class, resultRequestSetsL);
    return resultRequestSetsL;
  }

  public static List prepareSynchRequest(FetchedDataCache cache, Long folderId, Timestamp startStamp, int maxItemsForFetchedFolders, int maxItemsForNonFetchedFoldersInitial, int maxItemsForNonFetchedFoldersNext, int maxItemsForProbingNewItems, Boolean wasFetched, Boolean wasInvalidated, List resultRequestSetsL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheFldUtils.class, "prepareSynchRequest(FetchedDataCache cache, Long folderId, Timestamp startStamp, int maxItemsForFetchedFolders, int maxItemsForNonFetchedFoldersInitial, int maxItemsForNonFetchedFoldersNext, int maxItemsForProbingNewItems, Boolean wasFetched, Boolean wasInvalidated, List resultRequestSetsL)");
    if (trace != null) trace.args(folderId, startStamp);
    if (trace != null) trace.args(maxItemsForFetchedFolders);
    if (trace != null) trace.args(maxItemsForNonFetchedFoldersInitial);
    if (trace != null) trace.args(maxItemsForNonFetchedFoldersNext);
    if (trace != null) trace.args(maxItemsForProbingNewItems);
    if (trace != null) trace.args(wasFetched);
    if (trace != null) trace.args(wasInvalidated);
    if (wasFetched == null)
      wasFetched = folderId != null ? Boolean.valueOf(cache.wasFolderFetchRequestIssued(folderId)) : null;
    if (wasInvalidated == null)
      wasInvalidated = folderId != null ? Boolean.valueOf(cache.wasFolderViewInvalidated(folderId)) : null;
    if (trace != null) trace.data(10, "preparing for folderId="+folderId+", startStamp="+startStamp+", wasFetched="+wasFetched+", wasInvalidated="+wasInvalidated);
    FolderRecord folder = folderId != null ? cache.getFolderRecord(folderId) : null;
    FolderShareRecord share = folderId != null ? cache.getFolderShareRecordMy(folderId, true) : null;
    if (folderId == null || (folder != null && share != null)) {
      UserRecord uRec = cache.getUserRecord();
      if (folderId == null || folder.isFileType() || folder.isMsgType() || (folder.isContactType() && uRec.contactFolderId.equals(folderId))) {
        boolean isProbingForNewItems = startStamp == null && folder != null && (folder.isFileType() || folder.isMsgType()) && wasInvalidated != null && !wasInvalidated.booleanValue();
        boolean isTruncated = false;
        Boolean includeBodies = null;
        Record[] links = null;
        if (folderId == null) {
          ArrayList allMyRemoteSharesL = new ArrayList();
          Long userId = cache.getMyUserId();
          Set groupIDsSet = cache.getFolderGroupIDsMySet();
          FolderShareRecord[] allShares = cache.getFolderShareRecords();
          // filter out only my shares and remote type, include all share paths to each folder (individual and group access paths).. skip categories, etc.
          if (allShares != null) {
            for (int i=0; i<allShares.length; i++) {
              FolderShareRecord sRec = allShares[i];
              FolderRecord fRec = cache.getFolderRecord(sRec.folderId);
              if (fRec != null && !fRec.isCategoryType() && !fRec.isLocalFileType()) {
                if (sRec.isOwnedBy(userId, groupIDsSet))
                  allMyRemoteSharesL.add(sRec);
              }
            }
          }
          links = (Record[]) ArrayUtils.toArray(allMyRemoteSharesL, Record.class);
        } else if (folder.isContactType() && uRec.contactFolderId.equals(folderId)) {
          links = cache.getContactRecordsForUsers(new Long[] {uRec.userId});
        } else if (folder.isFileType()) {
          links = cache.getFileLinkRecordsOwnerAndType(folderId, new Short(Record.RECORD_TYPE_FOLDER));
        } else if (folder.isMsgType()) {
          includeBodies = Boolean.valueOf(folder != null && 
                  ( (folder.folderType.shortValue() == FolderRecord.POSTING_FOLDER && wasFetched.booleanValue()) ||
                    folder.isChatting() // always fetch chatting messages
                ));
          links = cache.getMsgLinkRecordsForFolder(folderId);
          // check for partially fetched msg folder - set isTruncated
          if (cache.hasNextFetchRequestOrFetching(folderId))
            isTruncated = true;
        }
        Integer[] diffBits = null;
        if (links != null) {
          if (trace != null) trace.data(20, "sorting links for folder "+folderId);
          // Sorting/Filtering is skipped for ContactRecords and FoldersShareRecords
          if (folder != null && (folder.isMsgType() || folder.isFileType())) {
            // sort links most-recent-first -- skip Contact and FolderShares sorting, we'll always fetch all Contacts and FolderShares
            if (links.length > 1) {
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
            }
            // For recent items or continuation requests use the supplied "max" item count,
            // Start with empty list, if nothing qualifies through our Time Stamp filter then we'll submit empty list.
            List linksFilteredL = new ArrayList();
            // truncate list at "maxItemsForFetchedFolders" items or 25 items and 3 days old
            int count = 0;
            for (int k=0; k<links.length; k++) {
              LinkRecordI link = (LinkRecordI) links[k];
              if (trace != null) trace.data(30, "considering link id", link.getId());
              // If not filtering based on Stamp, or if already entered stamp-eligible zone in sorted array, or StartStamp is newer than compared link's stamp.
              if (startStamp == null || linksFilteredL.size() > 0 || startStamp.compareTo(link.getCreatedStamp()) >= 0) {
                if (trace != null) trace.data(31, "stamp check passed");
                if ((count >= maxItemsForFetchedFolders && wasFetched.booleanValue()) || 
                        (count >= maxItemsForNonFetchedFoldersNext && !wasFetched.booleanValue()) ||
                        (count >= maxItemsForProbingNewItems && isProbingForNewItems && wasFetched.booleanValue())) {
                  if (trace != null) trace.data(33, "link not eligible, truncating at k=", k);
                  isTruncated = true;
                  break;
                }
                // Loop not braked-out so it must be eligible.
                linksFilteredL.add(link);
                count ++;
              }
            }
            // Copy back the filtered results, if none of the links qualified, then we'll have EMPTY array links which is ok.
            links = (Record[]) ArrayUtils.toArray(linksFilteredL, Record.class);
            if (trace != null) trace.data(40, "filter done, items "+links.length+" first element is "+(links != null && links.length > 0 ? links[0].toString() : " EMPTY ")+", last element is "+(links != null && links.length > 0 ? links[links.length-1].toString() : "LIST EMPTY"));
          }
          // gather corresponding stat marks
          diffBits = new Integer[links.length];
          for (int k=0; k<links.length; k++) {
            int comboDiffBits = 0;
            Record rec = links[k];
            if (rec instanceof ContactRecord) {
              ContactRecord cRec = (ContactRecord) rec;
              comboDiffBits = (cRec.permits.intValue() << 16) | cRec.status.intValue();
            } else {
              LinkRecordI link = (LinkRecordI) rec;
              StatRecord stat = cache.getStatRecord(link.getId(), link.getCompatibleStatTypeIndex());
              comboDiffBits = (stat != null ? stat.mark.intValue() : 0) << 16;
              if (link instanceof FolderShareRecord) {
                FolderShareRecord sLink = (FolderShareRecord) link;
                // compact to 3 bits per permission -- skip the StatRecord bits for diffing
                comboDiffBits = 0;
                comboDiffBits |= (sLink.canWrite.byteValue() & 0x0007);
                comboDiffBits |= (sLink.canDelete.byteValue() & 0x0007) << 3;
              } else if (link instanceof MsgLinkRecord) {
                MsgLinkRecord mLink = (MsgLinkRecord) link;
                comboDiffBits |= mLink.status.intValue();
              } else if (link instanceof FileLinkRecord) {
                FileLinkRecord fLink = (FileLinkRecord) link;
                comboDiffBits |= fLink.status.intValue();
              }
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

          boolean isFileMode = folder != null && folder.isMsgType();
          boolean isMsgMode = folder != null && folder.isFileType();

          // request structure: <shareId> <includeBodies> <startStamp> <list-of-link-ids>+ <stats-marks>+ <lastStamp> <isTruncated> <isProbingForNewItems> <wasFetched> <wasInvalidated> <fetchNumItems>
          Obj_List_Co objs = new Obj_List_Co();
          objs.objs = new Object[11];
          objs.objs[0] = share != null ? share.shareId : null;
          if (isFileMode || isMsgMode) {
            objs.objs[1] = includeBodies;
            objs.objs[2] = startStamp != null ? startStamp : null; // this is start stamp of our intent, NOT the of our first link! The top of list must be NULL so server will include most recent links.
          }
          objs.objs[3] = RecordUtils.getIDs(links);
          objs.objs[4] = diffBits;
          if (isFileMode || isMsgMode) {
            LinkRecordI lastLink = (LinkRecordI) (links != null && links.length > 0 && links[links.length-1] instanceof LinkRecordI ? links[links.length-1] : null);
            objs.objs[5] = lastLink != null ? lastLink.getCreatedStamp() : null; // in case server has links deleted, this will tell him what stamp we have cached
            objs.objs[6] = Boolean.valueOf(isTruncated); // We are sending entire list, or truncating at some point
            objs.objs[7] = Boolean.valueOf(isProbingForNewItems);
            objs.objs[8] = wasFetched;
            objs.objs[9] = wasInvalidated;
            objs.objs[10] = new Integer(wasFetched.booleanValue() ? maxItemsForFetchedFolders : (lastLink == null ? maxItemsForNonFetchedFoldersInitial : maxItemsForNonFetchedFoldersNext));
          }

          if (trace != null) trace.data(70, "REQUEST = "+Misc.objToStr(objs.objs));
          if (resultRequestSetsL == null) resultRequestSetsL = new ArrayList();
          resultRequestSetsL.add(objs);
        }
      } else {
        if (trace != null) trace.data(100, "not a file or message folder, not a contact folder, not a folder-re-synch, >> prep for synch skipped");
      }
    }
    if (trace != null) trace.exit(CacheFldUtils.class, "request set list");
    return resultRequestSetsL;
  }

}