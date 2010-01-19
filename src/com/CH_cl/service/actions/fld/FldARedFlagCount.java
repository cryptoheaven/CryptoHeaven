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

import java.util.*;

import com.CH_co.trace.Trace;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
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
    Vector updatedFoldersV = new Vector();

    for (int i=0; i<ids[0].length; i++) {
      Long folderId = ids[0][i];
      Long redFlagCount = ids[1][i];

      FolderRecord folderRecord = cache.getFolderRecord(folderId);
      if (folderRecord != null) {
        boolean suppressSound = folderRecord.folderId.equals(cache.getUserRecord().junkFolderId) || folderRecord.folderId.equals(cache.getUserRecord().recycleFolderId);
        folderRecord.setUpdated(redFlagCount.intValue(), suppressSound);
        updatedFoldersV.addElement(folderRecord);
      }
    }

    if (updatedFoldersV.size() > 0) {
      FolderRecord[] updatedFolders = (FolderRecord[]) ArrayUtils.toArray(updatedFoldersV, FolderRecord.class);
      // Cause folder listeners to do visual update.
      cache.addFolderRecords(updatedFolders);
    }

    if (trace != null) trace.exit(FldARedFlagCount.class, null);
    return null;
  }

}