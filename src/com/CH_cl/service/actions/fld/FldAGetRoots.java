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

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;

import com.CH_co.trace.Trace;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FldAGetRoots extends ClientMessageAction {

  /** Creates new FldAGetRoots */
  public FldAGetRoots() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldAGetRoots.class, "FldAGetRoots()");
    if (trace != null) trace.exit(FldAGetRoots.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldAGetRoots.class, "runAction(Connection)");

    Fld_Folders_Rp dataSet = (Fld_Folders_Rp) getMsgDataSet();
    FolderRecord[] folderRecords = dataSet.folderRecords;
    FolderShareRecord[] shareRecords = dataSet.shareRecords;

    ServerInterfaceLayer SIL = getServerInterfaceLayer();
    FetchedDataCache cache = SIL.getFetchedDataCache();

    cache.removeFolderRecords(cache.getFolderRecords());
    FldAGetFolders.runAction(SIL, folderRecords, shareRecords);

    if (folderRecords != null && folderRecords.length > 0) {
      Long[] folderIDs = RecordUtils.getIDs(folderRecords);
      SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_CHILDREN, new Obj_IDList_Co(folderIDs)));
    }

    if (trace != null) trace.exit(FldAGetRoots.class, null);
    return null;
  }

}