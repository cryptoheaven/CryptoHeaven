/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_cl.service.actions.msg;

import com.CH_cl.service.actions.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
public class MsgARemove extends ClientMessageAction {

  /** Creates new MsgARemove */
  public MsgARemove() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgARemove.class, "MsgARemove()");
    if (trace != null) trace.exit(MsgARemove.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgARemove.class, "runAction(Connection)");

    Obj_IDList_Co reply = (Obj_IDList_Co) getMsgDataSet();
    Long[] msgLinkIDs = reply.IDs;

    getFetchedDataCache().removeMsgLinkRecords(msgLinkIDs);

    if (trace != null) trace.exit(MsgARemove.class, null);
    return null;
  }

}