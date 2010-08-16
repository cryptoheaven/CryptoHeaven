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
import com.CH_co.util.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.service.msg.MessageAction;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class UsrALoginFailed extends ClientMessageAction {

  /** Creates new UsrALoginFailed */
  public UsrALoginFailed() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrALoginFailed.class, "UsrALoginFailed()");
    if (trace != null) trace.exit(UsrALoginFailed.class);
  }

  /** The action handler performs all actions related to the received error message (reply),
      and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrALoginFailed.class, "runAction()");
    // Unlock the Reader and Writer because login sequence is done, wether successful or not.
    getClientContext().releaseLoginStreamers();
    Str_Rp dataSet = (Str_Rp) getMsgDataSet();

    String msg = dataSet.message;
    if (msg != null && msg.length() > 0 && msg.toLowerCase().indexOf("please specify the account pass") < 0) {
      String title = "Login Failed";
      NotificationCenter.show(NotificationCenter.WARNING_MESSAGE, title, msg);
    }

    if (trace != null) trace.exit(UsrALoginFailed.class, null);
    return null;
  }

}