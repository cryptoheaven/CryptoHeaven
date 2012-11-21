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

package com.CH_cl.service.actions.sys;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.engine.ServerInterfaceWorker;
import com.CH_cl.service.records.FolderRecUtil;
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
* <b>$Revision: 1.14 $</b>
* @author  Marcin Kurzawa
* @version 
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
//System.out.println("SysANotify:  running in the SysANotify");
    final FetchedDataCache cache = getServerInterfaceLayer().getFetchedDataCache();

    if (cache.getUserRecord() != null && cache.getKeyRecordMyCurrent() != null && cache.getKeyRecordMyCurrent().getPrivateKey() != null) {
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
        boolean isTooRapid = stampNow - reSynchStamp < 15000 || stampNow < reSynchStamp; // include invalid stamps
        if (!isTooRapid) {
          reSynchStamp = System.currentTimeMillis();
//          System.out.println("SysANotify: should re-synch");
          Thread th = new ThreadTraced("ReSynch Runner") {
            public void runTraced() {
              // invalidate views of fetched folders so that client reloads them on demand
              FolderRecord[] folders = cache.getFolderRecords();
              for (int i=0; i<folders.length; i++) {
                Long folderId = folders[i].folderId;
                FolderRecUtil.markFolderViewInvalidated(folderId, true);
              }

//              System.out.println("SysANotify: about to request re-synchs");

              // if connection is still active then request update
              if (SIL.hasPersistentMainWorker()) {
                // send re-synch request for folders - wait for reply as subsequent synchronization may need to run for all folders
                Long folderId = null; // null signifies Folders-Mode
//                System.out.println("SysANotify: prepping for re-synch folders");
                List fldRequestSetsL = CacheFldUtils.prepareSynchRequest(cache, folderId, null, 0, 0, 0, 0, null);
                if (fldRequestSetsL != null && fldRequestSetsL.size() > 0) {
//                  System.out.println("SysANotify: requesting to synch folders");
                  SIL.submitAndWait(new MessageAction(CommandCodes.FLD_Q_SYNC, new Obj_List_Co(fldRequestSetsL)), 90000);
                }
                // send re-synch request for contacts
                Long contactFolderId = cache.getUserRecord().contactFolderId;
                List cntRequestSetsL = CacheFldUtils.prepareSynchRequest(cache, contactFolderId, null, 0, 0, 0, 0, null);
                FolderRecUtil.markFolderViewInvalidated(contactFolderId, false);
                if (cntRequestSetsL != null && cntRequestSetsL.size() > 0) {
//                  System.out.println("SysANotify: requesting to synch contacts");
                  SIL.submitAndWait(new MessageAction(CommandCodes.FLD_Q_SYNC, new Obj_List_Co(cntRequestSetsL)), 90000);
                }

                // get updated new object counts
                FolderShareRecord[] allShares = cache.getFolderSharesMy(true);
                if (allShares != null && allShares.length > 0) {
                  // if connection is still active then request update
                  if (SIL.hasPersistentMainWorker()) {
//                    System.out.println("SysANotify: ******* requesting to get red flag count");
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