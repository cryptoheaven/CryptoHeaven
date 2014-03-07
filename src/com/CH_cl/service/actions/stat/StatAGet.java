/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
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
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
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
    Long[] msgLinkIDs = StatRecord.getLinkIDs(statRecords, StatRecord.STAT_TYPE_MESSAGE);
    Long[] fileLinkIDs = StatRecord.getLinkIDs(statRecords, StatRecord.STAT_TYPE_FILE);
    MsgLinkRecord[] msgLinks = cache.getMsgLinkRecords(msgLinkIDs);
    FileLinkRecord[] fileLinks = cache.getFileLinkRecords(fileLinkIDs);

    if (msgLinks != null && msgLinks.length > 0)
      cache.addMsgLinkRecords(msgLinks);
    if (fileLinks != null && fileLinks.length > 0)
      cache.addFileLinkRecords(fileLinks);

    if (trace != null) trace.exit(StatAGet.class, null);
    return null;
  }

}