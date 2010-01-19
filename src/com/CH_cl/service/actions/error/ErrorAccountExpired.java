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
import com.CH_co.service.msg.dataSets.Str_Rp;
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
public class ErrorAccountExpired extends ClientMessageAction {

  /** Creates new ErrorAccountExpired */
  public ErrorAccountExpired() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ErrorAccountExpired.class, "ErrorAccountExpired()");
    if (trace != null) trace.exit(ErrorAccountExpired.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ErrorAccountExpired.class, "runAction(Connection)");

    ProgMonitor pm = ProgMonitorPool.getProgMonitor(getStamp());
    if (pm != null)
      pm.jobKilled();

    Str_Rp reply = (Str_Rp) getMsgDataSet();
    String msg = reply.message;
    MessageDialog.showErrorDialog(null, msg, "Account Expired");

    if (trace != null) trace.exit(ErrorAccountExpired.class, null);
    return null;
  }

}