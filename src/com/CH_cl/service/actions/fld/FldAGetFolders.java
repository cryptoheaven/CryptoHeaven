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

package com.CH_cl.service.actions.fld;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.CacheUtilities;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class FldAGetFolders extends ClientMessageAction {

  private static SingleTokenArbiter singleRedFlagCountArbiter;
  private static Object singleRedFlagCountArbiterKey;
  private static Hashtable newShareIDsHT;

  /** Creates new FldAGetFolders */
  public FldAGetFolders() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldAGetFolders.class, "FldAGetFolders()");
    if (trace != null) trace.exit(FldAGetFolders.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldAGetFolders.class, "runAction()");

    Fld_Folders_Rp dataSet = (Fld_Folders_Rp) getMsgDataSet();
    FolderRecord[] folderRecords = dataSet.folderRecords;
    FolderShareRecord[] shareRecords = dataSet.shareRecords;
    runAction(getServerInterfaceLayer(), folderRecords, shareRecords);

    if (trace != null) trace.exit(FldAGetFolders.class, null);
    return null;
  }

  public static void runAction(ServerInterfaceLayer SIL, FolderRecord[] folderRecords, FolderShareRecord[] shareRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldAGetFolders.class, "runAction(ServerInterfaceLayer SIL, FolderRecord[] folderRecords, FolderShareRecord[] shareRecords)");
    if (trace != null) trace.args(SIL);
    if (trace != null) trace.args(folderRecords);
    if (trace != null) trace.args(shareRecords);

    FetchedDataCache cache = SIL.getFetchedDataCache();
    FolderRecord[] allFolders = cache.getFolderRecords();
    Long userId = cache.getMyUserId();

    // initialize categories
    if (folderRecords != null && folderRecords.length > 0) {
      if (cache.countFolderRecords() == 0) {
        Vector foldersBufferV = new Vector();
        Vector sharesBufferV = new Vector();
        CacheUtilities.makeFolderCategories(userId, foldersBufferV, sharesBufferV);
        FolderRecord[] foldersBuffer = (FolderRecord[]) ArrayUtils.toArray(foldersBufferV, FolderRecord.class);
        FolderShareRecord[] sharesBuffer = (FolderShareRecord[]) ArrayUtils.toArray(sharesBufferV, FolderShareRecord.class);
        folderRecords = (FolderRecord[]) ArrayUtils.concatinate(foldersBuffer, folderRecords);
        shareRecords = (FolderShareRecord[]) ArrayUtils.concatinate(sharesBuffer, shareRecords);
      }
    }

    // remove old-style local file folder
    if (folderRecords != null && folderRecords.length > 0) {
      FolderRecord oldLocalFolder = new FolderRecord();
      oldLocalFolder.folderId = new Long(0);
      folderRecords = (FolderRecord[]) ArrayUtils.getDifference(folderRecords, new FolderRecord[] { oldLocalFolder });
    }

    if (trace != null) trace.data(10, "Adding folders to cache...");
    if (folderRecords != null && folderRecords.length > 0)
      cache.addFolderRecords(folderRecords);

    // Batch the shares that are asymetrically encrypted so that user can see folders as 
    // they come and not wait an hour for 1000+ folders that might be newly shared.
    if (trace != null) trace.data(11, "Adding shares to cache...");
    if (shareRecords != null && shareRecords.length > 0) {
      Vector sharesV = new Vector();
      int batchSize = 25;
      int batchStart = 0;
      do {
        sharesV.clear();
        int batchInnerCount = 0;
        for (int i=batchStart; batchInnerCount<batchSize && i<shareRecords.length; i++) {
          batchStart ++;
          if (shareRecords[i].ownerUserId.equals(userId) && shareRecords[i].getPubKeyId() != null) {
            batchInnerCount ++;
          }
          sharesV.addElement(shareRecords[i]);
        }
        if (sharesV.size() > 0) {
          FolderShareRecord[] shares = (FolderShareRecord[]) ArrayUtils.toArray(sharesV, FolderShareRecord.class);
          cache.addFolderShareRecords(shares);
        }
      } while (batchStart<shareRecords.length);
    }
    // Add all shares in one GO, -- this may take a while if folders are asymetrically encrypted ...... batching is better
    //if (shareRecords != null && shareRecords.length > 0)
      //cache.addFolderShareRecords(shareRecords);

    if (trace != null) trace.data(20, "Fetch the RED FLAG COUNT");

    // Fetch the RED FLAG COUNT for all the folders that are new to our cache.
    FolderRecord[] newFolders = (FolderRecord[]) ArrayUtils.getDifference(folderRecords, allFolders);
    if (newFolders != null && newFolders.length > 0) {
      FolderShareRecord[] newShares = cache.getFolderSharesMyForFolders(RecordUtils.getIDs(newFolders), true);
      if (newShares != null && newShares.length > 0) {
        doDelayedRedFlagCount(SIL, newShares);
      }
    }

    Vector userIDsV = null;
    Vector shareIDsV = null;
    // Fetch user handles for all shared with me folders and for all share records.
    if (folderRecords != null) {
      for (int i=0; i<folderRecords.length; i++) {
        FolderRecord fRec = folderRecords[i];
        Long ownerUserId = fRec.ownerUserId;
        if (cache.getUserRecord(ownerUserId) == null) {
          if (userIDsV == null) userIDsV = new Vector();
          if (!userIDsV.contains(ownerUserId)) {
            userIDsV.addElement(ownerUserId);
          }
        }

        if (fRec.isChatting()) {
          // only care if we have too little shares, ignore too many because if delete notification
          // failed to arrive then we would have infinite loop
          FolderShareRecord[] fShares = cache.getFolderShareRecordsForFolder(fRec.folderId);
          int fSharesLength = fShares != null ? fShares.length : 0;
          if (fRec.numOfShares.shortValue() > fSharesLength) {
            FolderShareRecord sRec = cache.getFolderShareRecordMy(fRec.folderId, true);
            if (sRec != null) {
              if (shareIDsV == null) shareIDsV = new Vector();
              if (!shareIDsV.contains(sRec.shareId))
                shareIDsV.addElement(sRec.shareId);
            }
          }
        }
      }
    }

    // Fetch folder shares for all chatting folders and fetch handles for share owners so we can display them properly.
    if (shareRecords != null) {

      for (int i=0; i<shareRecords.length; i++) {
        FolderShareRecord sRec = shareRecords[i];

        // Comment out this block because this would create infinite loop when some shares are missing
        // due to user account deletion.
        // Allow this block because when users is deleted, number of shares is decremented...
        // <<<
        FolderRecord fRec = cache.getFolderRecord(sRec.folderId);
        // Don't limit to chatting folders, get shares for all types of folders, so we can get/display their participants
        //if (fRec != null && fRec.isChatting()) {
        if (fRec != null) {
          FolderShareRecord[] fShares = cache.getFolderShareRecordsForFolder(fRec.folderId);
          int fSharesLength = fShares != null ? fShares.length : 0;
          if (fRec.numOfShares.shortValue() > fSharesLength) {
            if (shareIDsV == null) shareIDsV = new Vector();
            if (!shareIDsV.contains(sRec.shareId))
              shareIDsV.addElement(sRec.shareId);
          }
        }
        // >>>
        if (sRec.isOwnedByUser() && cache.getUserRecord(sRec.ownerUserId) == null) {
          if (userIDsV == null) userIDsV = new Vector();
          if (!userIDsV.contains(sRec.ownerUserId)) {
            userIDsV.addElement(sRec.ownerUserId);
          }
        }
      }
    }

    if (shareIDsV != null && shareIDsV.size() > 0) {
      Long[] shareIDs = (Long[]) ArrayUtils.toArray(shareIDsV, Long.class);
      SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_GET_FOLDER_SHARES, new Obj_IDList_Co(shareIDs)));
      // also request an update to related folders to avoid potential loops if out of synch data between shares and folders
      Long[] folderIDs = FolderShareRecord.getFolderIDs(cache.getFolderShareRecords(shareIDs));
      SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_SOME, new Obj_IDList_Co(folderIDs)));
    }

    if (userIDsV != null && userIDsV.size() > 0) {
      Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsV, Long.class);
      SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userIDs)));
    }

    // Check if there are any folder shares that need recrypting to symmetric key.
    // Recrypt my shares which are new and asymmetricaly encrypted
    Vector recryptedSharesV = null;
    if (shareRecords != null) {
      int batchSize = 50;
      int batchStart = 0;
      do {
        if (recryptedSharesV != null) recryptedSharesV.clear();
        int batchInnerCount = 0;
        for (int i=batchStart; batchInnerCount<batchSize && i<shareRecords.length; i++) {
          batchStart ++;
          batchInnerCount ++;
          FolderShareRecord sRec = shareRecords[i];
          if (sRec.ownerUserId.equals(userId) && sRec.getPubKeyId() != null) {
            // Seal with user's symmetric key, this will also nullify the pubKeyId for the share.
            sRec.seal(cache.getUserRecord().getSymKeyFldShares());
            if (recryptedSharesV == null) recryptedSharesV = new Vector();
            recryptedSharesV.addElement(sRec);
          }
        }
        if (recryptedSharesV != null && recryptedSharesV.size() > 0) {
          FolderShareRecord[] recryptedShares = (FolderShareRecord[]) ArrayUtils.toArray(recryptedSharesV, FolderShareRecord.class);
          SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_TO_SYM_ENC, new Fld_ToSymEnc_Rq(recryptedShares)));
        }
      } while (batchStart<shareRecords.length);
    }

    // Check if we have a Spam (Junk e-mail) folder, if not, create it
    FolderOps.getOrCreateJunkFolder(SIL);
    // Check if we have a Recycle Bin folder, if not, create it
    if (GlobalProperties.PROGRAM_BUILD_NUMBER >= 358)
      FolderOps.getOrCreateRecycleFolder(SIL);

    if (trace != null) trace.exit(FldAGetFolders.class, null);
  }

  private static void doDelayedRedFlagCount(final ServerInterfaceLayer SIL, FolderShareRecord[] newShares) {
    if (singleRedFlagCountArbiter == null) singleRedFlagCountArbiter = new SingleTokenArbiter();
    if (singleRedFlagCountArbiterKey == null) singleRedFlagCountArbiterKey = new Object();
    if (newShareIDsHT == null) newShareIDsHT = new Hashtable();

    final Object arbiterKey = singleRedFlagCountArbiterKey; // common static key for all threads
    final Object arbiterToken = new Object();
    Long[] toProcessShareIDs = null;

    synchronized (newShareIDsHT) {
      if (newShares != null && newShares.length > 0) {
        Long[] newShareIDs = RecordUtils.getIDs(newShares);
        for (int i=0; i<newShareIDs.length; i++) {
          newShareIDsHT.put(newShareIDs[i], newShareIDs[i]);
        }
      }
      if (newShareIDsHT.size() > 0) {
        if (singleRedFlagCountArbiter.putToken(arbiterKey, arbiterToken)) { // token will be removed when job finishes
          Set keys = newShareIDsHT.keySet();
          toProcessShareIDs = new Long[keys.size()];
          toProcessShareIDs = (Long[]) keys.toArray(toProcessShareIDs);
          newShareIDsHT.clear();
        }
      }
    }

    if (toProcessShareIDs != null && toProcessShareIDs.length > 0) {
      Runnable afterJob = new Runnable() {
        public void run() {
          singleRedFlagCountArbiter.removeToken(arbiterKey, arbiterToken);
          doDelayedRedFlagCount(SIL, null);
        }
      };
      Runnable timeoutJob = new Runnable() {
        public void run() {
          singleRedFlagCountArbiter.removeToken(arbiterKey, arbiterToken);
          doDelayedRedFlagCount(SIL, null);
        }
      };
      SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_RED_FLAG_COUNT, new Obj_IDList_Co(toProcessShareIDs)), 30000, afterJob, timeoutJob);
    }
  } // end doDelayedRedFlagCount()

}