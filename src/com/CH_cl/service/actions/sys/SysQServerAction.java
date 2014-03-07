/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
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
import com.CH_co.service.msg.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class SysQServerAction extends ClientMessageAction {

  /** Creates new SysQServerAction */
  public SysQServerAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysQServerAction.class, "SysQServerAction()");
    if (trace != null) trace.exit(SysQServerAction.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysQServerAction.class, "runAction(Connection)");

    // Mostly, this ClientMessageAction object is a NOOP!
    if (getActionCode() == CommandCodes.SYSNET_A_LOGIN_FAILED) {
      if (trace != null) trace.data(10, "SYSNET_A_LOGIN_FAILED");
      getServerInterfaceLayer().destroyServer();
    }

    if (trace != null) trace.exit(SysQServerAction.class, null);
    return null;
  }

}