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

import com.CH_co.service.msg.MessageAction;
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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class UsrAGetCumulativeUsage extends ClientMessageAction {

  /** Creates new UsrAGetCumulativeUsage */
  public UsrAGetCumulativeUsage() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrAGetCumulativeUsage.class, "UsrAGetCumulativeUsage()");
    if (trace != null) trace.exit(UsrAGetCumulativeUsage.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrAGetCumulativeUsage.class, "runAction(Connection)");

    // no action

    if (trace != null) trace.exit(UsrAGetCumulativeUsage.class, null);
    return null;
  }

}