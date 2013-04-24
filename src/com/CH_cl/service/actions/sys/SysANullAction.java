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

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: Action that does nothing.
 *                    When client cannot recognize the request, it will try
 *                    to encapsulate it in this action... provided that the data set
 *                    will be of valid format, connection will survive, if not
 *                    then client will be thrown off.  Useful to increase compatibility
 *                    between newer and older versions.
 * Class Details:
 *
 *
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class SysANullAction extends ClientMessageAction {

  /** Creates new SysANullAction */
  public SysANullAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysANullAction.class, "SysANullAction()");
    if (trace != null) trace.exit(SysANullAction.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysANullAction.class, "runAction(Connection)");

    if (trace != null) trace.exit(SysANullAction.class, null);
    return null;
  }

}