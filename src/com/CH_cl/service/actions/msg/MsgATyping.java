/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.msg;

import com.CH_cl.service.actions.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.obj.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MsgATyping extends ClientMessageAction {

  /** Creates new MsgATyping */
  public MsgATyping() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgATyping.class, "MsgATyping()");
    if (trace != null) trace.exit(MsgATyping.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgATyping.class, "runAction(Connection)");

    Obj_List_Co reply = (Obj_List_Co) getMsgDataSet();
    getServerInterfaceLayer().getFetchedDataCache().fireMsgTypingEvent(reply);

    if (trace != null) trace.exit(MsgATyping.class, null);
    return null;
  }

}