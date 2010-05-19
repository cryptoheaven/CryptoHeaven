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

package com.CH_cl.service.actions.error;

import com.CH_cl.service.actions.*;

import com.CH_co.monitor.*;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.util.*;
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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ErrorStorageExceeded extends ClientMessageAction {

  /** Creates new ErrorStorageExceeded */
  public ErrorStorageExceeded() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ErrorStorageExceeded.class, "ErrorStorageExceeded()");
    if (trace != null) trace.exit(ErrorStorageExceeded.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ErrorStorageExceeded.class, "runAction(Connection)");

    ProgMonitorI pm = ProgMonitorPool.getProgMonitor(getStamp());
    if (pm != null)
      pm.jobKilled();

    Str_Rp reply = (Str_Rp) getMsgDataSet();
    String title = "Server Storage Limit Exceeded";
    String msg = reply.message;
    NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, title, msg);

    if (trace != null) trace.exit(ErrorStorageExceeded.class, null);
    return null;
  }

}