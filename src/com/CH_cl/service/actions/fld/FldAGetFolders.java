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

package com.CH_cl.service.actions.fld;

import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;

import java.util.ArrayList;
import java.util.HashSet;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class FldAGetFolders extends ClientMessageAction {

  private static final class SingletonHolder {
    private static final SingleTokenArbiter arbiter = new SingleTokenArbiter();
    private static final Object singleRedFlagCountArbiterKey = new Object();
    private static final Object singleDftFolderCreatorArbiterKey = new Object();
    private static final HashSet newShareIDsHS = new HashSet();
  }

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
        ArrayList foldersBufferL = new ArrayList();
        ArrayList sharesBufferL = new ArrayList();
        CacheUtilities.makeFolderCategories(userId, foldersBufferL, sharesBufferL);
        FolderRecord[] foldersBuffer = (FolderRecord[]) ArrayUtils.toArray(foldersBufferL, FolderRecord.class);
        FolderShareRecord[] sharesBuffer = (FolderShareRecord[]) ArrayUtils.toArray(sharesBufferL, FolderShareRecord.class);
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
      ArrayList sharesL = new ArrayList();
      int batchSize = 25;
      int batchStart = 0;
      do {
        sharesL.clear();
        int batchInnerCount = 0;
        for (int i=batchStart; batchInnerCount<batchSize && i<shareRecords.length; i++) {
          batchStart ++;
          if (shareRecords[i].ownerUserId.equals(userId) && shareRecords[i].getPubKeyId() != null) {
            batchInnerCount ++;
          }
          sharesL.add(shareRecords[i]);
        }
        if (sharesL.size() > 0) {
          FolderShareRecord[] shares = (FolderShareRecord[]) ArrayUtils.toArray(sharesL, FolderShareRecord.class);
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

    HashSet userIDsHS = null;
    HashSet shareIDsHS = null;
    // Fetch user handles for all shared with me folders and for all share records.
    if (folderRecords != null) {
      for (int i=0; i<folderRecords.length; i++) {
        FolderRecord fRec = folderRecords[i];
        Long ownerUserId = fRec.ownerUserId;
        if (cache.getUserRecord(ownerUserId) == null) {
          if (userIDsHS == null) userIDsHS = new HashSet();
          userIDsHS.add(ownerUserId);
        }

        if (fRec.isChatting()) {
          // only care if we have too little shares, ignore too many because if delete notification
          // failed to arrive then we would have infinite loop
          FolderShareRecord[] fShares = cache.getFolderShareRecordsForFolder(fRec.folderId);
          int fSharesLength = fShares != null ? fShares.length : 0;
          if (fRec.numOfShares.shortValue() > fSharesLength) {
            FolderShareRecord sRec = cache.getFolderShareRecordMy(fRec.folderId, true);
            if (sRec != null) {
              if (shareIDsHS == null) shareIDsHS = new HashSet();
              shareIDsHS.add(sRec.shareId);
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
            if (shareIDsHS == null) shareIDsHS = new HashSet();
            shareIDsHS.add(sRec.shareId);
          }
        }
        // >>>
        if (sRec.isOwnedByUser() && cache.getUserRecord(sRec.ownerUserId) == null) {
          if (userIDsHS == null) userIDsHS = new HashSet();
          userIDsHS.add(sRec.ownerUserId);
        }
      }
    }

    if (shareIDsHS != null && shareIDsHS.size() > 0) {
      Long[] shareIDs = (Long[]) ArrayUtils.toArray(shareIDsHS, Long.class);
      SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_GET_FOLDER_SHARES, new Obj_IDList_Co(shareIDs)), 30000, 3);
      // also request an update to related folders to avoid potential loops if out of synch data between shares and folders
      Long[] folderIDs = FolderShareRecord.getFolderIDs(cache.getFolderShareRecords(shareIDs));
      SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_SOME, new Obj_IDList_Co(folderIDs)), 30000, 3);
    }

    if (userIDsHS != null && userIDsHS.size() > 0) {
      Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsHS, Long.class);
      SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userIDs)), 30000, 3);
    }

    // Check if there are any folder shares that need recrypting to symmetric key.
    // Recrypt my shares which are new and asymmetricaly encrypted
    ArrayList recryptedSharesL = null;
    if (shareRecords != null) {
      int batchSize = 50;
      int batchStart = 0;
      do {
        if (recryptedSharesL != null) recryptedSharesL.clear();
        int batchInnerCount = 0;
        for (int i=batchStart; batchInnerCount<batchSize && i<shareRecords.length; i++) {
          batchStart ++;
          batchInnerCount ++;
          FolderShareRecord sRec = shareRecords[i];
          if (sRec.ownerUserId.equals(userId) && sRec.getPubKeyId() != null) {
            // Seal with user's symmetric key, this will also nullify the pubKeyId for the share.
            sRec.seal(cache.getUserRecord().getSymKeyFldShares());
            if (recryptedSharesL == null) recryptedSharesL = new ArrayList();
            recryptedSharesL.add(sRec);
          }
        }
        if (recryptedSharesL != null && recryptedSharesL.size() > 0) {
          FolderShareRecord[] recryptedShares = (FolderShareRecord[]) ArrayUtils.toArray(recryptedSharesL, FolderShareRecord.class);
          SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_TO_SYM_ENC, new Fld_ToSymEnc_Rq(recryptedShares)));
        }
      } while (batchStart<shareRecords.length);
    }

    doDelayedDftFolderCreation(SIL);

    if (trace != null) trace.exit(FldAGetFolders.class, null);
  }

  private static void doDelayedDftFolderCreation(final ServerInterfaceLayer SIL) {
    // Lazily create Junk and Recycle folders if they don't exist
    final SingleTokenArbiter singleTokenArbiter = SingletonHolder.arbiter;
    final Object singleDftFolderCreatorArbiterKey = SingletonHolder.singleDftFolderCreatorArbiterKey;
    final Object token = new Object();
    if (singleTokenArbiter.putToken(singleDftFolderCreatorArbiterKey, token)) {
      Thread th = new ThreadTraced("Junk and Recycle Folder checker-creator") {
        public void runTraced() {
          try {
            // delay a little to let other requests through before this one
            try {
              Thread.sleep(3000);
            } catch(Throwable t) {
            }
            // Check if we have a Spam (Junk email) folder, if not, create it
            FolderOps.getOrCreateJunkFolder(SIL);
            // Check if we have a Recycle Bin folder, if not, create it
            if (GlobalProperties.PROGRAM_BUILD_NUMBER >= 358) {
              FolderOps.getOrCreateRecycleFolder(SIL);
            }
          } catch (Throwable t) {
            throw new RuntimeException(t);
          } finally {
            singleTokenArbiter.removeToken(singleDftFolderCreatorArbiterKey, token);
          }
        }
      };
      th.setDaemon(true);
      th.start();
    }
  }

  private static void doDelayedRedFlagCount(final ServerInterfaceLayer SIL, FolderShareRecord[] newShares) {
    HashSet newShareIDsHS = SingletonHolder.newShareIDsHS;
    final SingleTokenArbiter singleTokenArbiter = SingletonHolder.arbiter;
    final Object arbiterKey = SingletonHolder.singleRedFlagCountArbiterKey; // common static key for all threads
    final Object arbiterToken = new Object();
    Long[] toProcessShareIDs = null;

    synchronized (newShareIDsHS) {
      if (newShares != null && newShares.length > 0) {
        Long[] newShareIDs = RecordUtils.getIDs(newShares);
        for (int i=0; i<newShareIDs.length; i++) {
          newShareIDsHS.add(newShareIDs[i]);
        }
      }
      if (newShareIDsHS.size() > 0) {
        if (singleTokenArbiter.putToken(arbiterKey, arbiterToken)) { // token will be removed when job finishes
          toProcessShareIDs = new Long[newShareIDsHS.size()];
          toProcessShareIDs = (Long[]) newShareIDsHS.toArray(toProcessShareIDs);
          newShareIDsHS.clear();
        }
      }
    }

    if (toProcessShareIDs != null && toProcessShareIDs.length > 0) {
      Runnable afterJob = new Runnable() {
        public void run() {
          singleTokenArbiter.removeToken(arbiterKey, arbiterToken);
          doDelayedRedFlagCount(SIL, null);
        }
      };
      Runnable timeoutJob = new Runnable() {
        public void run() {
          singleTokenArbiter.removeToken(arbiterKey, arbiterToken);
          doDelayedRedFlagCount(SIL, null);
        }
      };
      SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_RED_FLAG_COUNT, new Obj_IDList_Co(toProcessShareIDs)), 30000, 3, null, afterJob, timeoutJob);
    }
  } // end doDelayedRedFlagCount()

}