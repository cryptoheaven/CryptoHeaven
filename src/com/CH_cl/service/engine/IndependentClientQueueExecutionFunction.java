/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.engine;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_co.queue.ProcessingFunctionI;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.trace.Trace;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.10 $</b>
*
* @author  Marcin Kurzawa
*/
public class IndependentClientQueueExecutionFunction extends Object implements ProcessingFunctionI {

  /** Creates new IndependentClientQueueExecutionFunction */
  public IndependentClientQueueExecutionFunction(ServerInterfaceLayer serverInterfaceLayer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(IndependentClientQueueExecutionFunction.class, "IndependentClientQueueExecutionFunction()");
    if (trace != null) trace.exit(IndependentClientQueueExecutionFunction.class);
  }

  /* =======================================================
  Methods from ProcessingFunctionI for the 'executionQueue'
  ========================================================= */
  /** start processing cached objects */
  public Object processQueuedObject(Object obj) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(IndependentClientQueueExecutionFunction.class, "processQueuedObject(Object)");
    if (trace != null) trace.args(obj);

    MessageAction reply = null;
    try {
      ClientMessageAction nextMsgAction = (ClientMessageAction) obj;
      reply = DefaultReplyRunner.runAction(nextMsgAction);
    } catch (Throwable t) {
      // execution of server reply went wrong -- critical error.
      if (trace != null) trace.exception(IndependentClientQueueExecutionFunction.class, 100, t);
    }

    if (trace != null) trace.exit(IndependentClientQueueExecutionFunction.class, reply);
    return reply;
  }

}