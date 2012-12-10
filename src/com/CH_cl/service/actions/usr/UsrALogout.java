/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_cl.service.actions.usr;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.trace.Trace;

/** 
* <b>Copyright</b> &copy; 2001-2012
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
* <b>$Revision: 1.8 $</b>
* @author  Marcin Kurzawa
* @version 
*/
public class UsrALogout extends ClientMessageAction {

  /** Creates new UsrALogout */
  public UsrALogout() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrALogout.class, "UsrALogout()");
    if (trace != null) trace.exit(UsrALogout.class);
  }

  /**
  * The action handler performs all actions related to the received message (reply),
  * and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrALoginSecureSession.class, "runAction()");

    getClientContext().login(false);
    getServerInterfaceLayer().destroyWorkers();

    // Unlock the Reader and Writer because synchronized request-reply is done.
    getClientContext().releaseLoginStreamers();

    if (trace != null) trace.exit(UsrALoginSecureSession.class);
    return null;
  }
}