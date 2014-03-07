/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.fld;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.util.ArrayList;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.3 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FldAGetChildren extends ClientMessageAction {

  /** Creates new FldAGetChildren */
  public FldAGetChildren() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldAGetChildren.class, "FldAGetChildren()");
    if (trace != null) trace.exit(FldAGetChildren.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldAGetChildren.class, "runAction(Connection)");

    Fld_Folders_Rp dataSet = (Fld_Folders_Rp) getMsgDataSet();
    FolderRecord[] folderRecords = dataSet.folderRecords;
    FolderShareRecord[] shareRecords = dataSet.shareRecords;

    final ServerInterfaceLayer SIL = getServerInterfaceLayer();
    FetchedDataCache cache = SIL.getFetchedDataCache();

    if (folderRecords != null && folderRecords.length > 0) {
      // gather all child folders not in the cache...
      FolderRecord[] foldersAlreadyCashed = cache.getFolderRecords(RecordUtils.getIDs(folderRecords));
      FolderRecord[] foldersNotCashed = folderRecords;
      if (foldersAlreadyCashed != null && foldersAlreadyCashed.length > 0)
        foldersNotCashed = (FolderRecord[]) ArrayUtils.getDifference(folderRecords, foldersAlreadyCashed);
      FldAGetFolders.runAction(SIL, folderRecords, shareRecords);
      if (foldersNotCashed != null && foldersNotCashed.length > 0) {
        ArrayList foldersNotCashedWithChildrenL = new ArrayList();
        for (int i=0; i < foldersNotCashed.length; i++) {
          if (foldersNotCashed[i].numOfViewChildren == null || foldersNotCashed[i].numOfViewChildren.intValue() > 0) // unknown or > 0
            foldersNotCashedWithChildrenL.add(foldersNotCashed[i]);
        }
        if (foldersNotCashedWithChildrenL.size() > 0) {
          Long[] folderIDs = RecordUtils.getIDs(foldersNotCashedWithChildrenL);
          final Long[][] folderIDsChunks = RecordUtils.divideIntoChunks(folderIDs, 100);
          final int[] index = new int[] { 0 };
          final Runnable[] nextJob = new Runnable[1];
          nextJob[0] = new Runnable() {
            public void run() {
              index[0] ++;
              if (index[0] < folderIDsChunks.length)
                SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_CHILDREN, new Obj_IDList_Co(folderIDsChunks[index[0]])), 30000, nextJob[0], nextJob[0]);
            }
          };
          SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_CHILDREN, new Obj_IDList_Co(folderIDsChunks[index[0]])), 30000, nextJob[0], nextJob[0]);
        }
      }
    }

    if (trace != null) trace.exit(FldAGetChildren.class, null);
    return null;
  }

}