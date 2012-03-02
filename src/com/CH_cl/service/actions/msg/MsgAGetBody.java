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

package com.CH_cl.service.actions.msg;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.msg.*;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.msg.*;
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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgAGetBody extends ClientMessageAction {

  /** Creates new MsgAGetBody */
  public MsgAGetBody() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgAGetBody.class, "MsgAGetBody()");
    if (trace != null) trace.exit(MsgAGetBody.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgAGetBody.class, "runAction(Connection)");

    // reply syntax:
    // 10420 <msgLinkId> <status> <dateDelivered> <dateUpdated>   <msgId> <encText> <encSignedDigest> <sendPrivKeyId>
    Msg_GetBody_Rp reply = (Msg_GetBody_Rp) getMsgDataSet();
    MsgLinkRecord linkRecord = reply.linkRecord;
    MsgDataRecord dataRecord = reply.dataRecord;
    StatRecord[] statRecords = reply.stats_rp != null ? reply.stats_rp.stats : null;

    FetchedDataCache cache = getFetchedDataCache();

    if (statRecords != null)
      cache.addStatRecords(statRecords);

    // We need data Records in the cache before the message table can display contents.
    // For that reason, the event will be fired when we are done with both, links and datas.
    // Skip adding the BODY if the link was already deleted and this reply came late as effect of connection problems
    MsgLinkRecord prevLinkRecord = cache.getMsgLinkRecord(linkRecord.msgLinkId);
    if (prevLinkRecord != null) {
      prevLinkRecord.merge(linkRecord);
      cache.addMsgLinkAndDataRecords(prevLinkRecord, dataRecord);
    }

    if (trace != null) trace.exit(MsgAGetBody.class, null);
    return null;
  }

}