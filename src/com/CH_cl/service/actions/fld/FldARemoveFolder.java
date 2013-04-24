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

import com.CH_cl.service.actions.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.fld.*;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FldARemoveFolder extends ClientMessageAction {

  /** Creates new FldARemoveFolder */
  public FldARemoveFolder() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldARemoveFolder.class, "FldARemoveFolder()");
    if (trace != null) trace.exit(FldARemoveFolder.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldARemoveFolder.class, "runAction(Connection)");

    Fld_Folders_Rp reply = (Fld_Folders_Rp) getMsgDataSet();
    FolderRecord[] folderRecords = reply.folderRecords;
    FolderShareRecord[] shareRecords = reply.shareRecords;

    getFetchedDataCache().removeFolderRecords(folderRecords);
    getFetchedDataCache().removeFolderShareRecords(shareRecords);

    if (trace != null) trace.exit(FldARemoveFolder.class, null);
    return null;
  }

}