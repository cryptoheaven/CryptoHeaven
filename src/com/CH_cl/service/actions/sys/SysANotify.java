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
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.engine.ServerInterfaceWorker;
import com.CH_cl.service.records.FolderRecUtil;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.records.RecordUtils;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;

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

    if (cache.getUserRecord() != null && cache.getKeyRecordMyCurrent() != null && cache.getKeyRecordMyCurrent().getPrivateKey() != null) {
      final ServerInterfaceLayer SIL = getServerInterfaceLayer();

      // 30 seconds disconnection or double ping-pong inactivity then request reconnection update
      boolean shouldReSynch = SIL.lastWorkerActivityResyncPending;
      if (!shouldReSynch && SIL.lastForcedWorkerStamp != null) {
        long stampNow = System.currentTimeMillis();
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
        Thread th = new ThreadTraced("ReSynch Runner") {
          public void runTraced() {
            // invalidate views of fetched folders so that client reloads them on demand
            FolderRecord[] folders = cache.getFolderRecords();
            for (int i=0; i<folders.length; i++) {
              Long folderId = folders[i].folderId;
              FolderRecUtil.markFolderViewInvalidated(folderId, true);
            }

            // if connection is still active then request update
            if (SIL.hasPersistantMainWorker()) {
              getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_RECONNECT_UPDATE), 60000);
              // get updated new object counts
              FolderShareRecord[] allShares = cache.getFolderSharesMy(true);
              if (allShares != null && allShares.length > 0) {
                // if connection is still active then request update
                if (SIL.hasPersistantMainWorker()) {
                  getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.FLD_Q_RED_FLAG_COUNT, new Obj_IDList_Co(RecordUtils.getIDs(allShares))));
                }
              }
            }
          }
        };
        th.setDaemon(true);
        th.start();
      } // end shouldReSynch
    }

    if (trace != null) trace.exit(SysANotify.class, null);
    return null;
  }

}