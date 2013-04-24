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

package com.CH_cl.service.actions.usr;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.*;

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
public class UsrARemove extends ClientMessageAction {

  /** Creates new UsrARemove */
  public UsrARemove() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrARemove.class, "UsrARemove()");
    if (trace != null) trace.exit(UsrARemove.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrARemove.class, "runAction(Connection)");

    Obj_IDList_Co reply = (Obj_IDList_Co) getMsgDataSet();
    Long[] userIDs = reply.IDs;

    FetchedDataCache cache = getFetchedDataCache();
    cache.removeUserRecords(cache.getUserRecords(userIDs));

    // fire removal of shares
    cache.removeFolderShareRecords(cache.getFolderShareRecordsForUsers(userIDs));
    // fire removal of folders
    cache.removeFolderRecords(cache.getFolderRecordsForUsers(userIDs));
    // fire removal of contacts
    cache.removeContactRecords(cache.getContactRecordsForUsers(userIDs));

    if (trace != null) trace.exit(UsrARemove.class, null);
    return null;
  }

}