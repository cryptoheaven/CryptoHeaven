/*
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
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
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class UsrARecycleSessionSequence extends ClientMessageAction {

  /** Creates new UsrARecycleSessionSequence */
  public UsrARecycleSessionSequence() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrARecycleSessionSequence.class, "UsrARecycleSessionSequence()");
    if (trace != null) trace.exit(UsrARecycleSessionSequence.class);
  }

  /**
   * The action handler performs all actions related to the received error message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrARecycleSessionSequence.class, "runAction()");
    // no-op
    if (trace != null) trace.exit(UsrARecycleSessionSequence.class, null);
    return null;
  }

}