/*
* Copyright 2001-2013 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_cl.service.actions.fld;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.FolderOps;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDs_Co;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import java.util.ArrayList;
import java.util.List;

/** 
* <b>Copyright</b> &copy; 2001-2013
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
* <b>$Revision: 1.8 $</b>
* @author  Marcin Kurzawa
* @version 
*/
public class FldARedFlagCount extends ClientMessageAction {

  /** Creates new FldARedFlagCount */
  public FldARedFlagCount() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldARedFlagCount.class, "FldARedFlagCount()");
    if (trace != null) trace.exit(FldARedFlagCount.class);
  }

  /** 
  * The action handler performs all actions related to the received message (reply),
  * and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldARedFlagCount.class, "runAction(Connection)");

    Obj_IDs_Co reply = (Obj_IDs_Co) getMsgDataSet();
    Long[][] ids = reply.IDs;

    FetchedDataCache cache = getFetchedDataCache();
    ArrayList updatedFoldersL = new ArrayList();
    List fldIDsWithRedFlagL = new ArrayList();

    for (int i=0; i<ids[0].length; i++) {
      Long folderId = ids[0][i];
      Long redFlagCount = ids[1][i];

      FolderRecord folderRecord = cache.getFolderRecord(folderId);
      if (folderRecord != null) {
        boolean suppressSound = folderRecord.folderId.equals(cache.getUserRecord().junkFolderId) || folderRecord.folderId.equals(cache.getUserRecord().recycleFolderId);
        folderRecord.setUpdated(redFlagCount.intValue(), suppressSound);
        updatedFoldersL.add(folderRecord);
      }
      if (redFlagCount != null && redFlagCount.longValue() > 0) {
        fldIDsWithRedFlagL.add(folderId);
      }
    }

    if (updatedFoldersL.size() > 0) {
      FolderRecord[] updatedFolders = (FolderRecord[]) ArrayUtils.toArray(updatedFoldersL, FolderRecord.class);
      // Cause folder listeners to do visual update.
      cache.addFolderRecords(updatedFolders);
    }

    // re-synch only those that were invalidated or never fetched and have a red flag
    ArrayList fldIDsToSynch = new ArrayList();
    for (int i=0; i<fldIDsWithRedFlagL.size(); i++) {
      Long fldId = (Long) fldIDsWithRedFlagL.get(i);
      boolean wasFetched = cache.wasFolderFetchRequestIssued(fldId);
      boolean wasInvalidated = cache.wasFolderViewInvalidated(fldId);
      if (!wasFetched || wasInvalidated) {
        fldIDsToSynch.add(fldId);
      }
    }
    if (fldIDsToSynch.size() > 0)
      FolderOps.runResynchFolders_Delayed(getServerInterfaceLayer(), fldIDsWithRedFlagL, 5000);

    if (trace != null) trace.exit(FldARedFlagCount.class, null);
    return null;
  }

}