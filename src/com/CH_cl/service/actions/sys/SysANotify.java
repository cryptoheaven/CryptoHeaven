/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.sys;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.engine.ServerInterfaceWorker;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.records.RecordUtils;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import java.util.List;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.14 $</b>
*
* @author  Marcin Kurzawa
*/
public class SysANotify extends ClientMessageAction {

  private static long reSynchStamp;

  /** Creates new SysANotify */
  public SysANotify() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysANotify.class, "SysANotify()");
    if (trace != null) trace.exit(SysANotify.class);
  }

  /** 
  * The action handler performs all actions related to the received message (reply),
  * and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysANotify.class, "runAction(Connection)");

    final FetchedDataCache cache = getServerInterfaceLayer().getFetchedDataCache();

    if (cache != null && cache.getUserRecord() != null && cache.getKeyRecordMyCurrent() != null && cache.getKeyRecordMyCurrent().getPrivateKey() != null) {
      final ServerInterfaceLayer SIL = getServerInterfaceLayer();

      long stampNow = System.currentTimeMillis();

      // 30 seconds disconnection or double ping-pong inactivity then request reconnection update
      boolean shouldReSynch = SIL.lastWorkerActivityResyncPending;
      if (!shouldReSynch && SIL.lastForcedWorkerStamp != null) {
        long stampLast = SIL.lastForcedWorkerStamp.getTime();
        boolean invalidStamp = stampLast > stampNow;
        shouldReSynch = stampNow - stampLast >= ServerInterfaceWorker.TIMEOUT_TO_TRIGGER_RECONNECT_UPDATE || invalidStamp;
      }
      // After we determined if we'll attempt resynch, wipe out last stamp to prevent
      // false reconnects in a short future
      SIL.lastForcedWorkerStamp = null;
      SIL.lastWorkerActivityResyncPending = false;

      // Now do the re-synch if needed.
      if (shouldReSynch) {
        // Skip too rapid repeted requests (less than 15 seconds apart)
        boolean isTooRapid = stampNow - reSynchStamp < 15000;
        // If not too often, or when invalid stamp
        if (!isTooRapid || stampNow < reSynchStamp) {
          reSynchStamp = System.currentTimeMillis();
          Thread th = new ThreadTraced("ReSynch Runner") {
            public void runTraced() {
              // invalidate views of fetched folders so that client reloads them on demand
              FolderRecord[] folders = cache.getFolderRecords();
              if (folders != null) {
                for (int i=0; i<folders.length; i++) {
                  Long folderId = folders[i].folderId;
                  if (cache.wasFolderFetchRequestIssued(folderId))
                    cache.markFolderViewInvalidated(folderId, true);
                }
              }

              boolean replyOk = true;
              // if any connection is still active then request updates
              if (replyOk && SIL.hasMainWorker()) {
                // send re-synch request for folders - wait for reply as subsequent synchronization may need to run for all folders
                Long folderId = null; // null signifies Folders-Mode
                List fldRequestSetsL = CacheFldUtils.prepareSynchRequest(cache, folderId, null, 0, 0, 0, 0, null, null, null);
                if (fldRequestSetsL != null && fldRequestSetsL.size() > 0) {
                  replyOk = SIL.submitAndWait(new MessageAction(CommandCodes.FLD_Q_SYNC_FOLDER_TREE, new Obj_List_Co(fldRequestSetsL)), 90000);
                }
              }

              // if any connection is still active then request updates
              if (replyOk && SIL.hasMainWorker()) {
                // send re-synch request for contacts
                Long contactFolderId = cache.getUserRecord().contactFolderId;
                List cntRequestSetsL = CacheFldUtils.prepareSynchRequest(cache, contactFolderId, null, 0, 0, 0, 0, null, null, null);
                cache.markFolderViewInvalidated(contactFolderId, false);
                if (cntRequestSetsL != null && cntRequestSetsL.size() > 0) {
                  replyOk = SIL.submitAndWait(new MessageAction(CommandCodes.FLD_Q_SYNC_CONTACTS, new Obj_List_Co(cntRequestSetsL)), 90000);
                }
              }

              // if any connection is still active then request updates
              if (replyOk && SIL.hasMainWorker()) {
                // get updated new object counts
                FolderShareRecord[] allShares = cache.getFolderSharesMy(true);
                if (allShares != null && allShares.length > 0) {
                  // if connection is still active then request update
                  if (SIL.hasPersistentMainWorker()) {
                    SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_RED_FLAG_COUNT, new Obj_IDList_Co(RecordUtils.getIDs(allShares))));
                  }
                }
              }
            }
          };
          th.setDaemon(true);
          th.start();
        } // end shouldReSynch
      }
    }

    if (trace != null) trace.exit(SysANotify.class, null);
    return null;
  }

}