/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.usr;

import com.CH_cl.service.actions.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.Str_Rp;
import com.CH_co.util.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description:
 *
 * Class Details:
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class UsrASuspended extends ClientMessageAction {

  /** Creates new UsrARemove */
  public UsrASuspended() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrASuspended.class, "UsrARemove()");
    if (trace != null) trace.exit(UsrASuspended.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrASuspended.class, "runAction(Connection)");

    getFetchedDataCache().clear();

    Str_Rp reply = (Str_Rp) getMsgDataSet();
    if (reply.message != null && reply.message.length() > 0) {
      String title = "Account Suspended";
      NotificationCenter.show(NotificationCenter.WARNING_MESSAGE, title, reply.message);
    }

    if (trace != null) trace.exit(UsrASuspended.class, null);
    return null;
  }

}