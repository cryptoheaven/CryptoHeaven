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

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_co.monitor.Stats;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.0 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class SysATimeoutPoke extends ClientMessageAction {

  /** Creates new SysATimeoutPoke */
  public SysATimeoutPoke() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysATimeoutPoke.class, "SysATimeoutPoke()");
    if (trace != null) trace.exit(SysATimeoutPoke.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysATimeoutPoke.class, "runAction(Connection)");

    Stats.setStatusAll("Poked, socs:" + Stats.getConnectionsPlain() + ", http:" + Stats.getConnectionsHTML() + (Stats.getPingMS() != null ? ", p:" + Stats.getPingMS() + " ms" : "") + ", mw#:"+Stats.getMainWorkerCounter());

    if (trace != null) trace.exit(SysATimeoutPoke.class, null);
    return null;
  }
 
}