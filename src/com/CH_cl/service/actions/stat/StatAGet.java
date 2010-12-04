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

package com.CH_cl.service.actions.stat;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.stat.*;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StatAGet extends ClientMessageAction {

  /** Creates new StatAGet */
  public StatAGet() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatAGet.class, "StatAGet()");
    if (trace != null) trace.exit(StatAGet.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatAGet.class, "runAction(Connection)");

    // get the returning data set
    Stats_Get_Rp reply = (Stats_Get_Rp) getMsgDataSet();
    StatRecord[] statRecords = reply.stats;

    UserOps.fetchUnknownUsers(getServerInterfaceLayer(), statRecords);

    FetchedDataCache cache = getFetchedDataCache();
    cache.addStatRecords(statRecords);

    // Gather all Message Links and File Links that are involved to cause notification of listeners for link objects.
    MsgLinkRecord[] msgLinks = cache.getMsgLinkRecords(RecordUtils.getIDs(statRecords));
    FileLinkRecord[] fileLinks = cache.getFileLinkRecords(RecordUtils.getIDs(statRecords));

    if (msgLinks != null && msgLinks.length > 0)
      cache.addMsgLinkRecords(msgLinks);
    if (fileLinks != null && fileLinks.length > 0)
      cache.addFileLinkRecords(fileLinks);

    if (trace != null) trace.exit(StatAGet.class, null);
    return null;
  }

}