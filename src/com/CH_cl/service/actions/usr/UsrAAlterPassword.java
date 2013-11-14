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

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class UsrAAlterPassword extends ClientMessageAction {

  /** Creates new UsrAAlterPassword */
  public UsrAAlterPassword() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrAAlterPassword.class, "UsrAAlterPassword()");
    if (trace != null) trace.exit(UsrAAlterPassword.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrAAlterPassword.class, "runAction(Connection)");

    if (trace != null) trace.exit(UsrAAlterPassword.class, null);
    return null;
  }

}