/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_cl.service.actions.sys;

import com.CH_cl.service.actions.*;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.trace.Trace;

/**
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SysELogin extends ClientMessageAction {

  /** Creates new SysELogin */
  public SysELogin() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysELogin.class, "SysELogin()");
    if (trace != null) trace.exit(SysELogin.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysELogin.class, "runAction(Connection)");

    if (trace != null) trace.exit(SysELogin.class, null);
    return null;
  }

}