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

package com.CH_cl.service.actions.usr;

import com.CH_cl.service.actions.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.Str_Rp;
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